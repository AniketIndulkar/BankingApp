package com.example.bankingapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room entity for bank account data
 */
@Entity(
    tableName = "accounts",
    indices = [Index(value = ["accountNumber"], unique = true)]
)
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val accountNumber: String, // Encrypted in storage
    val accountType: String,
    val balanceAmount: String, // Stored as encrypted string
    val currency: String,
    val isActive: Boolean,
    val lastUpdated: Long, // Unix timestamp
    val createdDate: Long, // Unix timestamp
    val syncStatus: String = "SYNCED" // SYNCED, PENDING, FAILED
)