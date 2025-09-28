package com.example.bankingapp.domain.repository

import com.example.bankingapp.domain.model.PaymentCard
import com.example.bankingapp.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    /**
     * Get all payment cards
     */
    suspend fun getCards(forceRefresh: Boolean = false): Flow<Result<List<PaymentCard>>>

    /**
     * Get card by ID
     */
    suspend fun getCardById(cardId: String): Result<PaymentCard>

    /**
     * Get cached cards for offline mode
     */
    suspend fun getCachedCards(): List<PaymentCard>

    /**
     * Refresh cards from network
     */
    suspend fun refreshCards(): Result<List<PaymentCard>>

    /**
     * Enable/disable card
     */
    suspend fun toggleCard(cardId: String, isActive: Boolean): Result<PaymentCard>
}