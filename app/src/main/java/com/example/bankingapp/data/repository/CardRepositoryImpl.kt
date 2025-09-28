package com.example.bankingapp.data.repository

import com.example.bankingapp.data.local.dao.CardDao
import com.example.bankingapp.data.mapper.CardMapper
import com.example.bankingapp.data.remote.BankingApiService
import com.example.bankingapp.domain.model.AppError
import com.example.bankingapp.domain.model.PaymentCard
import com.example.bankingapp.domain.repository.CardRepository
import com.example.bankingapp.security.EncryptionManager
import com.example.bankingapp.utils.NetworkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.bankingapp.domain.model.Result

class CardRepositoryImpl(
    private val apiService: BankingApiService,
    private val cardDao: CardDao,
    private val encryptionManager: EncryptionManager,
    private val networkManager: NetworkManager
) : CardRepository {

    private val cardMapper = CardMapper(encryptionManager)
    private val cacheExpiryTime = 15 * 60 * 1000L // 15 minutes

    override suspend fun getCards(forceRefresh: Boolean): Flow<Result<List<PaymentCard>>> = flow {
        try {
            // Always emit cached data first
            val cachedCards = getCachedCards()
            if (cachedCards.isNotEmpty()) {
                emit(Result.Success(cachedCards))
            } else {
                emit(Result.Loading)
            }

            // Check if we need to refresh
            val shouldRefresh = forceRefresh ||
                    cachedCards.isEmpty() ||
                    isCacheExpired() ||
                    networkManager.isConnected()

            if (shouldRefresh && networkManager.isConnected()) {
                val refreshResult = refreshCards()
                when (refreshResult) {
                    is Result.Success -> emit(Result.Success(refreshResult.data))
                    is Result.Error -> {
                        if (cachedCards.isEmpty()) {
                            emit(refreshResult)
                        }
                    }
                    is Result.Loading -> { /* Ignore */ }
                }
            }
        } catch (e: Exception) {
            emit(Result.Error(AppError.UnknownError(e.message ?: "Unknown error")))
        }
    }

    override suspend fun getCardById(cardId: String): Result<PaymentCard> {
        return try {
            // Try cache first
            cardDao.getCardById(cardId)?.let { entity ->
                val card = cardMapper.entityToDomain(entity)
                return Result.Success(card)
            }

            // If not in cache and network available, fetch from API
            if (networkManager.isConnected()) {
                val response = apiService.getCardById(cardId)
                if (response.isSuccessful && response.body() != null) {
                    val card = cardMapper.dtoToDomain(response.body()!!)
                    // Cache the card
                    cardDao.insertCard(cardMapper.domainToEntity(card))
                    Result.Success(card)
                } else {
                    Result.Error(AppError.DataNotFoundError("Card not found"))
                }
            } else {
                Result.Error(AppError.DataNotFoundError("Card not found in cache"))
            }
        } catch (e: Exception) {
            Result.Error(AppError.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getCachedCards(): List<PaymentCard> {
        return try {
            cardDao.getCardsByAccount("acc_12345")
                .map { cardMapper.entityToDomain(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun refreshCards(): Result<List<PaymentCard>> {
        return try {
            if (!networkManager.isConnected()) {
                return Result.Error(AppError.NetworkError("No internet connection"))
            }

            val response = apiService.getCards()
            if (response.isSuccessful && response.body() != null) {
                val cardDtos = response.body()!!
                val cards = cardDtos.map { cardMapper.dtoToDomain(it) }

                // Clear old cards and insert new ones
                cardDao.clearCards("acc_12345")
                cardDao.insertCards(cards.map { cardMapper.domainToEntity(it) })

                Result.Success(cards)
            } else {
                Result.Error(AppError.NetworkError("Failed to fetch cards"))
            }
        } catch (e: Exception) {
            Result.Error(AppError.NetworkError(e.message ?: "Network error"))
        }
    }

    override suspend fun toggleCard(cardId: String, isActive: Boolean): Result<PaymentCard> {
        return try {
            if (!networkManager.isConnected()) {
                return Result.Error(AppError.NetworkError("No internet connection"))
            }

            val response = apiService.toggleCard(cardId, isActive)
            if (response.isSuccessful && response.body() != null) {
                val card = cardMapper.dtoToDomain(response.body()!!)
                // Update cache
                cardDao.updateCard(cardMapper.domainToEntity(card))
                Result.Success(card)
            } else {
                Result.Error(AppError.NetworkError("Failed to toggle card"))
            }
        } catch (e: Exception) {
            Result.Error(AppError.NetworkError(e.message ?: "Network error"))
        }
    }

    private suspend fun isCacheExpired(): Boolean {
        val cardCount = cardDao.getCardCount("acc_12345")
        if (cardCount == 0) return true

        // For simplicity, we'll consider cache expired based on time
        // In a real app, you might store cache metadata
        return false // Implement proper cache expiry logic
    }
}