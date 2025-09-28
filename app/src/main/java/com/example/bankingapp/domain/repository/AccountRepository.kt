package com.example.bankingapp.domain.repository

import com.example.bankingapp.domain.model.BankAccount
import com.example.bankingapp.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    /**
     * Get account balance with offline-first approach
     * @param forceRefresh true to fetch from network regardless of cache
     */
    suspend fun getAccountBalance(forceRefresh: Boolean = false): Flow<Result<BankAccount>>

    /**
     * Get account details
     */
    suspend fun getAccountDetails(): Flow<Result<BankAccount>>

    /**
     * Get cached account data for offline mode
     */
    suspend fun getCachedAccount(): BankAccount?

    /**
     * Refresh account data from network
     */
    suspend fun refreshAccountData(): Result<BankAccount>
}