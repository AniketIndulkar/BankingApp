package com.example.bankingapp.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//@JsonClass(generateAdapter = true)
data class CardDto(
    @Json(name = "id") val id: String,
    @Json(name = "accountId") val accountId: String,
    @Json(name = "cardNumber") val cardNumber: String,
    @Json(name = "maskedNumber") val maskedNumber: String,
    @Json(name = "holderName") val holderName: String,
    @Json(name = "expiryMonth") val expiryMonth: Int,
    @Json(name = "expiryYear") val expiryYear: Int,
    @Json(name = "cvv") val cvv: String,
    @Json(name = "cardType") val cardType: String,
    @Json(name = "brand") val brand: String,
    @Json(name = "isActive") val isActive: Boolean,
    @Json(name = "isBlocked") val isBlocked: Boolean,
    @Json(name = "dailyLimit") val dailyLimit: MoneyAmountDto,
    @Json(name = "monthlyLimit") val monthlyLimit: MoneyAmountDto,
    @Json(name = "lastUsed") val lastUsed: String?,
    @Json(name = "createdDate") val createdDate: String
)