package com.example.bankingapp.presentation.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bankingapp.domain.model.CardBrand
import com.example.bankingapp.domain.model.PaymentCard
import com.example.bankingapp.presentation.viewmodel.CardUiState
import com.example.bankingapp.presentation.viewmodel.CardViewModel
import com.example.bankingapp.security.BiometricAuthenticationScreen
import com.example.bankingapp.security.SecurityManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardContentScreen(
    uiState: CardUiState,
    onToggleCard: (String, Boolean) -> Unit,
    onRefresh: () -> Unit,
    onResetAuth: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection Status
        if (uiState.showOfflineIndicator) {
            item {
                OfflineIndicatorCard(uiState.connectionStatusText)
            }
        }

        // Security Info Card
        item {
            SecurityInfoCard(onResetAuth = onResetAuth)
        }

        // Error State
        uiState.error?.let { error ->
            item {
                ErrorCard(error)
            }
        }

        // Loading State
        if (uiState.isLoading && uiState.cards.isEmpty()) {
            item {
                LoadingCard()
            }
        }

        // Cards Section
        if (uiState.showCards) {
            // Active Cards
            if (uiState.activeCards.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Cards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.activeCards) { card ->
                    PaymentCardItem(
                        card = card,
                        onToggleCard = onToggleCard
                    )
                }
            }

            // Inactive Cards
            if (uiState.inactiveCards.isNotEmpty()) {
                item {
                    Text(
                        text = "Inactive Cards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(uiState.inactiveCards) { card ->
                    PaymentCardItem(
                        card = card,
                        onToggleCard = onToggleCard
                    )
                }
            }

            // Empty State
            if (uiState.isEmpty) {
                item {
                    EmptyCardsCard()
                }
            }
        }
    }
}

@Composable
private fun OfflineIndicatorCard(connectionStatusText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = connectionStatusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SecurityInfoCard(onResetAuth: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Secure",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Secure Access",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Your card information is protected with biometric authentication",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            TextButton(onClick = onResetAuth) {
                Text("Lock", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading your cards...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PaymentCardItem(
    card: PaymentCard,
    onToggleCard: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = card.brand.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (card.isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = card.cardType.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (card.isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                CardBrandIcon(card.brand)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card Number
            Text(
                text = card.maskedNumber,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = if (card.isActive)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = MaterialTheme.typography.headlineSmall.letterSpacing
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Card Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CARD HOLDER",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (card.isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "**** ****", // Masked for security
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (card.isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column {
                    Text(
                        text = "EXPIRES",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (card.isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${String.format("%02d", card.expiryMonth)}/${card.expiryYear}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (card.isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card Status and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardStatusChip(
                    isActive = card.isActive,
                    isBlocked = card.isBlocked
                )

                Row {
                    if (card.isBlocked) {
                        TextButton(
                            onClick = { /* Handle unblock */ },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Unblock")
                        }
                    } else {
                        TextButton(
                            onClick = { onToggleCard(card.id, !card.isActive) }
                        ) {
                            Text(if (card.isActive) "Disable" else "Enable")
                        }
                    }
                }
            }

            // Card Limits
            Spacer(modifier = Modifier.height(12.dp))
            CardLimitsSection(card)
        }
    }
}

@Composable
private fun CardBrandIcon(brand: CardBrand) {
    val (icon, color) = when (brand) {
        CardBrand.VISA -> Icons.Default.CreditCard to Color(0xFF1A1F71)
        CardBrand.MASTERCARD -> Icons.Default.CreditCard to Color(0xFFEB001B)
        CardBrand.AMERICAN_EXPRESS -> Icons.Default.CreditCard to Color(0xFF006FCF)
        CardBrand.DISCOVER -> Icons.Default.CreditCard to Color(0xFFFF6000)
        else -> Icons.Default.CreditCard to MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.size(40.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = brand.name,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CardStatusChip(
    isActive: Boolean,
    isBlocked: Boolean
) {
    val (color, text) = when {
        isBlocked -> MaterialTheme.colorScheme.error to "BLOCKED"
        isActive -> Color.Green to "ACTIVE"
        else -> MaterialTheme.colorScheme.outline to "INACTIVE"
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CardLimitsSection(card: PaymentCard) {
    Column {
        Text(
            text = "Spending Limits",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LimitItem(
                label = "Daily",
                amount = "${card.dailyLimit.currency.symbol}${card.dailyLimit.amount}"
            )
            LimitItem(
                label = "Monthly",
                amount = "${card.monthlyLimit.currency.symbol}${card.monthlyLimit.amount}"
            )
        }

        card.lastUsed?.let { lastUsed ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Last used: ${formatDate(lastUsed.toString())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LimitItem(label: String, amount: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyCardsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = "No cards",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Payment Cards",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No payment cards are associated with your account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        // Simple formatting - in real app you'd parse the actual LocalDateTime
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date())
    } catch (e: Exception) {
        dateString.take(10)
    }
}
@Composable
fun AccountBalanceScreen(
    viewModel: CardViewModel,
    securityManager: SecurityManager,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Payment Cards") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (uiState.isBiometricAuthenticated) {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }
        )

        // Show biometric authentication screen if required
        if (uiState.requiresBiometric && !uiState.isBiometricAuthenticated) {
            BiometricAuthenticationScreen(
                title = "Card Access",
                subtitle = "Authenticate to view your payment cards",
                onAuthenticationSuccess = {
                    viewModel.onBiometricAuthenticationSuccess()
                },
                onAuthenticationFailed = { error ->
                    viewModel.onBiometricAuthenticationFailed()
                },
                onCancel = onNavigateBack,
                securityManager = securityManager
            )
        } else {
            // Show card content after authentication
            CardContentScreen(
                uiState = uiState,
                onToggleCard = viewModel::toggleCard,
                onRefresh = { viewModel.refresh() },
                onResetAuth = { viewModel.resetAuthentication() }
            )
        }
    }
}

