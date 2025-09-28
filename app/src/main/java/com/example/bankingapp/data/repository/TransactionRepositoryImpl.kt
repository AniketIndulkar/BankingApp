package com.example.bankingapp.data.repository

import com.example.bankingapp.data.local.dao.TransactionDao
import com.example.bankingapp.data.mapper.TransactionMapper
import com.example.bankingapp.data.remote.BankingApiService
import com.example.bankingapp.domain.model.AppError
import com.example.bankingapp.domain.model.Transaction
import com.example.bankingapp.domain.repository.TransactionRepository
import com.example.bankingapp.security.EncryptionManager
import com.example.bankingapp.utils.NetworkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.bankingapp.domain.model.Result

class TransactionRepositoryImpl(
    private val apiService: BankingApiService,
    private val transactionDao: TransactionDao,
    private val encryptionManager: EncryptionManager,
    private val networkManager: NetworkManager
) : TransactionRepository {

    private val transactionMapper = TransactionMapper(encryptionManager)
    private val cacheExpiryTime = 10 * 60 * 1000L // 10 minutes

    override suspend fun getTransactionHistory(
        page: Int,
        pageSize: Int,
        forceRefresh: Boolean
    ): Flow<Result<List<Transaction>>> = flow {
        try {
            // Always emit cached data first
            val cachedTransactions = getCachedTransactions(page, pageSize)
            if (cachedTransactions.isNotEmpty()) {
                emit(Result.Success(cachedTransactions))
            } else {
                emit(Result.Loading)
            }

            // Check if we need to refresh
            val shouldRefresh = forceRefresh ||
                    cachedTransactions.isEmpty() ||
                    isCacheExpired() ||
                    networkManager.isConnected()

            if (shouldRefresh && networkManager.isConnected()) {
                val refreshResult = refreshTransactions()
                when (refreshResult) {
                    is Result.Success -> {
                        // Get fresh data from cache after refresh
                        val freshTransactions = getCachedTransactions(page, pageSize)
                        emit(Result.Success(freshTransactions))
                    }
                    is Result.Error -> {
                        if (cachedTransactions.isEmpty()) {
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

    override suspend fun getCachedTransactions(page: Int, pageSize: Int): List<Transaction> {
        return try {
            val offset = page * pageSize
            transactionDao.getTransactions("acc_12345", pageSize, offset)
                .map { transactionMapper.entityToDomain(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun refreshTransactions(): Result<List<Transaction>> {
        return try {
            if (!networkManager.isConnected()) {
                return Result.Error(AppError.NetworkError("No internet connection"))
            }

            val response = apiService.getTransactions()
            if (response.isSuccessful && response.body() != null) {
                val transactionDtos = response.body()!!
                val transactions = transactionDtos.map { transactionMapper.dtoToDomain(it) }

                // Clear old transactions and insert new ones
                transactionDao.clearTransactions("acc_12345")
                transactionDao.insertTransactions(
                    transactions.map { transactionMapper.domainToEntity(it) }
                )

                Result.Success(transactions)
            } else {
                Result.Error(AppError.NetworkError("Failed to fetch transactions"))
            }
        } catch (e: Exception) {
            Result.Error(AppError.NetworkError(e.message ?: "Network error"))
        }
    }

    override suspend fun getTransactionById(id: String): Result<Transaction> {
        return try {
            // Try cache first
            transactionDao.getTransactionById(id)?.let { entity ->
                val transaction = transactionMapper.entityToDomain(entity)
                return Result.Success(transaction)
            }

            // If not in cache and network available, fetch from API
            if (networkManager.isConnected()) {
                val response = apiService.getTransactionById(id)
                if (response.isSuccessful && response.body() != null) {
                    val transaction = transactionMapper.dtoToDomain(response.body()!!)
                    // Cache the transaction
                    transactionDao.insertTransaction(transactionMapper.domainToEntity(transaction))
                    Result.Success(transaction)
                } else {
                    Result.Error(AppError.DataNotFoundError("Transaction not found"))
                }
            } else {
                Result.Error(AppError.DataNotFoundError("Transaction not found in cache"))
            }
        } catch (e: Exception) {
            Result.Error(AppError.UnknownError(e.message ?: "Unknown error"))
        }
    }

    private suspend fun isCacheExpired(): Boolean {
        val latestDate = transactionDao.getLatestTransactionDate("acc_12345") ?: return true
        val currentTime = System.currentTimeMillis()
        return (currentTime - latestDate * 1000) > cacheExpiryTime
    }
}
