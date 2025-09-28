package com.example.bankingapp.security

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.bankingapp.domain.model.Result
import kotlinx.coroutines.launch

@Composable
fun BiometricAuthenticationScreen(
    title: String = "Secure Access",
    subtitle: String = "Authenticate to view sensitive information",
    onAuthenticationSuccess: () -> Unit,
    onAuthenticationFailed: (String) -> Unit,
    onCancel: () -> Unit,
    securityManager: SecurityManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Auto-trigger biometric authentication when screen loads
        if (securityManager.isBiometricEnabled()) {
            coroutineScope.launch {
                isLoading = true
                val result = securityManager.authenticateWithBiometrics(
                    activity = context as FragmentActivity,
                    title = title,
                    subtitle = subtitle
                )
                isLoading = false

                when (result) {
                    is Result.Success -> onAuthenticationSuccess()
                    is Result.Error -> {
                        errorMessage = result.exception.message
                        onAuthenticationFailed(result.exception.message ?: "Authentication failed")
                    }
                    is Result.Loading -> { /* Handle loading if needed */ }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Biometric icon
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Biometric Authentication",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
        }

        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Try Again button
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    coroutineScope.launch {
                        isLoading = true
                        val result = securityManager.authenticateWithBiometrics(
                            activity = context as FragmentActivity,
                            title = title,
                            subtitle = subtitle
                        )
                        isLoading = false

                        when (result) {
                            is Result.Success -> onAuthenticationSuccess()
                            is Result.Error -> {
                                errorMessage = result.exception.message
                                onAuthenticationFailed(result.exception.message ?: "Authentication failed")
                            }
                            is Result.Loading -> { /* Handle loading if needed */ }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Try Again")
            }

            // Cancel button
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    }
}

/**
 * Security status indicator composable
 */
@Composable
fun SecurityStatusIndicator(
    securityLevel: SecurityLevel,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (securityLevel) {
        SecurityLevel.HIGH -> Triple(
            Icons.Default.Security,
            MaterialTheme.colorScheme.primary,
            "High Security"
        )
        SecurityLevel.MEDIUM -> Triple(
            Icons.Default.Security,
            MaterialTheme.colorScheme.tertiary,
            "Medium Security"
        )
        SecurityLevel.LOW -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.error,
            "Low Security"
        )
        SecurityLevel.NONE -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.error,
            "No Security"
        )
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/**
 * Session timer display
 */
@Composable
fun SessionTimerDisplay(
    timeRemaining: Long,
    sessionState: SessionState,
    modifier: Modifier = Modifier
) {
    val minutes = (timeRemaining / 1000) / 60
    val seconds = (timeRemaining / 1000) % 60

    val (color, text) = when (sessionState) {
        SessionState.ACTIVE -> {
            if (timeRemaining < 60000) { // Less than 1 minute
                MaterialTheme.colorScheme.error to "Session expires in ${seconds}s"
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant to "Session: ${minutes}m ${seconds}s"
            }
        }
        SessionState.WARNING -> {
            MaterialTheme.colorScheme.error to "Session expires in ${seconds}s"
        }
        SessionState.EXPIRED -> {
            MaterialTheme.colorScheme.error to "Session expired"
        }
        else -> {
            MaterialTheme.colorScheme.onSurfaceVariant to "No active session"
        }
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * Biometric setup prompt
 */
@Composable
fun BiometricSetupPrompt(
    onEnableBiometric: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Enhanced Security",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enable biometric authentication for quick and secure access to your sensitive banking information.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEnableBiometric,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Enable")
                }

                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Skip")
                }
            }
        }
    }
}

/**
 * Security warning dialog
 */
@Composable
fun SecurityWarningDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "OK",
    dismissText: String = "Cancel"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title)
            }
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

/**
 * Device security status card
 */
@Composable
fun DeviceSecurityCard(
    deviceSecurityStatus: DeviceSecurityStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Device Security",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecurityCheckItem(
                label = "Screen Lock",
                isSecure = deviceSecurityStatus.hasScreenLock,
                description = if (deviceSecurityStatus.hasScreenLock)
                    "Device has screen lock enabled"
                else "Please enable screen lock for security"
            )

            SecurityCheckItem(
                label = "Device Security",
                isSecure = deviceSecurityStatus.isDeviceSecure,
                description = if (deviceSecurityStatus.isDeviceSecure)
                    "Device meets security requirements"
                else "Device security requirements not met"
            )

            SecurityCheckItem(
                label = "Biometric Authentication",
                isSecure = deviceSecurityStatus.biometricAvailable,
                description = if (deviceSecurityStatus.biometricAvailable)
                    "Biometric authentication available"
                else "Biometric authentication not available"
            )

            if (deviceSecurityStatus.isRooted) {
                SecurityCheckItem(
                    label = "Root Detection",
                    isSecure = false,
                    description = "Device appears to be rooted - security may be compromised"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SecurityStatusIndicator(
                securityLevel = deviceSecurityStatus.getSecurityLevel(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Individual security check item
 */
@Composable
private fun SecurityCheckItem(
    label: String,
    isSecure: Boolean,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSecure) Icons.Default.Security else Icons.Default.Warning,
            contentDescription = if (isSecure) "Secure" else "Warning",
            tint = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}