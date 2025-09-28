package com.example.bankingapp.domain.usecase

import com.example.bankingapp.domain.model.AppError
import com.example.bankingapp.domain.repository.AccountRepository
import com.example.bankingapp.domain.repository.CardRepository
import com.example.bankingapp.domain.repository.TransactionRepository
import com.example.bankingapp.domain.model.Result

class RefreshDataUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Run all refresh operations in parallel
            val accountResult = accountRepository.refreshAccountData()
            val transactionsResult = transactionRepository.refreshTransactions()
            val cardsResult = cardRepository.refreshCards()

            when {
                accountResult is Result.Error -> accountResult
                transactionsResult is Result.Error -> transactionsResult
                cardsResult is Result.Error -> cardsResult
                else -> Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error(
                AppError.UnknownError(
                    e.message ?: "Unknown error occurred"
                )
            )
        }
    }
}