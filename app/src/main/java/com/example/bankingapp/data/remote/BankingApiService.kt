package com.example.bankingapp.data.remote

import com.example.bankingapp.data.remote.dto.AccountDto
import com.example.bankingapp.data.remote.dto.CardDto
import com.example.bankingapp.data.remote.dto.TransactionDto

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API service interface for banking operations
 */
interface BankingApiService {

    @GET("account/balance")
    suspend fun getAccountBalance(): Response<AccountDto>

    @GET("account/details")
    suspend fun getAccountDetails(): Response<AccountDto>

    @GET("transactions")
    suspend fun getTransactions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<List<TransactionDto>>

    @GET("transactions/{id}")
    suspend fun getTransactionById(@Path("id") transactionId: String): Response<TransactionDto>

    @GET("cards")
    suspend fun getCards(): Response<List<CardDto>>

    @GET("cards/{id}")
    suspend fun getCardById(@Path("id") cardId: String): Response<CardDto>

    @PUT("cards/{id}/toggle")
    suspend fun toggleCard(
        @Path("id") cardId: String,
        @Query("active") isActive: Boolean
    ): Response<CardDto>
}