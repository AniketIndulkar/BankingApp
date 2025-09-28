package com.example.bankingapp.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//@JsonClass(generateAdapter = true)
data class AccountDto(
    @Json(name = "id") val id: String,
    @Json(name = "accountNumber") val accountNumber: String,
    @Json(name = "accountType") val accountType: String,
    @Json(name = "balance") val balance: MoneyAmountDto,
    @Json(name = "currency") val currency: String,
    @Json(name = "isActive") val isActive: Boolean,
    @Json(name = "lastUpdated") val lastUpdated: String,
    @Json(name = "createdDate") val createdDate: String
)