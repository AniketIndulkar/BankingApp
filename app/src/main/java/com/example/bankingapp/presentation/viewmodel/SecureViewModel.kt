package com.example.bankingapp.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.domain.model.AppError
import com.example.bankingapp.domain.model.Result
import com.example.bankingapp.security.SecurityState
import com.example.bankingapp.security.SessionManager
import com.example.bankingapp.security.SessionState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.bankingapp.security.SecurityManager

/**
 * Security middleware that wraps ViewModels with security checks
 */
abstract class SecureViewModel(
    private val securityManager: SecurityManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _securityState = MutableStateFlow(SecurityState.AUTHENTICATION_REQUIRED)
    val securityState: StateFlow<SecurityState> = _securityState.asStateFlow()

    private val _isSessionValid = MutableStateFlow(false)
    val isSessionValid: StateFlow<Boolean> = _isSessionValid.asStateFlow()

    init {
        // Monitor security state changes
        viewModelScope.launch {
            securityManager.securityState.collect { state ->
                _securityState.value = state
                _isSessionValid.value = state == SecurityState.AUTHENTICATED
            }
        }

        // Monitor session state changes
        viewModelScope.launch {
            sessionManager.sessionState.collect { sessionState ->
                when (sessionState) {
                    SessionState.EXPIRED,
                    SessionState.BACKGROUND_TIMEOUT -> {
                        _securityState.value = SecurityState.SESSION_EXPIRED
                        _isSessionValid.value = false
                        onSessionExpired()
                    }
                    SessionState.WARNING -> {
                        onSessionWarning()
                    }
                    SessionState.ACTIVE -> {
                        _isSessionValid.value = true
                    }
                    else -> {
                        _isSessionValid.value = false
                    }
                }
            }
        }
    }

    /**
     * Execute a secure operation that requires authentication
     */
    protected suspend fun <T> executeSecureOperation(
        requiresBiometric: Boolean = false,
        operation: suspend () -> Result<T>
    ): Result<T> {
        // Check if session is valid
        if (!sessionManager.isSessionActive()) {
            return Result.Error(AppError.AuthenticationError("Session expired"))
        }

        // Check if biometric authentication is required and available
        if (requiresBiometric && !securityManager.isBiometricEnabled()) {
            return Result.Error(AppError.SecurityError("Biometric authentication required"))
        }

        // Record activity to extend session
        sessionManager.recordActivity()

        // Execute the operation
        return try {
            operation()
        } catch (e: Exception) {
            Result.Error(AppError.UnknownError(e.message ?: "Unknown error"))
        }
    }

    /**
     * Execute operation with automatic retry on auth failure
     */
    protected suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        operation: suspend () -> Result<T>
    ): Result<T> {
        var lastError: AppError? = null

        repeat(maxRetries) { attempt ->
            when (val result = operation()) {
                is Result.Success -> return result
                is Result.Error -> {
                    lastError = result.exception
                    if (result.exception is AppError.AuthenticationError && attempt < maxRetries - 1) {
                        // Wait before retry
//                        kotlinx.coroutines.delay(1000 * (attempt + 1))
                    } else {
                        return result
                    }
                }
                is Result.Loading -> { /* Continue to next attempt */ }
            }
        }

        return Result.Error(lastError ?: AppError.UnknownError("Max retries exceeded"))
    }

    /**
     * Called when session expires
     */
    protected open fun onSessionExpired() {
        // Default implementation - subclasses can override
    }

    /**
     * Called when session warning is triggered
     */
    protected open fun onSessionWarning() {
        // Default implementation - subclasses can override
    }

    /**
     * Validate device security before sensitive operations
     */
    protected fun validateDeviceSecurity(): Result<Unit> {
        val deviceStatus = securityManager.validateDeviceSecurity()

        return when {
            deviceStatus.isRooted -> Result.Error(
                AppError.SecurityError("Device appears to be rooted")
            )
            !deviceStatus.hasScreenLock -> Result.Error(
                AppError.SecurityError("Device must have screen lock enabled")
            )
            !deviceStatus.isDeviceSecure -> Result.Error(
                AppError.SecurityError("Device does not meet security requirements")
            )
            else -> Result.Success(Unit)
        }
    }

    /**
     * Rate limiting for sensitive operations
     */
    private val operationTimestamps = mutableMapOf<String, Long>()

    protected fun checkRateLimit(
        operationType: String,
        maxOperationsPerMinute: Int = 10
    ): Result<Unit> {
        val currentTime = System.currentTimeMillis()
        val operationKey = "${this::class.simpleName}_$operationType"

        val lastOperation = operationTimestamps[operationKey] ?: 0
        val timeSinceLastOperation = currentTime - lastOperation
        val minInterval = 60000L / maxOperationsPerMinute // ms between operations

        return if (timeSinceLastOperation >= minInterval) {
            operationTimestamps[operationKey] = currentTime
            Result.Success(Unit)
        } else {
            Result.Error(AppError.SecurityError("Operation rate limit exceeded"))
        }
    }
}

