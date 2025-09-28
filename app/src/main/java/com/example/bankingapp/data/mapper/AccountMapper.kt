package com.example.bankingapp.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.bankingapp.data.local.entity.AccountEntity
import com.example.bankingapp.data.remote.dto.AccountDto
import com.example.bankingapp.domain.model.AccountType
import com.example.bankingapp.domain.model.BankAccount
import com.example.bankingapp.domain.model.Currency
import com.example.bankingapp.domain.model.MoneyAmount
import com.example.bankingapp.security.EncryptedData
import com.example.bankingapp.security.EncryptionManager
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class AccountMapper(private val encryptionManager: EncryptionManager) {

    /**
     * Convert DTO to Domain model
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun dtoToDomain(dto: AccountDto): BankAccount {
        return BankAccount(
            id = dto.id,
            accountNumber = dto.accountNumber,
            accountType = AccountType.valueOf(dto.accountType),
            balance = mapMoneyAmount(dto.balance),
            currency = Currency.valueOf(dto.currency),
            isActive = dto.isActive,
            lastUpdated = parseDateTime(dto.lastUpdated),
            createdDate = parseDateTime(dto.createdDate)
        )
    }

    /**
     * Convert Domain model to Entity (with encryption)
     */
    fun domainToEntity(domain: BankAccount): AccountEntity {
        return AccountEntity(
            id = domain.id,
            accountNumber = encryptionManager.encrypt(domain.accountNumber).let {
                "${it.encryptedData.joinToString(",")};${it.iv.joinToString(",")}"
            },
            accountType = domain.accountType.name,
            balanceAmount = encryptionManager.encrypt(domain.balance.amount.toString()).let {
                "${it.encryptedData.joinToString(",")};${it.iv.joinToString(",")}"
            },
            currency = domain.currency.name,
            isActive = domain.isActive,
            lastUpdated = domain.lastUpdated.toEpochSecond(ZoneOffset.UTC),
            createdDate = domain.createdDate.toEpochSecond(ZoneOffset.UTC)
        )
    }

    /**
     * Convert Entity to Domain model (with decryption)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun entityToDomain(entity: AccountEntity): BankAccount {
        return BankAccount(
            id = entity.id,
            accountNumber = decryptString(entity.accountNumber),
            accountType = AccountType.valueOf(entity.accountType),
            balance = MoneyAmount(
                amount = BigDecimal(decryptString(entity.balanceAmount)),
                currency = Currency.valueOf(entity.currency)
            ),
            currency = Currency.valueOf(entity.currency),
            isActive = entity.isActive,
            lastUpdated = LocalDateTime.ofEpochSecond(entity.lastUpdated, 0, ZoneOffset.UTC),
            createdDate = LocalDateTime.ofEpochSecond(entity.createdDate, 0, ZoneOffset.UTC)
        )
    }

    private fun decryptString(encryptedString: String): String {
        val parts = encryptedString.split(";")
        val encryptedData = parts[0].split(",").map { it.toByte() }.toByteArray()
        val iv = parts[1].split(",").map { it.toByte() }.toByteArray()
        return encryptionManager.decrypt(EncryptedData(encryptedData, iv))
    }
}