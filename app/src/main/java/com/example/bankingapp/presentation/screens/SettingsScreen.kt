package com.example.bankingapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bankingapp.security.SecurityManager
import com.example.bankingapp.security.SecurityLevel
import com.example.bankingapp.security.DeviceSecurityCard
import com.example.bankingapp.security.SessionTimerDisplay
import com.example.bankingapp.security.SecurityStatusIndicator
import com.example.bankingapp.security.BiometricSetupPrompt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    securityManager: SecurityManager,
    onNavigateBack: () -> Unit = {}
) {
    var showBiometricDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Collect security states
//    val securityState by securityManager.securityState.collectAsStateWithLifecycle()
//    val biometricState by securityManager.biometricState.collectAsStateWithLifecycle()

    // Get device security status
    val deviceSecurityStatus = remember { securityManager.validateDeviceSecurity() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Security Section
        SettingsSection(title = "Security") {
            // Security Status Overview
            SecurityStatusIndicator(
                securityLevel = deviceSecurityStatus.getSecurityLevel(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Device Security Details
            DeviceSecurityCard(
                deviceSecurityStatus = deviceSecurityStatus
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Biometric Settings
            if (!securityManager.isBiometricEnabled() && deviceSecurityStatus.biometricAvailable) {
                BiometricSetupPrompt(
                    onEnableBiometric = {
                        scope.launch {
                            securityManager.enableBiometricAuth()
                        }
                    },
                    onSkip = { /* Do nothing */ }
                )
            } else {
                SettingsItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric Authentication",
                    subtitle = if (securityManager.isBiometricEnabled())
                        "Enabled - Secure access with fingerprint/face"
                    else "Not available on this device",
                    onClick = {
                        if (securityManager.isBiometricEnabled()) {
                            showBiometricDialog = true
                        }
                    },
                    enabled = securityManager.isBiometricEnabled()
                )
            }

            // Session Management
            SettingsItem(
                icon = Icons.Default.Timer,
                title = "Session Timeout",
                subtitle = "Auto-logout after 5 minutes of inactivity",
                onClick = { /* Handle session settings */ }
            )

            // Security Log
            SettingsItem(
                icon = Icons.Default.Security,
                title = "Security Log",
                subtitle = "View recent security events",
                onClick = { /* Handle security log */ }
            )
        }

        // Account Section
        SettingsSection(title = "Account") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Profile Information",
                subtitle = "Update your personal details",
                onClick = { /* Handle profile */ }
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Manage your notification preferences",
                onClick = { /* Handle notifications */ }
            )

            SettingsItem(
                icon = Icons.Default.Language,
                title = "Language & Region",
                subtitle = "English (US)",
                onClick = { /* Handle language */ }
            )
        }

        // Data & Privacy Section
        SettingsSection(title = "Data & Privacy") {
            SettingsItem(
                icon = Icons.Default.CloudOff,
                title = "Offline Mode",
                subtitle = "Data is cached locally for offline access",
                onClick = { /* Handle offline settings */ }
            )

            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Clear Cache",
                subtitle = "Clear locally stored data",
                onClick = { /* Handle clear cache */ }
            )

            SettingsItem(
                icon = Icons.Default.Security,
                title = "Privacy Policy",
                subtitle = "View our privacy policy",
                onClick = { /* Handle privacy policy */ }
            )
        }

        // Support Section
        SettingsSection(title = "Support") {
            SettingsItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                subtitle = "Get help with your account",
                onClick = { /* Handle help */ }
            )

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App version and information",
                onClick = { /* Handle about */ }
            )
        }

        // Danger Zone
        SettingsSection(title = "Account Actions") {
            SettingsItem(
                icon = Icons.Default.Logout,
                title = "Sign Out",
                subtitle = "Sign out of your account",
                onClick = { showLogoutDialog = true },
                isDestructive = true
            )
        }

        // Bottom padding
        Spacer(modifier = Modifier.height(32.dp))
    }

    // Biometric Settings Dialog
    if (showBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Biometric Authentication")
                }
            },
            text = {
                Text("Do you want to disable biometric authentication? You'll need to use your password to access sensitive information.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        securityManager.disableBiometricAuth()
                        showBiometricDialog = false
                    }
                ) {
                    Text("Disable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
            },
            text = {
                Text("Are you sure you want to sign out? You'll need to authenticate again to access your account.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        securityManager.clearSecurityData()
                        showLogoutDialog = false
                        // Handle navigation to login screen
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        content()
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { if (enabled) onClick },
        enabled = enabled,
        colors = if (isDestructive) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else if (enabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else if (enabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDestructive)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else if (enabled)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }

            if (enabled) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}