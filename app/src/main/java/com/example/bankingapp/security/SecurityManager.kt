package com.example.bankingapp.security


import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.bankingapp.domain.model.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import com.example.bankingapp.domain.model.Result

/**
 * Comprehensive security manager for the banking app
 */
class SecurityManager(
    private val context: Context,
    private val encryptionManager: EncryptionManager,
    private val biometricAuthManager: BiometricAuthManager,
    private val encryptedPrefs: EncryptedSharedPreferences
) {
    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_LAST_AUTH_TIME = "last_auth_time"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_TIME = "lockout_time"

        private const val SESSION_TIMEOUT = 5 * 60 * 1000L // 5 minutes
        private const val MAX_FAILED_ATTEMPTS = 3
        private const val LOCKOUT_DURATION = 15 * 60 * 1000L // 15 minutes
    }

    private val _securityState = MutableStateFlow(SecurityState.UNKNOWN)
    val securityState: Flow<SecurityState> = _securityState.asStateFlow()

    private val _biometricState = MutableStateFlow(BiometricState.NOT_CONFIGURED)
    val biometricState: Flow<BiometricState> = _biometricState.asStateFlow()

    init {
        initializeSecurityState()
    }

    /**
     * Initialize security state on app start
     */
    private fun initializeSecurityState() {
        val biometricStatus = biometricAuthManager.isBiometricAvailable()
        val isBiometricEnabled = encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

        _biometricState.value = when {
            !biometricStatus.isAvailable() -> BiometricState.NOT_AVAILABLE
            !isBiometricEnabled -> BiometricState.NOT_CONFIGURED
            else -> BiometricState.CONFIGURED
        }

        // Check if user is locked out
        if (isUserLockedOut()) {
            _securityState.value = SecurityState.LOCKED_OUT
        } else {
            _securityState.value = SecurityState.AUTHENTICATION_REQUIRED
        }
    }

    /**
     * Enable biometric authentication
     */
    suspend fun enableBiometricAuth(): Result<Unit> {
        return try {
            val biometricStatus = biometricAuthManager.isBiometricAvailable()
            if (!biometricStatus.isAvailable()) {
                return Result.Error(
                    AppError.SecurityError("Biometric authentication not available: ${biometricStatus.getUserFriendlyMessage()}")
                )
            }

            encryptedPrefs.edit()
                .putBoolean(KEY_BIOMETRIC_ENABLED, true)
                .apply()

            _biometricState.value = BiometricState.CONFIGURED
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.SecurityError("Failed to enable biometric auth: ${e.message}"))
        }
    }

    /**
     * Disable biometric authentication
     */
    fun disableBiometricAuth() {
        encryptedPrefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .remove(KEY_SESSION_TOKEN)
            .apply()

        _biometricState.value = BiometricState.NOT_CONFIGURED
        _securityState.value = SecurityState.AUTHENTICATION_REQUIRED
    }

    /**
     * Authenticate user with biometrics
     */
    suspend fun authenticateWithBiometrics(
        activity: androidx.fragment.app.FragmentActivity,
        title: String = "Authenticate",
        subtitle: String = "Use your biometric credential to access your account"
    ): Result<AuthenticationResult> {
        return try {
            if (!isBiometricEnabled()) {
                return Result.Error(AppError.SecurityError("Biometric authentication not enabled"))
            }

            if (isUserLockedOut()) {
                return Result.Error(AppError.SecurityError("Account temporarily locked"))
            }

            val result = biometricAuthManager.authenticate(activity, title, subtitle)
            when (result) {
                is BiometricResult.Success -> {
                    handleSuccessfulAuth()
                    Result.Success(AuthenticationResult.SUCCESS)
                }
                is BiometricResult.Failed -> {
                    handleFailedAuth()
                    Result.Error(AppError.AuthenticationError("Authentication failed"))
                }
                is BiometricResult.Error -> {
                    handleFailedAuth()
                    Result.Error(AppError.AuthenticationError("Authentication error: ${result.errorMessage}"))
                }
            }
        } catch (e: Exception) {
            Result.Error(AppError.SecurityError("Biometric authentication failed: ${e.message}"))
        }
    }

    /**
     * Check if user session is valid
     */
    fun isSessionValid(): Boolean {
        val lastAuthTime = encryptedPrefs.getLong(KEY_LAST_AUTH_TIME, 0)
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastAuthTime) < SESSION_TIMEOUT
    }

    /**
     * Check if biometric authentication is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false) &&
                biometricAuthManager.isBiometricAvailable().isAvailable()
    }

    /**
     * Invalidate current session
     */
    fun invalidateSession() {
        encryptedPrefs.edit()
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_LAST_AUTH_TIME)
            .apply()

        _securityState.value = SecurityState.AUTHENTICATION_REQUIRED
    }

    /**
     * Generate secure session token
     */
    private fun generateSessionToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Handle successful authentication
     */
    private fun handleSuccessfulAuth() {
        val currentTime = System.currentTimeMillis()
        val sessionToken = generateSessionToken()

        encryptedPrefs.edit()
            .putLong(KEY_LAST_AUTH_TIME, currentTime)
            .putString(KEY_SESSION_TOKEN, sessionToken)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .remove(KEY_LOCKOUT_TIME)
            .apply()

        _securityState.value = SecurityState.AUTHENTICATED
    }

    /**
     * Handle failed authentication
     */
    private fun handleFailedAuth() {
        val failedAttempts = encryptedPrefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1

        encryptedPrefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, failedAttempts)
            .apply()

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            val lockoutTime = System.currentTimeMillis() + LOCKOUT_DURATION
            encryptedPrefs.edit()
                .putLong(KEY_LOCKOUT_TIME, lockoutTime)
                .apply()

            _securityState.value = SecurityState.LOCKED_OUT
        }
    }

    /**
     * Check if user is locked out
     */
    private fun isUserLockedOut(): Boolean {
        val lockoutTime = encryptedPrefs.getLong(KEY_LOCKOUT_TIME, 0)
        return if (lockoutTime > 0) {
            System.currentTimeMillis() < lockoutTime
        } else {
            false
        }
    }

    /**
     * Get remaining lockout time in minutes
     */
    fun getRemainingLockoutTime(): Long {
        val lockoutTime = encryptedPrefs.getLong(KEY_LOCKOUT_TIME, 0)
        val currentTime = System.currentTimeMillis()
        return if (lockoutTime > currentTime) {
            TimeUnit.MILLISECONDS.toMinutes(lockoutTime - currentTime)
        } else {
            0
        }
    }

    /**
     * Validate device security
     */
    fun validateDeviceSecurity(): DeviceSecurityStatus {
        return DeviceSecurityStatus(
            isDeviceSecure = isDeviceSecure(),
            isRooted = isDeviceRooted(),
            hasScreenLock = hasScreenLock(),
            biometricAvailable = biometricAuthManager.isBiometricAvailable().isAvailable()
        )
    }

    /**
     * Check if device has basic security (screen lock)
     */
    private fun isDeviceSecure(): Boolean {
        return try {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.isDeviceSecure
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Basic root detection (not foolproof)
     */
    private fun isDeviceRooted(): Boolean {
        return try {
            val buildTags = Build.TAGS
            buildTags != null && buildTags.contains("test-keys")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if device has screen lock
     */
    private fun hasScreenLock(): Boolean {
        return try {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.isKeyguardSecure
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clear all security data (for logout)
     */
    fun clearSecurityData() {
        encryptedPrefs.edit()
            .remove(KEY_LAST_AUTH_TIME)
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_FAILED_ATTEMPTS)
            .remove(KEY_LOCKOUT_TIME)
            .apply()

        _securityState.value = SecurityState.AUTHENTICATION_REQUIRED
    }
}

/**
 * Security state enum
 */
enum class SecurityState {
    UNKNOWN,
    AUTHENTICATION_REQUIRED,
    AUTHENTICATED,
    LOCKED_OUT,
    SESSION_EXPIRED
}

/**
 * Biometric state enum
 */
enum class BiometricState {
    NOT_AVAILABLE,
    NOT_CONFIGURED,
    CONFIGURED,
    FAILED
}

/**
 * Authentication result enum
 */
enum class AuthenticationResult {
    SUCCESS,
    FAILED,
    CANCELLED,
    ERROR
}

/**
 * Device security status
 */
data class DeviceSecurityStatus(
    val isDeviceSecure: Boolean,
    val isRooted: Boolean,
    val hasScreenLock: Boolean,
    val biometricAvailable: Boolean
) {
    fun isSecure(): Boolean = isDeviceSecure && hasScreenLock && !isRooted

    fun getSecurityLevel(): SecurityLevel = when {
        isSecure() && biometricAvailable -> SecurityLevel.HIGH
        isSecure() -> SecurityLevel.MEDIUM
        hasScreenLock -> SecurityLevel.LOW
        else -> SecurityLevel.NONE
    }
}

/**
 * Security level enum
 */
enum class SecurityLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}