package com.example.bankingapp.domain.repository

import com.example.bankingapp.domain.model.Result
import com.example.bankingapp.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    /**
     * Get transaction history with pagination
     * @param page page number (0-based)
     * @param pageSize number of transactions per page
     * @param forceRefresh true to fetch from network
     */
    suspend fun getTransactionHistory(
        page: Int = 0,
        pageSize: Int = 20,
        forceRefresh: Boolean = false
    ): Flow<Result<List<Transaction>>>

    /**
     * Get cached transactions for offline mode
     */
    suspend fun getCachedTransactions(
        page: Int = 0,
        pageSize: Int = 20
    ): List<Transaction>

    /**
     * Refresh transactions from network
     */
    suspend fun refreshTransactions(): Result<List<Transaction>>

    /**
     * Get transaction by ID
     */
    suspend fun getTransactionById(id: String): com.example.bankingapp.domain.model.Result<Transaction>
}