/**
 * Security annotation for methods that require biometric authentication
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresBiometric(val reason: String = "Access to sensitive data")

/**
 * Security annotation for methods that require device validation
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresDeviceValidation

/**
 * Security annotation for rate-limited operations
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimited(val maxOperationsPerMinute: Int = 10)

/**
 * Security middleware for sensitive data access
 */
class SensitiveDataGuard(
    private val securityManager: SecurityManager,
    private val sessionManager: SessionManager
) {

    /**
     * Guard for accessing card details
     */
    suspend fun accessCardDetails(action: suspend () -> Unit): Result<Unit> {
        return guardSensitiveAccess(
            dataType = "card_details",
            requiresBiometric = true,
            action = action
        )
    }

    /**
     * Guard for accessing account details
     */
    suspend fun accessAccountDetails(action: suspend () -> Unit): Result<Unit> {
        return guardSensitiveAccess(
            dataType = "account_details",
            requiresBiometric = true,
            action = action
        )
    }

    /**
     * Guard for accessing transaction history
     */
    suspend fun accessTransactionHistory(action: suspend () -> Unit): Result<Unit> {
        return guardSensitiveAccess(
            dataType = "transaction_history",
            requiresBiometric = false,
            action = action
        )
    }

    /**
     * Generic guard for sensitive data access
     */
    private suspend fun guardSensitiveAccess(
        dataType: String,
        requiresBiometric: Boolean,
        action: suspend () -> Unit
    ): Result<Unit> {
        // Check session validity
        if (!sessionManager.isSessionActive()) {
            return Result.Error(AppError.AuthenticationError("Session expired"))
        }

        // Check biometric requirement
        if (requiresBiometric && !securityManager.isBiometricEnabled()) {
            return Result.Error(AppError.SecurityError("Biometric authentication required for $dataType"))
        }

        // Validate device security
        val deviceStatus = securityManager.validateDeviceSecurity()
        if (!deviceStatus.isSecure()) {
            return Result.Error(AppError.SecurityError("Device security requirements not met"))
        }

        // Record access attempt
        sessionManager.recordActivity()

        return try {
            action()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.UnknownError("Failed to access $dataType: ${e.message}"))
        }
    }
}

/**
 * Security audit logger
 */
class SecurityAuditLogger {
    private val auditLogs = mutableListOf<SecurityAuditEvent>()

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun logAuthenticationAttempt(success: Boolean, method: String, timestamp: Long = System.currentTimeMillis()) {
        auditLogs.add(
            SecurityAuditEvent.AuthenticationAttempt(
                success = success,
                method = method,
                timestamp = timestamp
            )
        )

        // Keep only last 100 events
        if (auditLogs.size > 100) {
            auditLogs.removeFirst()
        }
    }

    fun logSensitiveDataAccess(dataType: String, timestamp: Long = System.currentTimeMillis()) {
        auditLogs.add(
            SecurityAuditEvent.SensitiveDataAccess(
                dataType = dataType,
                timestamp = timestamp
            )
        )
    }

    fun logSecurityViolation(violation: String, timestamp: Long = System.currentTimeMillis()) {
        auditLogs.add(
            SecurityAuditEvent.SecurityViolation(
                violation = violation,
                timestamp = timestamp
            )
        )
    }

    fun getAuditLogs(): List<SecurityAuditEvent> = auditLogs.toList()

    fun clearAuditLogs() = auditLogs.clear()
}

/**
 * Security audit events
 */
sealed class SecurityAuditEvent {
    abstract val timestamp: Long

    data class AuthenticationAttempt(
        val success: Boolean,
        val method: String,
        override val timestamp: Long
    ) : SecurityAuditEvent()

    data class SensitiveDataAccess(
        val dataType: String,
        override val timestamp: Long
    ) : SecurityAuditEvent()

    data class SecurityViolation(
        val violation: String,
        override val timestamp: Long
    ) : SecurityAuditEvent()

    data class SessionEvent(
        val eventType: String,
        override val timestamp: Long
    ) : SecurityAuditEvent()
}