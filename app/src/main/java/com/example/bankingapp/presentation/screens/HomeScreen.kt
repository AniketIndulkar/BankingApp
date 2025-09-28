package com.example.bankingapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bankingapp.presentation.viewmodel.AccountViewModel
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBalance: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToAccountDetails: () -> Unit
) {
    // Get ViewModels and Network State
    val accountViewModel: AccountViewModel = koinViewModel()
    val accountUiState by accountViewModel.uiState.collectAsStateWithLifecycle()

    var showConnectivitySnackbar by remember { mutableStateOf(false) }

    // Watch for connectivity changes
    LaunchedEffect(accountUiState.isConnected) {
        showConnectivitySnackbar = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Global Network Status Bar
        NetworkStatusBar(isConnected = accountUiState.isConnected)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header with Connection Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Secure Banking",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ConnectionStatusIndicator(
                        isConnected = accountUiState.isConnected
                    )
                }
            }

            item {
                // Connection Status Card (shows when offline)
                ConnectionStatusCard(
                    isConnected = accountUiState.isConnected,
                    lastUpdated = accountUiState.lastUpdated
                )
            }

            item {
                // Quick Balance Card with Network-Aware Refresh
                QuickBalanceCard(
                    onClick = onNavigateToBalance,
                    account = accountUiState.account,
                    isConnected = accountUiState.isConnected,
                    isLoading = accountUiState.isLoading,
                    lastUpdated = accountUiState.lastUpdated,
                    onRefresh = { accountViewModel.refresh() }
                )
            }

            item {
                Text(
                    text = "Features",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                // Feature Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "Account Balance",
                        description = "View your current balance",
                        icon = Icons.Default.AccountBalance,
                        onClick = onNavigateToBalance,
                        modifier = Modifier.weight(1f)
                    )

                    FeatureCard(
                        title = "Transactions",
                        description = "View transaction history",
                        icon = Icons.Default.Receipt,
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "Cards",
                        description = "Manage your cards",
                        icon = Icons.Default.CreditCard,
                        onClick = onNavigateToCards,
                        modifier = Modifier.weight(1f)
                    )

                    FeatureCard(
                        title = "Account Details",
                        description = "View account info",
                        icon = Icons.Default.Person,
                        onClick = onNavigateToAccountDetails,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Offline Features Section
                OfflineFeaturesCard(isConnected = accountUiState.isConnected)
            }
        }
    }

    // Connectivity Change Snackbar
    if (showConnectivitySnackbar) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000) // Auto dismiss after 3 seconds
            showConnectivitySnackbar = false
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            ConnectivitySnackbar(
                isConnected = accountUiState.isConnected,
                onDismiss = { showConnectivitySnackbar = false },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickBalanceCard(
    onClick: () -> Unit,
    account: com.example.bankingapp.domain.model.BankAccount?,
    isConnected: Boolean,
    isLoading: Boolean,
    lastUpdated: Long?,
    onRefresh: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = account?.let {
                            "${it.currency.symbol}${it.balance.amount}"
                        } ?: "$2,547.83",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // Data Freshness Indicator
                    DataFreshnessIndicator(
                        lastUpdated = lastUpdated,
                        isConnected = isConnected,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    NetworkAwareRefreshButton(
                        isConnected = isConnected,
                        isLoading = isLoading,
                        onRefresh = onRefresh
                    )
                }
            }
        }
    }
}

@Composable
fun OfflineFeaturesCard(isConnected: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.OfflinePin else Icons.Default.CloudOff,
                    contentDescription = "Offline Features",
                    tint = if (isConnected)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) "Offline Features Available" else "Currently Using Offline Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isConnected)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val features = if (isConnected) {
                "• View cached account balance\n• Browse transaction history\n• Access card details (with biometric)\n• Share account information\n• All data auto-syncs when offline"
            } else {
                "• Viewing cached account balance\n• Browsing offline transaction history\n• Accessing saved card details\n• Limited functionality until connection restored"
            }

            Text(
                text = features,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isConnected)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Keep existing FeatureCard composable unchanged
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}