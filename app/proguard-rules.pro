# app/proguard-rules.pro - Security-focused ProGuard configuration

# Keep security-critical classes
-keep class com.bankingapp.secure.security.** { *; }
-keep class com.bankingapp.secure.domain.model.** { *; }

# Obfuscate sensitive data classes
-keep class com.bankingapp.secure.data.local.entity.** {
    # Keep only necessary fields, obfuscate others
    java.lang.String id;
}

# Keep encryption and security APIs
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-keep class android.security.** { *; }

# Biometric APIs
-keep class androidx.biometric.** { *; }

# Room database - keep entity classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Retrofit and Moshi
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class *

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Obfuscate but keep functionality
-repackageclasses 'obf'
-allowaccessmodification
-overloadaggressively

# Security: Remove debug information
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Anti-tampering: Make reverse engineering harder
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5

# app/src/main/res/values/security_config.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Security configuration values -->
    <integer name="session_timeout_minutes">5</integer>
    <integer name="max_failed_attempts">3</integer>
    <integer name="lockout_duration_minutes">15</integer>
    <integer name="background_timeout_seconds">30</integer>

    <!-- Cache expiry times in minutes -->
    <integer name="account_cache_expiry">5</integer>
    <integer name="transaction_cache_expiry">10</integer>
    <integer name="card_cache_expiry">15</integer>

    <!-- Rate limiting -->
    <integer name="max_api_calls_per_minute">60</integer>
    <integer name="max_biometric_attempts_per_hour">10</integer>

    <!-- Security strings -->
    <string name="biometric_title">Secure Banking Access</string>
    <string name="biometric_subtitle">Use your biometric credential to access your banking information</string>
    <string name="biometric_negative_button">Use PIN</string>

    <!-- Error messages -->
    <string name="error_session_expired">Your session has expired. Please authenticate again.</string>
    <string name="error_device_not_secure">Your device does not meet security requirements.</string>
    <string name="error_biometric_not_available">Biometric authentication is not available on this device.</string>
    <string name="error_account_locked">Account temporarily locked due to multiple failed attempts.</string>
    <string name="error_network_security">Network security validation failed.</string>

    <!-- Security warnings -->
    <string name="warning_rooted_device">Warning: Device appears to be rooted. Security may be compromised.</string>
    <string name="warning_no_screen_lock">Warning: Please enable screen lock for enhanced security.</string>
    <string name="warning_session_expiring">Your session will expire in 1 minute.</string>
</resources>

# app/src/main/res/values/attrs.xml - Custom attributes for security components
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Security level indicator attributes -->
    <declare-styleable name="SecurityIndicator">
        <attr name="securityLevel" format="enum">
            <enum name="none" value="0" />
            <enum name="low" value="1" />
            <enum name="medium" value="2" />
            <enum name="high" value="3" />
        </attr>
        <attr name="showIcon" format="boolean" />
        <attr name="showText" format="boolean" />
    </declare-styleable>

    <!-- Biometric prompt attributes -->
    <declare-styleable name="BiometricPrompt">
        <attr name="promptTitle" format="string" />
        <attr name="promptSubtitle" format="string" />
        <attr name="negativeButtonText" format="string" />
        <attr name="allowDeviceCredential" format="boolean" />
    </declare-styleable>
</resources>

# Security Constants - Kotlin object for centralized security configuration
package com.bankingapp.secure.security

object SecurityConstants {
    // Session management
    const val SESSION_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    const val SESSION_WARNING_MS = 4 * 60 * 1000L // 4 minutes (1 min warning)
    const val BACKGROUND_TIMEOUT_MS = 30 * 1000L // 30 seconds

    // Authentication
    const val MAX_FAILED_ATTEMPTS = 3
    const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L // 15 minutes
    const val BIOMETRIC_TIMEOUT_MS = 30 * 1000L // 30 seconds

    // Cache management
    const val ACCOUNT_CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    const val TRANSACTION_CACHE_DURATION_MS = 10 * 60 * 1000L // 10 minutes
    const val CARD_CACHE_DURATION_MS = 15 * 60 * 1000L // 15 minutes

    // Rate limiting
    const val MAX_API_CALLS_PER_MINUTE = 60
    const val MAX_BIOMETRIC_ATTEMPTS_PER_HOUR = 10
    const val MIN_TIME_BETWEEN_OPERATIONS_MS = 1000L // 1 second

    // Encryption
    const val AES_KEY_SIZE = 256
    const val GCM_IV_LENGTH = 12
    const val GCM_TAG_LENGTH = 16
    const val PBKDF2_ITERATIONS = 10000

    // Device security
    const val MIN_SDK_VERSION = 23 // Android 6.0 for proper security features
    const val REQUIRE_HARDWARE_KEYSTORE = true
    const val ALLOW_DEBUG_BUILDS = false // Set to false for production

    // Network security
    const val CERTIFICATE_PINNING_ENABLED = true
    const val REQUIRE_TLS_1_2_MINIMUM = true
    const val NETWORK_TIMEOUT_MS = 30 * 1000L // 30 seconds

    // Audit logging
    const val MAX_AUDIT_LOGS = 1000
    const val AUDIT_LOG_RETENTION_DAYS = 30

    // Error codes
    const val ERROR_SESSION_EXPIRED = 1001
    const val ERROR_DEVICE_NOT_SECURE = 1002
    const val ERROR_BIOMETRIC_FAILED = 1003
    const val ERROR_RATE_LIMIT_EXCEEDED = 1004
    const val ERROR_ENCRYPTION_FAILED = 1005
}