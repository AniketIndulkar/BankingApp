package com.example.bankingapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bankingapp.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM cards WHERE accountId = :accountId ORDER BY createdDate DESC")
    suspend fun getCardsByAccount(accountId: String): List<CardEntity>

    @Query("SELECT * FROM cards WHERE accountId = :accountId ORDER BY createdDate DESC")
    fun getCardsByAccountFlow(accountId: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :cardId")
    suspend fun getCardById(cardId: String): CardEntity?

    @Query("SELECT * FROM cards WHERE maskedNumber = :maskedNumber")
    suspend fun getCardByMaskedNumber(maskedNumber: String): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Query("DELETE FROM cards WHERE accountId = :accountId")
    suspend fun clearCards(accountId: String)

    @Query("DELETE FROM cards")
    suspend fun clearAllCards()

    @Query("SELECT COUNT(*) FROM cards WHERE accountId = :accountId")
    suspend fun getCardCount(accountId: String): Int

    @Query("UPDATE cards SET isActive = :isActive WHERE id = :cardId")
    suspend fun updateCardStatus(cardId: String, isActive: Boolean)
}