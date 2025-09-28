package com.example.bankingapp.domain.usecase

import com.example.bankingapp.domain.model.Transaction
import com.example.bankingapp.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import com.example.bankingapp.domain.model.Result

class GetTransactionHistoryUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        page: Int = 0,
        pageSize: Int = 20,
        forceRefresh: Boolean = false
    ): Flow<Result<List<Transaction>>> {
        return transactionRepository.getTransactionHistory(page, pageSize, forceRefresh)
    }
}