package com.example.bankingapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "cache_metadata",
    indices = [Index(value = ["dataType"], unique = true)]
)
data class CacheMetadataEntity(
    @PrimaryKey
    val dataType: String, // "account", "transactions", "cards"
    val lastFetchTime: Long, // Unix timestamp
    val expiryTime: Long, // Unix timestamp
    val isExpired: Boolean = false,
    val recordCount: Int = 0
)