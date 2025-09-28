package com.example.bankingapp.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.bankingapp.data.local.entity.CardEntity
import com.example.bankingapp.data.remote.dto.CardDto
import com.example.bankingapp.domain.model.CardBrand
import com.example.bankingapp.domain.model.CardType
import com.example.bankingapp.domain.model.Currency
import com.example.bankingapp.domain.model.MoneyAmount
import com.example.bankingapp.domain.model.PaymentCard
import com.example.bankingapp.security.EncryptedData
import com.example.bankingapp.security.EncryptionManager
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset

class CardMapper(private val encryptionManager: EncryptionManager) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun dtoToDomain(dto: CardDto): PaymentCard {
        return PaymentCard(
            id = dto.id,
            accountId = dto.accountId,
            cardNumber = dto.cardNumber,
            maskedNumber = dto.maskedNumber,
            holderName = dto.holderName,
            expiryMonth = dto.expiryMonth,
            expiryYear = dto.expiryYear,
            cvv = dto.cvv,
            cardType = CardType.valueOf(dto.cardType),
            brand = CardBrand.valueOf(dto.brand),
            isActive = dto.isActive,
            isBlocked = dto.isBlocked,
            dailyLimit = mapMoneyAmount(dto.dailyLimit),
            monthlyLimit = mapMoneyAmount(dto.monthlyLimit),
            lastUsed = dto.lastUsed?.let { parseDateTime(it) },
            createdDate = parseDateTime(dto.createdDate)
        )
    }

    fun domainToEntity(domain: PaymentCard): CardEntity {
        return CardEntity(
            id = domain.id,
            accountId = domain.accountId,
            cardNumber = encryptString(domain.cardNumber),
            maskedNumber = domain.maskedNumber, // Safe to store unencrypted
            holderName = encryptString(domain.holderName),
            expiryMonth = domain.expiryMonth,
            expiryYear = domain.expiryYear,
            cvv = encryptString(domain.cvv),
            cardType = domain.cardType.name,
            brand = domain.brand.name,
            isActive = domain.isActive,
            isBlocked = domain.isBlocked,
            dailyLimitAmount = encryptString(domain.dailyLimit.amount.toString()),
            dailyLimitCurrency = domain.dailyLimit.currency.name,
            monthlyLimitAmount = encryptString(domain.monthlyLimit.amount.toString()),
            monthlyLimitCurrency = domain.monthlyLimit.currency.name,
            lastUsed = domain.lastUsed?.toEpochSecond(ZoneOffset.UTC),
            createdDate = domain.createdDate.toEpochSecond(ZoneOffset.UTC)
        )
    }

    fun entityToDomain(entity: CardEntity): PaymentCard {
        return PaymentCard(
            id = entity.id,
            accountId = entity.accountId,
            cardNumber = decryptString(entity.cardNumber),
            maskedNumber = entity.maskedNumber,
            holderName = decryptString(entity.holderName),
            expiryMonth = entity.expiryMonth,
            expiryYear = entity.expiryYear,
            cvv = decryptString(entity.cvv),
            cardType = CardType.valueOf(entity.cardType),
            brand = CardBrand.valueOf(entity.brand),
            isActive = entity.isActive,
            isBlocked = entity.isBlocked,
            dailyLimit = MoneyAmount(
                amount = BigDecimal(decryptString(entity.dailyLimitAmount)),
                currency = Currency.valueOf(entity.dailyLimitCurrency)
            ),
            monthlyLimit = MoneyAmount(
                amount = BigDecimal(decryptString(entity.monthlyLimitAmount)),
                currency = Currency.valueOf(entity.monthlyLimitCurrency)
            ),
            lastUsed = entity.lastUsed?.let {
                LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
            },
            createdDate = LocalDateTime.ofEpochSecond(entity.createdDate, 0, ZoneOffset.UTC)
        )
    }

    private fun encryptString(value: String): String {
        val encrypted = encryptionManager.encrypt(value)
        return "${encrypted.encryptedData.joinToString(",")};${encrypted.iv.joinToString(",")}"
    }

    private fun decryptString(encryptedString: String): String {
        val parts = encryptedString.split(";")
        val encryptedData = parts[0].split(",").map { it.toByte() }.toByteArray()
        val iv = parts[1].split(",").map { it.toByte() }.toByteArray()
        return encryptionManager.decrypt(EncryptedData(encryptedData, iv))
    }
}
