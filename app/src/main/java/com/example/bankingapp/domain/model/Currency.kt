package com.example.bankingapp.domain.model

enum class Currency(val code: String, val symbol: String) {
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£"),
    JPY("JPY", "¥"),
    CAD("CAD", "C$"),
    AUD("AUD", "A$")
}