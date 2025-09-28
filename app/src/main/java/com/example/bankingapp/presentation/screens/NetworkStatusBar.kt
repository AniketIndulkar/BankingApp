package com.example.bankingapp.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Global network status bar that appears at the top of screens
 */
@Composable
fun NetworkStatusBar(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isConnected,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "You're offline - Showing cached data",
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Connection status card for individual screens
 */
@Composable
fun ConnectionStatusCard(
    isConnected: Boolean,
    lastUpdated: Long?,
    modifier: Modifier = Modifier
) {
    if (!isConnected) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Offline Mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Showing cached data${lastUpdated?.let { " • Last updated: ${formatTime(it)}" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Connection status indicator for app bars
 */
@Composable
fun ConnectionStatusIndicator(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isConnected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.error,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.Cloud else Icons.Default.CloudOff,
                contentDescription = if (isConnected) "Online" else "Offline",
                modifier = Modifier.size(12.dp),
                tint = if (isConnected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onError
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isConnected) "Online" else "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = if (isConnected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onError
            )
        }
    }
}

/**
 * Snackbar for connectivity changes
 */
@Composable
fun ConnectivitySnackbar(
    isConnected: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = if (isConnected) {
        "Back online - Data will refresh automatically"
    } else {
        "You're offline - Using cached data"
    }

    val backgroundColor = if (isConnected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Snackbar(
        modifier = modifier,
        action = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = backgroundColor,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isConnected) Icons.Default.Cloud else Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(message)
        }
    }
}

/**
 * Network-aware refresh button
 */
@Composable
fun NetworkAwareRefreshButton(
    isConnected: Boolean,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onRefresh,
        enabled = isConnected && !isLoading,
        modifier = modifier
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
            isConnected -> {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Offline - Cannot refresh",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

/**
 * Data freshness indicator
 */
@Composable
fun DataFreshnessIndicator(
    lastUpdated: Long?,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val text = when {
        lastUpdated == null -> "No data available"
        isConnected -> "Updated ${formatTime(lastUpdated)}"
        else -> "Cached data from ${formatTime(lastUpdated)}"
    }

    val color = when {
        lastUpdated == null -> MaterialTheme.colorScheme.error
        isConnected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

// Helper function to format timestamp
private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}