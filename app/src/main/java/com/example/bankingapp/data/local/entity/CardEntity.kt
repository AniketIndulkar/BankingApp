package com.example.bankingapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["maskedNumber"])
    ]
)
data class CardEntity(
    @PrimaryKey
    val id: String,
    val accountId: String,
    val cardNumber: String, // Encrypted full card number
    val maskedNumber: String, // Safe to store unencrypted
    val holderName: String, // Encrypted
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String, // Encrypted
    val cardType: String,
    val brand: String,
    val isActive: Boolean,
    val isBlocked: Boolean,
    val dailyLimitAmount: String, // Encrypted
    val dailyLimitCurrency: String,
    val monthlyLimitAmount: String, // Encrypted
    val monthlyLimitCurrency: String,
    val lastUsed: Long?, // Unix timestamp
    val createdDate: Long, // Unix timestamp
    val syncStatus: String = "SYNCED"
)