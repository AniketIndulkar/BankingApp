package com.example.bankingapp.domain.model

import java.time.LocalDateTime

data class BankAccount(
    val id: String,
    val accountNumber: String,
    val accountType: AccountType,
    val balance: MoneyAmount,
    val currency: Currency,
    val isActive: Boolean,
    val lastUpdated: LocalDateTime,
    val createdDate: LocalDateTime
)