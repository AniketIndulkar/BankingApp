package com.example.bankingapp

import android.app.Application
import com.bankingapp.secure.BuildConfig
import com.example.bankingapp.di.appModule
import com.example.bankingapp.di.dataModule
import com.example.bankingapp.di.domainModule
import com.example.bankingapp.di.networkModule
import com.example.bankingapp.di.presentationModule
import com.example.bankingapp.di.securityModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BankingApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@BankingApplication)
//            workManagerFactory()
            modules(
                appModule,
                networkModule,
                dataModule,
                domainModule,
                securityModule,
                presentationModule
            )
        }
    }
}