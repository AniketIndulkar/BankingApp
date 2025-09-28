package com.example.bankingapp.data.mapper

import com.example.bankingapp.data.local.entity.TransactionEntity
import com.example.bankingapp.data.remote.dto.TransactionDto
import com.example.bankingapp.domain.model.Currency
import com.example.bankingapp.domain.model.MoneyAmount
import com.example.bankingapp.domain.model.Transaction
import com.example.bankingapp.domain.model.TransactionStatus
import com.example.bankingapp.domain.model.TransactionType
import com.example.bankingapp.security.EncryptedData
import com.example.bankingapp.security.EncryptionManager
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset

class TransactionMapper(private val encryptionManager: EncryptionManager) {

    fun dtoToDomain(dto: TransactionDto): Transaction {
        return Transaction(
            id = dto.id,
            accountId = dto.accountId,
            amount = mapMoneyAmount(dto.amount),
            type = TransactionType.valueOf(dto.type),
            status = TransactionStatus.valueOf(dto.status),
            description = dto.description,
            recipientName = dto.recipientName,
            recipientAccount = dto.recipientAccount,
            reference = dto.reference,
            date = parseDateTime(dto.date),
            balanceAfter = dto.balanceAfter?.let { mapMoneyAmount(it) }
        )
    }

    fun domainToEntity(domain: Transaction): TransactionEntity {
        return TransactionEntity(
            id = domain.id,
            accountId = domain.accountId,
            amount = encryptString(domain.amount.amount.toString()),
            currency = domain.amount.currency.name,
            type = domain.type.name,
            status = domain.status.name,
            description = encryptString(domain.description),
            recipientName = domain.recipientName?.let { encryptString(it) },
            recipientAccount = domain.recipientAccount?.let { encryptString(it) },
            reference = domain.reference,
            date = domain.date.toEpochSecond(ZoneOffset.UTC),
            balanceAfter = domain.balanceAfter?.amount?.toString()?.let { encryptString(it) },
            balanceAfterCurrency = domain.balanceAfter?.currency?.name
        )
    }

    fun entityToDomain(entity: TransactionEntity): Transaction {
        return Transaction(
            id = entity.id,
            accountId = entity.accountId,
            amount = MoneyAmount(
                amount = BigDecimal(decryptString(entity.amount)),
                currency = Currency.valueOf(entity.currency)
            ),
            type = TransactionType.valueOf(entity.type),
            status = TransactionStatus.valueOf(entity.status),
            description = decryptString(entity.description),
            recipientName = entity.recipientName?.let { decryptString(it) },
            recipientAccount = entity.recipientAccount?.let { decryptString(it) },
            reference = entity.reference,
            date = LocalDateTime.ofEpochSecond(entity.date, 0, ZoneOffset.UTC),
            balanceAfter = if (entity.balanceAfter != null && entity.balanceAfterCurrency != null) {
                MoneyAmount(
                    amount = BigDecimal(decryptString(entity.balanceAfter)),
                    currency = Currency.valueOf(entity.balanceAfterCurrency)
                )
            } else null
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
