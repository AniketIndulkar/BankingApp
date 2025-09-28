package com.example.bankingapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["date"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val accountId: String,
    val amount: String, // Encrypted amount
    val currency: String,
    val type: String,
    val status: String,
    val description: String, // Encrypted description
    val recipientName: String?, // Encrypted if not null
    val recipientAccount: String?, // Encrypted if not null
    val reference: String?,
    val date: Long, // Unix timestamp
    val balanceAfter: String?, // Encrypted if not null
    val balanceAfterCurrency: String?,
    val syncStatus: String = "SYNCED",
    val createdAt: Long = System.currentTimeMillis()
)