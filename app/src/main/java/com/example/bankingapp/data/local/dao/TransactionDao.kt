package com.example.bankingapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bankingapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactions(accountId: String, limit: Int, offset: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsFlow(accountId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND type = :type ORDER BY date DESC LIMIT :limit")
    suspend fun getTransactionsByType(accountId: String, type: String, limit: Int): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun clearTransactions(accountId: String)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun getTransactionCount(accountId: String): Int

    @Query("SELECT MAX(date) FROM transactions WHERE accountId = :accountId")
    suspend fun getLatestTransactionDate(accountId: String): Long?

    @Query("DELETE FROM transactions WHERE accountId = :accountId AND date < :beforeDate")
    suspend fun deleteOldTransactions(accountId: String, beforeDate: Long)
}