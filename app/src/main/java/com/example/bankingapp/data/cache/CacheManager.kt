package com.example.bankingapp.data.cache

import com.example.bankingapp.data.local.dao.CacheMetadataDao
import com.example.bankingapp.data.local.entity.CacheDataTypes
import com.example.bankingapp.data.local.entity.CacheMetadataEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Cache manager for handling data expiry and cache policies
 */
class CacheManager(
    private val cacheMetadataDao: CacheMetadataDao
) {
    companion object {
        private const val ACCOUNT_CACHE_DURATION = 5 * 60 * 1000L // 5 minutes
        private const val TRANSACTION_CACHE_DURATION = 10 * 60 * 1000L // 10 minutes
        private const val CARD_CACHE_DURATION = 15 * 60 * 1000L // 15 minutes
    }

    /**
     * Check if cache is expired for a given data type
     */
    suspend fun isCacheExpired(dataType: String): Boolean {
        val metadata = cacheMetadataDao.getCacheMetadata(dataType)
        return metadata?.let {
            val currentTime = System.currentTimeMillis()
            currentTime > it.expiryTime || it.isExpired
        } ?: true
    }

    // In CacheManager.kt
    suspend fun isCacheValid(dataType: String): Boolean {
        val metadata = cacheMetadataDao.getCacheMetadata(dataType)
        return metadata?.let {
            val currentTime = System.currentTimeMillis()
            currentTime <= it.expiryTime && !it.isExpired && it.recordCount > 0
        } ?: false
    }

    /**
     * Update cache metadata when data is fetched
     */
    suspend fun updateCacheMetadata(dataType: String, recordCount: Int) {
        val currentTime = System.currentTimeMillis()
        val expiryTime = currentTime + getCacheDuration(dataType)

        val metadata = CacheMetadataEntity(
            dataType = dataType,
            lastFetchTime = currentTime,
            expiryTime = expiryTime,
            isExpired = false,
            recordCount = recordCount
        )

        cacheMetadataDao.insertCacheMetadata(metadata)
    }

    /**
     * Mark cache as expired for a data type
     */
    suspend fun expireCache(dataType: String) {
        val metadata = cacheMetadataDao.getCacheMetadata(dataType)
        metadata?.let {
            cacheMetadataDao.updateCacheMetadata(
                dataType = dataType,
                fetchTime = it.lastFetchTime,
                expiryTime = it.expiryTime,
                isExpired = true,
                recordCount = it.recordCount
            )
        }
    }

    /**
     * Get cache status for all data types
     */
    suspend fun getCacheStatus(): Flow<CacheStatus> = flow {
        val accountMeta = cacheMetadataDao.getCacheMetadata(CacheDataTypes.ACCOUNT)
        val transactionMeta = cacheMetadataDao.getCacheMetadata(CacheDataTypes.TRANSACTIONS)
        val cardMeta = cacheMetadataDao.getCacheMetadata(CacheDataTypes.CARDS)

        emit(
            CacheStatus(
                accountCached = accountMeta != null && !isCacheExpired(CacheDataTypes.ACCOUNT),
                transactionsCached = transactionMeta != null && !isCacheExpired(CacheDataTypes.TRANSACTIONS),
                cardsCached = cardMeta != null && !isCacheExpired(CacheDataTypes.CARDS),
                lastAccountUpdate = accountMeta?.lastFetchTime,
                lastTransactionUpdate = transactionMeta?.lastFetchTime,
                lastCardUpdate = cardMeta?.lastFetchTime
            )
        )
    }

    /**
     * Clear all cache metadata
     */
    suspend fun clearAllCache() {
        cacheMetadataDao.clearAllCacheMetadata()
    }

    /**
     * Get cache duration for data type
     */
    private fun getCacheDuration(dataType: String): Long {
        return when (dataType) {
            CacheDataTypes.ACCOUNT -> ACCOUNT_CACHE_DURATION
            CacheDataTypes.TRANSACTIONS -> TRANSACTION_CACHE_DURATION
            CacheDataTypes.CARDS -> CARD_CACHE_DURATION
            else -> ACCOUNT_CACHE_DURATION
        }
    }
}

/**
 * Cache status data class
 */
data class CacheStatus(
    val accountCached: Boolean,
    val transactionsCached: Boolean,
    val cardsCached: Boolean,
    val lastAccountUpdate: Long?,
    val lastTransactionUpdate: Long?,
    val lastCardUpdate: Long?
) {
    fun hasAnyCache(): Boolean = accountCached || transactionsCached || cardsCached

    fun isFullyCached(): Boolean = accountCached && transactionsCached && cardsCached
}

/**
 * Cache policy enum
 */
enum class CachePolicy {
    CACHE_FIRST,    // Always try cache first, then network
    NETWORK_FIRST,  // Always try network first, fallback to cache
    CACHE_ONLY,     // Only use cache, never network
    NETWORK_ONLY    // Only use network, never cache
}