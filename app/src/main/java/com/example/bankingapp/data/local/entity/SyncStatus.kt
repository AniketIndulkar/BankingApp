package com.example.bankingapp.data.local.entity

enum class SyncStatus {
    SYNCED,     // Data is synchronized with server
    PENDING,    // Data waiting to be synced
    FAILED      // Sync failed, retry needed
}