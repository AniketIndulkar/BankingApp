package com.example.bankingapp.presentation.navigation

//import com.example.bankingapp.presentation.screens.CardContentScreen


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bankingapp.presentation.screens.AccountDetailsScreen
import com.example.bankingapp.presentation.screens.HomeScreen
import com.example.bankingapp.presentation.viewmodel.CardViewModel
import com.example.bankingapp.security.SecurityManager
import org.koin.androidx.compose.koinViewModel
import com.example.bankingapp.presentation.screens.AccountBalanceScreen as ExistingAccountBalanceScreen
import com.example.bankingapp.presentation.screens.CardDetailsScreen as ExistingCardDetailsScreen
import com.example.bankingapp.presentation.screens.SettingsScreen as ExistingSettingsScreen
import com.example.bankingapp.presentation.screens.TransactionHistoryScreen as ExistingTransactionHistoryScreen

/**
 * Main navigation destinations
 */
object BankingDestinations {
    const val HOME = "home"
    const val ACCOUNT_BALANCE = "account_balance"
    const val ACCOUNT_DETAILS = "account_details"
    const val TRANSACTION_HISTORY = "transaction_history"
    const val CARD_DETAILS = "card_details"
    const val SETTINGS = "settings"
}

/**
 * Main navigation host - Updated to work with your existing screens
 */
@Composable
fun BankingNavHost(
    navController: NavHostController,
    securityManager: SecurityManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BankingDestinations.HOME,
        modifier = modifier
    ) {
        composable(BankingDestinations.HOME) {
            HomeScreen(
                onNavigateToBalance = {
                    navController.navigate(BankingDestinations.ACCOUNT_BALANCE)
                },
                onNavigateToTransactions = {
                    navController.navigate(BankingDestinations.TRANSACTION_HISTORY)
                },
                onNavigateToCards = {
                    navController.navigate(BankingDestinations.CARD_DETAILS)
                },
                onNavigateToAccountDetails = {
                    navController.navigate(BankingDestinations.ACCOUNT_DETAILS)
                }
            )
        }

        composable(BankingDestinations.ACCOUNT_BALANCE) {
            // This uses your CardViewModel but displays it as account balance
            // You can update this to use AccountViewModel when you have the proper screen
            val cardViewModel: CardViewModel = koinViewModel()
            ExistingAccountBalanceScreen(
                viewModel = cardViewModel,
                securityManager = securityManager,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(BankingDestinations.TRANSACTION_HISTORY) {
            ExistingTransactionHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(BankingDestinations.CARD_DETAILS) {
            ExistingCardDetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(BankingDestinations.ACCOUNT_DETAILS) {
            AccountDetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(BankingDestinations.SETTINGS) {
            ExistingSettingsScreen(
                securityManager = securityManager,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}