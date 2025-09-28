package com.example.bankingapp.domain.usecase

import com.example.bankingapp.domain.model.BankAccount
import com.example.bankingapp.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import com.example.bankingapp.domain.model.Result

class GetAccountBalanceUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Flow<Result<BankAccount>> {
        return accountRepository.getAccountBalance(forceRefresh)
    }
}