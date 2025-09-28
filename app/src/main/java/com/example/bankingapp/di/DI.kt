package com.example.bankingapp.di

import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.work.WorkManager
import com.example.bankingapp.data.cache.CacheManager
import com.example.bankingapp.data.local.BankingDatabase
import com.example.bankingapp.data.local.dao.AccountDao
import com.example.bankingapp.data.local.dao.CacheMetadataDao
import com.example.bankingapp.data.local.dao.CardDao
import com.example.bankingapp.data.local.dao.TransactionDao
import com.example.bankingapp.data.mapper.AccountMapper
import com.example.bankingapp.data.mapper.CardMapper
import com.example.bankingapp.data.mapper.TransactionMapper
import com.example.bankingapp.data.remote.BankingApiService
import com.example.bankingapp.data.remote.MockInterceptor
import com.example.bankingapp.data.repository.AccountRepositoryImpl
import com.example.bankingapp.data.repository.CardRepositoryImpl
import com.example.bankingapp.data.repository.TransactionRepositoryImpl
import com.example.bankingapp.domain.repository.AccountRepository
import com.example.bankingapp.domain.repository.CardRepository
import com.example.bankingapp.domain.repository.TransactionRepository
import com.example.bankingapp.domain.usecase.GetAccountBalanceUseCase
import com.example.bankingapp.domain.usecase.GetAccountDetailsUseCase
import com.example.bankingapp.domain.usecase.GetCardDetailsUseCase
import com.example.bankingapp.domain.usecase.GetTransactionHistoryUseCase
import com.example.bankingapp.domain.usecase.RefreshDataUseCase
import com.example.bankingapp.presentation.viewmodel.SecurityAuditLogger
import com.example.bankingapp.presentation.viewmodel.SensitiveDataGuard
import com.example.bankingapp.security.BiometricAuthManager
import com.example.bankingapp.security.EncryptionManager
import com.example.bankingapp.security.SecurityManager
import com.example.bankingapp.security.SessionManager
import com.example.bankingapp.utils.NetworkManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.GlobalScope
import androidx.security.crypto.MasterKey
import com.bankingapp.secure.BuildConfig
import com.example.bankingapp.presentation.viewmodel.AccountViewModel
import com.example.bankingapp.presentation.viewmodel.CardViewModel
import com.example.bankingapp.presentation.viewmodel.TransactionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel

val appModule = module {
    single { WorkManager.getInstance(androidContext()) }
    single { NetworkManager(androidContext()) }
}

val networkModule = module {
    single<Moshi> {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(get<MockInterceptor>())
            .apply {
                if (BuildConfig.ENABLE_LOGGING) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single<MockInterceptor> { MockInterceptor() }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single<BankingApiService> { get<Retrofit>().create(BankingApiService::class.java) }
}

val dataModule = module {
    single<BankingDatabase> {
        Room.databaseBuilder(
            androidContext(),
            BankingDatabase::class.java,
            "banking_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single<AccountDao> { get<BankingDatabase>().accountDao() }
    single<TransactionDao> { get<BankingDatabase>().transactionDao() }
    single<CardDao> { get<BankingDatabase>().cardDao() }
    single<CacheMetadataDao> { get<BankingDatabase>().cacheMetadataDao() }

    // Data mappers
    single { AccountMapper(get()) }
    single { TransactionMapper(get()) }
    single { CardMapper(get()) }

    single<AccountRepository> {
        AccountRepositoryImpl(
            apiService = get(),
            accountDao = get(),
            encryptionManager = get(),
            networkManager = get(),
            cacheManager = get()
        )
    }

    single<TransactionRepository> {
        TransactionRepositoryImpl(
            apiService = get(),
            transactionDao = get(),
            encryptionManager = get(),
            networkManager = get()
        )
    }

    single<CardRepository> {
        CardRepositoryImpl(
            apiService = get(),
            cardDao = get(),
            encryptionManager = get(),
            networkManager = get()
        )
    }
}

val domainModule = module {
    factory { GetAccountBalanceUseCase(get()) }
    factory { GetAccountDetailsUseCase(get()) }
    factory { GetTransactionHistoryUseCase(get()) }
    factory { GetCardDetailsUseCase(get()) }
    factory { RefreshDataUseCase(get(), get(), get()) }
}

val presentationModule = module {
    viewModel { AccountViewModel(
        get(), get(), get(),
        networkManager = get()
    ) }
    viewModel { TransactionViewModel(
        get(),
        networkManager = get()
    ) }
    viewModel { CardViewModel(
        get(),
        networkManager = get()
    ) }
}

val securityModule = module {
    // Fix: Explicit type declaration for MasterKey
    single<MasterKey> {
        MasterKey.Builder(androidContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    // Fix: Explicit type declaration for EncryptedSharedPreferences
    single<EncryptedSharedPreferences> {
        EncryptedSharedPreferences.create(
            androidContext(),
            "banking_secure_prefs",
            get<MasterKey>(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    single { EncryptionManager(get()) }
    single { BiometricAuthManager(androidContext()) }

    // Enhanced security components
    single { SecurityManager(androidContext(), get(), get(), get()) }
    single { SessionManager(get(), GlobalScope) }
    single { SensitiveDataGuard(get(), get()) }
    single { SecurityAuditLogger() }

    // Cache manager
    single { CacheManager(get()) }
}