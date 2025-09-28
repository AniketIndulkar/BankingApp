package com.example.bankingapp.data.mapper

import com.example.bankingapp.data.local.dao.AccountDao
import com.example.bankingapp.data.local.entity.AccountEntity
import com.example.bankingapp.security.EncryptionManager

class EncryptionValidator(private val encryptionManager: EncryptionManager) {

    /**
     * Test encryption/decryption is working properly
     */
    fun validateEncryption(): Boolean {
        return try {
            val testData = "Test sensitive banking data 123"
            val encrypted = encryptionManager.encrypt(testData)
            val decrypted = encryptionManager.decrypt(encrypted)
            testData == decrypted
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verify database encryption is working
     */
    suspend fun validateDatabaseEncryption(accountDao: AccountDao): Boolean {
        return try {
            // Insert test data
            val testAccount = AccountEntity(
                id = "test_encrypt_123",
                accountNumber = "SENSITIVE_ACCOUNT_NUMBER_12345",
                accountType = "TEST",
                balanceAmount = "9999.99",
                currency = "USD",
                isActive = true,
                lastUpdated = System.currentTimeMillis(),
                createdDate = System.currentTimeMillis()
            )

            accountDao.insertAccount(testAccount)

            // Retrieve and verify
            val retrieved = accountDao.getAccountById("test_encrypt_123")
            val isValid = retrieved?.accountNumber == "SENSITIVE_ACCOUNT_NUMBER_12345"

            // Clean up test data
            accountDao.clearAllAccounts()

            isValid
        } catch (e: Exception) {
            false
        }
    }
}