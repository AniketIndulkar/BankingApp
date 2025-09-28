package com.example.bankingapp.domain.model

import java.math.BigDecimal

data class MoneyAmount(
    val amount: BigDecimal,
    val currency: Currency
) {
    operator fun plus(other: MoneyAmount): MoneyAmount {
        require(currency == other.currency) { "Cannot add different currencies" }
        return copy(amount = amount + other.amount)
    }

    operator fun minus(other: MoneyAmount): MoneyAmount {
        require(currency == other.currency) { "Cannot subtract different currencies" }
        return copy(amount = amount - other.amount)
    }

    fun isPositive(): Boolean = amount > BigDecimal.ZERO
    fun isNegative(): Boolean = amount < BigDecimal.ZERO
    fun isZero(): Boolean = amount == BigDecimal.ZERO

    companion object {
        fun zero(currency: Currency) = MoneyAmount(BigDecimal.ZERO, currency)
    }
}