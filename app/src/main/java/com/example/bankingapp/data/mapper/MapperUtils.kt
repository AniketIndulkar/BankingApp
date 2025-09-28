package com.example.bankingapp.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.bankingapp.data.remote.dto.MoneyAmountDto
import com.example.bankingapp.domain.model.Currency
import com.example.bankingapp.domain.model.MoneyAmount
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun mapMoneyAmount(dto: MoneyAmountDto): MoneyAmount {
    return MoneyAmount(
        amount = BigDecimal(dto.amount),
        currency = Currency.valueOf(dto.currency)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun parseDateTime(dateString: String): LocalDateTime {
    return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
}