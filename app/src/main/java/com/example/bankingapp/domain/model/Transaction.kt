package com.example.bankingapp.domain.model

import java.time.LocalDateTime

data class Transaction(
    val id: String,
    val accountId: String,
    val amount: MoneyAmount,
    val type: TransactionType,
    val status: TransactionStatus,
    val description: String,
    val recipientName: String?,
    val recipientAccount: String?,
    val reference: String?,
    val date: LocalDateTime,
    val balanceAfter: MoneyAmount?
)