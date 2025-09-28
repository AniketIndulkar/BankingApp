package com.example.bankingapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.bankingapp.data.cache.CacheManager
import com.example.bankingapp.data.local.dao.AccountDao
import com.example.bankingapp.data.local.entity.CacheDataTypes
import com.example.bankingapp.data.mapper.AccountMapper
import com.example.bankingapp.data.remote.BankingApiService
import com.example.bankingapp.domain.model.AppError
import com.example.bankingapp.domain.model.BankAccount
import com.example.bankingapp.domain.repository.AccountRepository
import com.example.bankingapp.security.EncryptionManager
import com.example.bankingapp.utils.NetworkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.bankingapp.domain.model.Result

class AccountRepositoryImpl(
    private val apiService: BankingApiService,
    private val accountDao: AccountDao,
    private val encryptionManager: EncryptionManager,
    private val networkManager: NetworkManager,
    private val cacheManager: CacheManager
) : AccountRepository {

    private val accountMapper = AccountMapper(encryptionManager)

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getAccountBalance(forceRefresh: Boolean): Flow<Result<BankAccount>> = flow {
        try {
            // 1. Check cache validity first
            val isCacheExpired = cacheManager.isCacheExpired(CacheDataTypes.ACCOUNT)
            val cachedAccount = getCachedAccount()

            // 2. Determine if we should fetch from network
            val shouldFetchFromNetwork = forceRefresh ||
                    isCacheExpired ||
                    cachedAccount == null ||
                    (networkManager.isConnected() && !forceRefresh && isCacheExpired)

            // 3. If we have valid cache and don't need to refresh, return it immediately
            if (cachedAccount != null && !isCacheExpired && !forceRefresh) {
                emit(Result.Success(cachedAccount))
                return@flow
            }

            // 4. If we have cache (even if expired), emit it first for immediate UI update
            cachedAccount?.let {
                emit(Result.Success(it))
            } ?: run {
                emit(Result.Loading)
            }

            // 5. Fetch from network if conditions are met
            if (shouldFetchFromNetwork && networkManager.isConnected()) {
                when (val refreshResult = refreshAccountData()) {
                    is Result.Success -> {
                        emit(Result.Success(refreshResult.data))
                    }
                    is Result.Error -> {
                        // If we had cached data, we already emitted it
                        // Only emit error if we have no cached fallback
                        if (cachedAccount == null) {
                            emit(refreshResult)
                        }
                    }
                    is Result.Loading -> { /* Already handling loading state */ }
                }
            } else if (!networkManager.isConnected() && cachedAccount == null) {
                // No cache and no network
                emit(Result.Error(AppError.NetworkError("No internet connection and no cached data available")))
            }

        } catch (e: Exception) {
            emit(Result.Error(AppError.UnknownError(e.message ?: "Unknown error")))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getCachedAccount(): BankAccount? {
        return try {
            accountDao.getAccount()?.let { accountMapper.entityToDomain(it) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun refreshAccountData(): Result<BankAccount> {
        return try {
            if (!networkManager.isConnected()) {
                return Result.Error(AppError.NetworkError("No internet connection"))
            }

            val response = apiService.getAccountBalance()
            if (response.isSuccessful && response.body() != null) {
                val accountDto = response.body()!!
                val account = accountMapper.dtoToDomain(accountDto)

                // Cache the account data
                accountDao.insertAccount(accountMapper.domainToEntity(account))

                // Update cache metadata
                cacheManager.updateCacheMetadata(CacheDataTypes.ACCOUNT, 1)

                Result.Success(account)
            } else {
                Result.Error(AppError.NetworkError("Failed to fetch account data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(AppError.NetworkError(e.message ?: "Network error"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getAccountDetails(): Flow<Result<BankAccount>> =
        getAccountBalance(forceRefresh = false)
}