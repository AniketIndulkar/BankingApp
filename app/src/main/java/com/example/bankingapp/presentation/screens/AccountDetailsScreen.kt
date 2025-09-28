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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Mock Account Data
data class AccountDetails(
    val accountNumber: String,
    val routingNumber: String,
    val accountType: String,
    val accountHolderName: String,
    val bankName: String,
    val branchName: String,
    val branchAddress: String,
    val swiftCode: String,
    val iban: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    onNavigateBack: () -> Unit
) {
    var showBiometricPrompt by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    // Mock account data
    val accountDetails = remember {
        AccountDetails(
            accountNumber = "1234567890",
            routingNumber = "021000021",
            accountType = "Checking Account",
            accountHolderName = "John Doe",
            bankName = "Secure Banking Corp",
            branchName = "Main Branch",
            branchAddress = "123 Banking St, Finance City, FC 12345",
            swiftCode = "SBCPUS33",
            iban = "US12 SBCP 0210 0002 1123 4567 890"
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Account Details") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        // Share account details
                        shareAccountDetails(accountDetails)
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        )

        if (showBiometricPrompt && !isAuthenticated) {
            BiometricPromptForAccount(
                onAuthenticate = {
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(1000)
                        isAuthenticated = true
                        showBiometricPrompt = false
                    }
                },
                onCancel = onNavigateBack
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SecurityBadge()
                }

                item {
                    AccountSummaryCard(accountDetails)
                }

                item {
                    BankingDetailsCard(
                        accountDetails = accountDetails,
                        onCopyToClipboard = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    )
                }

                item {
                    QuickShareCard(
                        onShareAccount = { shareAccountDetails(accountDetails) },
                        onShareRouting = { shareRoutingNumber(accountDetails) }
                    )
                }

                item {
                    SecurityInfoCard()
                }
            }
        }
    }
}

@Composable
fun BiometricPromptForAccount(
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
            imageVector = Icons.Default.AccountBalance,
            contentDescription = "Account Details",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Account Information Access",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Authenticate to view your complete account details and sharing options",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isAuthenticating) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Authenticating...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun SecurityBadge() {
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
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Secure Access Verified",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Your account details are protected and encrypted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun AccountSummaryCard(accountDetails: AccountDetails) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = accountDetails.accountHolderName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = accountDetails.accountType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "ACTIVE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = accountDetails.bankName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = accountDetails.branchName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BankingDetailsCard(
    accountDetails: AccountDetails,
    onCopyToClipboard: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Banking Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            BankingDetailRow(
                label = "Account Number",
                value = accountDetails.accountNumber,
                onCopy = { onCopyToClipboard(accountDetails.accountNumber) },
                isSensitive = true
            )

            BankingDetailRow(
                label = "Routing Number",
                value = accountDetails.routingNumber,
                onCopy = { onCopyToClipboard(accountDetails.routingNumber) }
            )

            BankingDetailRow(
                label = "SWIFT Code",
                value = accountDetails.swiftCode,
                onCopy = { onCopyToClipboard(accountDetails.swiftCode) }
            )

            BankingDetailRow(
                label = "IBAN",
                value = accountDetails.iban,
                onCopy = { onCopyToClipboard(accountDetails.iban) }
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Branch Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = accountDetails.branchAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankingDetailRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
    isSensitive: Boolean = false
) {
    var showSnackbar by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSensitive) FontWeight.Bold else FontWeight.Medium
            )
        }

        IconButton(
            onClick = {
                onCopy()
                showSnackbar = true
            }
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy $label",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showSnackbar) {
        LaunchedEffect(showSnackbar) {
            kotlinx.coroutines.delay(2000)
            showSnackbar = false
        }
    }
}

@Composable
fun QuickShareCard(
    onShareAccount: () -> Unit,
    onShareRouting: () -> Unit
) {
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
                text = "Quick Share",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Share your banking details securely with trusted contacts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onShareAccount,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Full Details")
                }

                OutlinedButton(
                    onClick = onShareRouting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Numbers,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Routing Only")
                }
            }
        }
    }
}

@Composable
fun SecurityInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Information",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Security & Privacy",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• Your account details are encrypted and protected\n" +
                        "• Biometric authentication required for access\n" +
                        "• Share only with trusted recipients\n" +
                        "• Never share your full account details publicly",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper functions for sharing
private fun shareAccountDetails(accountDetails: AccountDetails) {
    // In a real app, this would use Android's sharing intent
    // For now, this is just a placeholder
    val shareText = """
        Account Holder: ${accountDetails.accountHolderName}
        Bank: ${accountDetails.bankName}
        Account Number: ${accountDetails.accountNumber}
        Routing Number: ${accountDetails.routingNumber}
        SWIFT Code: ${accountDetails.swiftCode}
        
        Shared securely from Secure Banking App
    """.trimIndent()

    // TODO: Implement actual sharing intent
    println("Sharing: $shareText")
}

private fun shareRoutingNumber(accountDetails: AccountDetails) {
    val shareText = """
        ${accountDetails.bankName}
        Routing Number: ${accountDetails.routingNumber}
        SWIFT Code: ${accountDetails.swiftCode}
        
        Shared securely from Secure Banking App
    """.trimIndent()

    // TODO: Implement actual sharing intent
    println("Sharing routing: $shareText")
}