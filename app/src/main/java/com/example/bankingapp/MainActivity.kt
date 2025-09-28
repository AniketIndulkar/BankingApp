package com.example.bankingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.example.bankingapp.presentation.navigation.BankingNavHost
import com.example.bankingapp.ui.theme.BankingAppTheme
import org.koin.compose.koinInject
import com.example.bankingapp.security.SecurityManager

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BankingAppTheme {
                BankingApp()
            }
        }
    }
}

@Composable
fun BankingApp() {
    val navController = rememberNavController()
    val securityManager: SecurityManager = koinInject()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        BankingNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            securityManager = securityManager
        )
    }
}