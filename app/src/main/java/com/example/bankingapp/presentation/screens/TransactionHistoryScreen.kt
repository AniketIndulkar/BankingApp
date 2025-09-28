package com.example.bankingapp.presentation.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Mock Transaction Data
data class TransactionItem(
    val id: String,
    val description: String,
    val amount: String,
    val type: TransactionType,
    val date: Long,
    val status: String,
    val recipientName: String? = null
)

enum class TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, ATM_WITHDRAWAL, ONLINE_PURCHASE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onNavigateBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }

    // Mock data
    val transactions = remember {
        listOf(
            TransactionItem(
                id = "txn_001",
                description = "Amazon Purchase",
                amount = "-85.00",
                type = TransactionType.ONLINE_PURCHASE,
                date = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000,
                status = "COMPLETED",
                recipientName = "Amazon.com"
            ),
            TransactionItem(
                id = "txn_002",
                description = "ATM Withdrawal",
                amount = "-45.50",
                type = TransactionType.ATM_WITHDRAWAL,
                date = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000,
                status = "COMPLETED"
            ),
            TransactionItem(
                id = "txn_003",
                description = "Salary Deposit",
                amount = "1200.00",
                type = TransactionType.DEPOSIT,
                date = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000,
                status = "COMPLETED"
            ),
            TransactionItem(
                id = "txn_004",
                description = "Netflix Subscription",
                amount = "-25.99",
                type = TransactionType.PAYMENT,
                date = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000,
                status = "COMPLETED",
                recipientName = "Netflix Inc."
            ),
            TransactionItem(
                id = "txn_005",
                description = "Grocery Shopping",
                amount = "-156.78",
                type = TransactionType.ONLINE_PURCHASE,
                date = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000,
                status = "COMPLETED",
                recipientName = "Whole Foods Market"
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Transaction History") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        isLoading = true
                        GlobalScope.launch {
                            delay(2000)
                            isLoading = false
                        }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }

                IconButton(onClick = { /* TODO: Filter */ }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // Connection Status
//                ConnectionStatusCard(
//                    isOffline = isOffline,
//                    statusText = ""
//                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Summary Card
                TransactionSummaryCard(transactions = transactions)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(transactions) { transaction ->
                TransactionCard(transaction = transaction)
            }

            item {
                // Load More Button
                if (!isLoading) {
                    TextButton(
                        onClick = { /* TODO: Load more */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Load More Transactions")
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionSummaryCard(transactions: List<TransactionItem>) {
    val totalSpent = transactions
        .filter { it.amount.startsWith("-") }
        .sumOf { it.amount.drop(1).toDoubleOrNull() ?: 0.0 }

    val totalReceived = transactions
        .filter { !it.amount.startsWith("-") }
        .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "This Month",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryColumn(
                    title = "Spent",
                    amount = "-${String.format("%.2f", totalSpent)}",
                    color = MaterialTheme.colorScheme.error
                )

                SummaryColumn(
                    title = "Received",
                    amount = "+${String.format("%.2f", totalReceived)}",
                    color = MaterialTheme.colorScheme.primary
                )

                SummaryColumn(
                    title = "Transactions",
                    amount = "${transactions.size}",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun SummaryColumn(
    title: String,
    amount: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(transaction: TransactionItem) {
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val isNegative = transaction.amount.startsWith("-")

    Card(
        onClick = { /* TODO: Show transaction details */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction Icon
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = getTransactionIconColor(transaction.type)
            ) {
                Icon(
                    imageVector = getTransactionIcon(transaction.type),
                    contentDescription = transaction.type.name,
                    modifier = Modifier.padding(8.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (transaction.recipientName != null) {
                    Text(
                        text = transaction.recipientName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = formatter.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (isNegative) transaction.amount else "+${transaction.amount}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "USD",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun getTransactionIcon(type: TransactionType): ImageVector {
    return when (type) {
        TransactionType.DEPOSIT -> Icons.Default.TrendingUp
        TransactionType.WITHDRAWAL -> Icons.Default.TrendingDown
        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
        TransactionType.PAYMENT -> Icons.Default.Payment
        TransactionType.ATM_WITHDRAWAL -> Icons.Default.LocalAtm
        TransactionType.ONLINE_PURCHASE -> Icons.Default.ShoppingCart
    }
}

@Composable
fun getTransactionIconColor(type: TransactionType): Color {
    return when (type) {
        TransactionType.DEPOSIT -> Color(0xFF4CAF50)
        TransactionType.WITHDRAWAL -> Color(0xFFF44336)
        TransactionType.TRANSFER -> Color(0xFF2196F3)
        TransactionType.PAYMENT -> Color(0xFF9C27B0)
        TransactionType.ATM_WITHDRAWAL -> Color(0xFFFF9800)
        TransactionType.ONLINE_PURCHASE -> Color(0xFF607D8B)
    }
}