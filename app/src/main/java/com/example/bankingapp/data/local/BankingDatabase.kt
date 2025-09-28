package com.example.bankingapp.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.bankingapp.data.local.dao.AccountDao
import com.example.bankingapp.data.local.dao.CacheMetadataDao
import com.example.bankingapp.data.local.dao.CardDao
import com.example.bankingapp.data.local.dao.TransactionDao
import com.example.bankingapp.data.local.entity.AccountEntity
import com.example.bankingapp.data.local.entity.CacheMetadataEntity
import com.example.bankingapp.data.local.entity.CardEntity
import com.example.bankingapp.data.local.entity.TransactionEntity

/**
 * Main Room database for the banking app
 */
@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CardEntity::class,
        CacheMetadataEntity::class
    ],
    version = 1,
    exportSchema = true
)
//@TypeConverters(DatabaseConverters::class)
abstract class BankingDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun cardDao(): CardDao
    abstract fun cacheMetadataDao(): CacheMetadataDao

    companion object {
        const val DATABASE_NAME = "banking_database"

        // Migration from version 1 to 2 (example for future use)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration - add new column
                // database.execSQL("ALTER TABLE accounts ADD COLUMN newColumn TEXT")
            }
        }
    }
}