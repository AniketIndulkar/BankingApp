package com.example.bankingapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bankingapp.data.local.entity.CacheMetadataEntity

@Dao
interface CacheMetadataDao {

    @Query("SELECT * FROM cache_metadata WHERE dataType = :dataType")
    suspend fun getCacheMetadata(dataType: String): CacheMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCacheMetadata(metadata: CacheMetadataEntity)

    @Query("UPDATE cache_metadata SET lastFetchTime = :fetchTime, expiryTime = :expiryTime, isExpired = :isExpired, recordCount = :recordCount WHERE dataType = :dataType")
    suspend fun updateCacheMetadata(
        dataType: String,
        fetchTime: Long,
        expiryTime: Long,
        isExpired: Boolean,
        recordCount: Int
    )

    @Query("SELECT isExpired FROM cache_metadata WHERE dataType = :dataType")
    suspend fun isCacheExpired(dataType: String): Boolean?

    @Query("DELETE FROM cache_metadata WHERE dataType = :dataType")
    suspend fun deleteCacheMetadata(dataType: String)

    @Query("DELETE FROM cache_metadata")
    suspend fun clearAllCacheMetadata()

    @Query("UPDATE cache_metadata SET isExpired = 1")
    suspend fun expireAllCache()
}