package com.example.bankingapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Banking-specific color palette
private val BankingPrimary = Color(0xFF1565C0) // Deep Blue
private val BankingPrimaryVariant = Color(0xFF0D47A1) // Darker Blue
private val BankingSecondary = Color(0xFF43A047) // Success Green
private val BankingSecondaryVariant = Color(0xFF2E7D32) // Darker Green
private val BankingError = Color(0xFFD32F2F) // Error Red
private val BankingWarning = Color(0xFFFF9800) // Warning Orange
private val BankingSurface = Color(0xFFFAFAFA) // Light Gray
private val BankingBackground = Color(0xFFFFFFFF) // White

private val DarkBankingPrimary = Color(0xFF42A5F5) // Light Blue
private val DarkBankingSecondary = Color(0xFF66BB6A) // Light Green
private val DarkBankingSurface = Color(0xFF121212) // Dark Gray
private val DarkBankingBackground = Color(0xFF000000) // Black

private val LightColorScheme = lightColorScheme(
    primary = BankingPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),

    secondary = BankingSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E8),
    onSecondaryContainer = Color(0xFF1B5E20),

    tertiary = BankingWarning,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = Color(0xFFE65100),

    error = BankingError,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),

    background = BankingBackground,
    onBackground = Color(0xFF1A1A1A),

    surface = BankingSurface,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),

    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkBankingPrimary,
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color(0xFFE3F2FD),

    secondary = DarkBankingSecondary,
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF388E3C),
    onSecondaryContainer = Color(0xFFE8F5E8),

    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFFE65100),
    tertiaryContainer = Color(0xFFF57C00),
    onTertiaryContainer = Color(0xFFFFF3E0),

    error = Color(0xFFEF5350),
    onError = Color(0xFFB71C1C),
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color(0xFFFFEBEE),

    background = DarkBankingBackground,
    onBackground = Color(0xFFE0E0E0),

    surface = DarkBankingSurface,
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFBDBDBD),

    outline = Color(0xFF616161),
    outlineVariant = Color(0xFF424242)
)

@Composable
fun BankingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BankingTypography,
        shapes = BankingShapes,
        content = content
    )
}

// Custom colors for banking-specific UI elements
@Composable
fun BankingColors() = object {
    val success = if (isSystemInDarkTheme()) Color(0xFF66BB6A) else Color(0xFF43A047)
    val successContainer = if (isSystemInDarkTheme()) Color(0xFF2E7D32) else Color(0xFFE8F5E8)
    val onSuccessContainer = if (isSystemInDarkTheme()) Color(0xFFE8F5E8) else Color(0xFF1B5E20)

    val warning = if (isSystemInDarkTheme()) Color(0xFFFFB74D) else Color(0xFFFF9800)
    val warningContainer = if (isSystemInDarkTheme()) Color(0xFFF57C00) else Color(0xFFFFF3E0)
    val onWarningContainer = if (isSystemInDarkTheme()) Color(0xFFFFF3E0) else Color(0xFFE65100)

    val info = if (isSystemInDarkTheme()) Color(0xFF42A5F5) else Color(0xFF1976D2)
    val infoContainer = if (isSystemInDarkTheme()) Color(0xFF1976D2) else Color(0xFFE3F2FD)
    val onInfoContainer = if (isSystemInDarkTheme()) Color(0xFFE3F2FD) else Color(0xFF0D47A1)

    // Banking-specific colors
    val creditCard = Color(0xFF9C27B0) // Purple for credit cards
    val debitCard = Color(0xFF2196F3) // Blue for debit cards
    val depositAmount = success // Green for deposits
    val withdrawalAmount = MaterialTheme.colorScheme.error // Red for withdrawals
    val pendingTransaction = warning // Orange for pending
    val completedTransaction = success // Green for completed
}