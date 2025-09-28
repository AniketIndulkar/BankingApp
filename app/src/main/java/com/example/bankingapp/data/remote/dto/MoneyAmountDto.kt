package com.example.bankingapp.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//@JsonClass(generateAdapter = true)
data class MoneyAmountDto(
    @Json(name = "amount") val amount: String,
    @Json(name = "currency") val currency: String
)