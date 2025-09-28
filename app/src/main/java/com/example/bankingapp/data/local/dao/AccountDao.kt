package com.example.bankingapp.data.local.dao

import androidx.room.*
import com.example.bankingapp.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for account operations
 */
@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: String): AccountEntity?

    @Query("SELECT * FROM accounts LIMIT 1")
    suspend fun getAccount(): AccountEntity?

    @Query("SELECT * FROM accounts LIMIT 1")
    fun getAccountFlow(): Flow<AccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts")
    suspend fun clearAllAccounts()

    @Query("SELECT lastUpdated FROM accounts WHERE id = :accountId")
    suspend fun getLastUpdated(accountId: String): Long?
}