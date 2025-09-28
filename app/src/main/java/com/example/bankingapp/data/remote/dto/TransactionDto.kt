package com.example.bankingapp.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//@JsonClass(generateAdapter = true)
data class TransactionDto(
    @Json(name = "id") val id: String,
    @Json(name = "accountId") val accountId: String,
    @Json(name = "amount") val amount: MoneyAmountDto,
    @Json(name = "type") val type: String,
    @Json(name = "status") val status: String,
    @Json(name = "description") val description: String,
    @Json(name = "recipientName") val recipientName: String?,
    @Json(name = "recipientAccount") val recipientAccount: String?,
    @Json(name = "reference") val reference: String?,
    @Json(name = "date") val date: String,
    @Json(name = "balanceAfter") val balanceAfter: MoneyAmountDto?
)