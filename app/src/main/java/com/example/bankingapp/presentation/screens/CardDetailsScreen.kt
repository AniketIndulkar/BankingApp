package com.example.bankingapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Mock Card Data
data class CardItem(
    val id: String,
    val maskedNumber: String,
    val holderName: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cardType: String,
    val brand: String,
    val isActive: Boolean,
    val isBlocked: Boolean,
    val dailyLimit: String,
    val monthlyLimit: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailsScreen(
    onNavigateBack: () -> Unit
) {
    var showBiometricPrompt by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Mock cards data
    val cards = remember {
        listOf(
            CardItem(
                id = "card_001",
                maskedNumber = "**** **** **** 9012",
                holderName = "JOHN DOE",
                expiryMonth = 12,
                expiryYear = 2027,
                cardType = "DEBIT",
                brand = "VISA",
                isActive = true,
                isBlocked = false,
                dailyLimit = "1,000.00",
                monthlyLimit = "5,000.00"
            ),
            CardItem(
                id = "card_002",
                maskedNumber = "**** **** **** 2222",
                holderName = "JOHN DOE",
                expiryMonth = 8,
                expiryYear = 2026,
                cardType = "CREDIT",
                brand = "MASTERCARD",
                isActive = true,
                isBlocked = false,
                dailyLimit = "2,000.00",
                monthlyLimit = "10,000.00"
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("My Cards") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        isLoading = true
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(2000)
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
            }
        )

        if (showBiometricPrompt && !isAuthenticated) {
            BiometricPromptScreen(
                onAuthenticate = {
                    // Simulate biometric authentication
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(1000)
                        isAuthenticated = true
                        showBiometricPrompt = false
                    }
                },
                onCancel = onNavigateBack
            )
        } else {
            // Show card details after authentication
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SecurityNoticeCard()
                }

                items(cards) { card ->
                    CardDetailCard(
                        card = card,
                        onToggleCard = { cardId, isActive ->
                            // TODO: Toggle card status
                        }
                    )
                }

                item {
                    AddCardButton(
                        onClick = { /* TODO: Add new card */ }
                    )
                }
            }
        }
    }
}

@Composable
fun BiometricPromptScreen(
    onAuthenticate: () -> Unit,
    onCancel: () -> Unit
) {
    var isAuthenticating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Biometric Authentication",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Secure Access Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Use your biometric credential to view your card details",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isAuthenticating) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        isAuthenticating = true
                        onAuthenticate()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Authenticate")
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun SecurityNoticeCard() {
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
                contentDescription = "Security",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Secure Access Granted",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Your card details are protected with biometric authentication",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun CardDetailCard(
    card: CardItem,
    onToggleCard: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Card Visual
            CreditCardVisual(card = card)

            Spacer(modifier = Modifier.height(16.dp))

            // Card Info
            CardInfoSection(card = card)

            Spacer(modifier = Modifier.height(16.dp))

            // Card Actions
            CardActionsSection(
                card = card,
                onToggleCard = onToggleCard
            )
        }
    }
}

@Composable
fun CreditCardVisual(card: CardItem) {
    val cardGradient = when (card.brand.uppercase()) {
        "VISA" -> Brush.linearGradient(
            colors = listOf(Color(0xFF1A1F71), Color(0xFF4338CA))
        )
        "MASTERCARD" -> Brush.linearGradient(
            colors = listOf(Color(0xFFEB001B), Color(0xFFF79E1B))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color(0xFF374151), Color(0xFF6B7280))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Card Brand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = card.brand,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Card",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Card Number
            Text(
                text = card.maskedNumber,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )

            // Card Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CARDHOLDER",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = card.holderName,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = "EXPIRES",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${card.expiryMonth.toString().padStart(2, '0')}/${card.expiryYear.toString().takeLast(2)}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CardInfoSection(card: CardItem) {
    Column {
        Text(
            text = "Card Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        InfoRow("Card Type", card.cardType)
        InfoRow("Status", if (card.isActive) "Active" else "Inactive")
        InfoRow("Daily Limit", "$${card.dailyLimit}")
        InfoRow("Monthly Limit", "$${card.monthlyLimit}")
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CardActionsSection(
    card: CardItem,
    onToggleCard: (String, Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { onToggleCard(card.id, !card.isActive) },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (card.isActive) Icons.Default.Block else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (card.isActive) "Disable" else "Enable")
        }

        OutlinedButton(
            onClick = { /* TODO: Card settings */ },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Settings")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Card",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add New Card",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}