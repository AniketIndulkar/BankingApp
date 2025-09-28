package com.example.bankingapp.domain.model

import java.time.LocalDateTime

data class PaymentCard(
    val id: String,
    val accountId: String,
    val cardNumber: String,
    val maskedNumber: String, // **** **** **** 1234
    val holderName: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String,
    val cardType: CardType,
    val brand: CardBrand,
    val isActive: Boolean,
    val isBlocked: Boolean,
    val dailyLimit: MoneyAmount,
    val monthlyLimit: MoneyAmount,
    val lastUsed: LocalDateTime?,
    val createdDate: LocalDateTime
)