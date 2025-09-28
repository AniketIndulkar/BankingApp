package com.example.bankingapp.security


import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

/**
 * Manages user sessions and automatic timeouts for security
 */
class SessionManager(
    private val securityManager: SecurityManager,
    private val coroutineScope: CoroutineScope
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    companion object {
        private const val SESSION_TIMEOUT = 5 * 60 * 1000L // 5 minutes
        private const val WARNING_TIMEOUT = 4 * 60 * 1000L // 4 minutes (1 min warning)
        private const val BACKGROUND_TIMEOUT = 30 * 1000L // 30 seconds in background
    }

    private val _sessionState = MutableStateFlow(SessionState.INACTIVE)
    val sessionState = _sessionState.asStateFlow()

    private val _sessionTimeRemaining = MutableStateFlow(SESSION_TIMEOUT)
    val sessionTimeRemaining = _sessionTimeRemaining.asStateFlow()

    private var sessionJob: Job? = null
    private var warningJob: Job? = null
    private var backgroundJob: Job? = null

    private val lastActivityTime = AtomicLong(System.currentTimeMillis())
    private var isAppInForeground = true
    private var activityCount = 0

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Start a new user session
     */
    fun startSession() {
        _sessionState.value = SessionState.ACTIVE
        lastActivityTime.set(System.currentTimeMillis())
        startSessionTimer()
    }

    /**
     * End the current session
     */
    fun endSession() {
        _sessionState.value = SessionState.INACTIVE
        cancelAllTimers()
        securityManager.invalidateSession()
    }

    /**
     * Extend the current session (reset timeout)
     */
    fun extendSession() {
        if (_sessionState.value == SessionState.ACTIVE) {
            lastActivityTime.set(System.currentTimeMillis())
            _sessionTimeRemaining.value = SESSION_TIMEOUT
            startSessionTimer()
        }
    }

    /**
     * Record user activity to extend session
     */
    fun recordActivity() {
        lastActivityTime.set(System.currentTimeMillis())
        if (_sessionState.value == SessionState.ACTIVE) {
            extendSession()
        }
    }

    /**
     * Check if session is still valid
     */
    fun isSessionActive(): Boolean {
        return _sessionState.value == SessionState.ACTIVE &&
                securityManager.isSessionValid()
    }

    /**
     * Force session timeout (for testing or admin purposes)
     */
    fun forceTimeout() {
        _sessionState.value = SessionState.EXPIRED
        cancelAllTimers()
        securityManager.invalidateSession()
    }

    /**
     * Start session countdown timer
     */
    private fun startSessionTimer() {
        cancelAllTimers()

        // Main session timer
        sessionJob = coroutineScope.launch {
            var remainingTime = SESSION_TIMEOUT

            while (remainingTime > 0 && _sessionState.value == SessionState.ACTIVE) {
                _sessionTimeRemaining.value = remainingTime
                delay(1000) // Update every second
                remainingTime -= 1000

                // Check if user was active recently
                val timeSinceActivity = System.currentTimeMillis() - lastActivityTime.get()
                if (timeSinceActivity > SESSION_TIMEOUT) {
                    break
                }
                remainingTime = SESSION_TIMEOUT - timeSinceActivity
            }

            if (_sessionState.value == SessionState.ACTIVE) {
                _sessionState.value = SessionState.EXPIRED
                securityManager.invalidateSession()
            }
        }

        // Warning timer (1 minute before timeout)
        warningJob = coroutineScope.launch {
            delay(WARNING_TIMEOUT)
            if (_sessionState.value == SessionState.ACTIVE) {
                _sessionState.value = SessionState.WARNING
            }
        }
    }

    /**
     * Start background timeout timer
     */
    private fun startBackgroundTimer() {
        backgroundJob = coroutineScope.launch {
            delay(BACKGROUND_TIMEOUT)
            if (!isAppInForeground && _sessionState.value == SessionState.ACTIVE) {
                _sessionState.value = SessionState.BACKGROUND_TIMEOUT
                securityManager.invalidateSession()
            }
        }
    }

    /**
     * Cancel all running timers
     */
    private fun cancelAllTimers() {
        sessionJob?.cancel()
        warningJob?.cancel()
        backgroundJob?.cancel()
    }

    // Lifecycle Observer Methods
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isAppInForeground = true
        backgroundJob?.cancel()

        // If session was active before background, check if it's still valid
        if (_sessionState.value == SessionState.BACKGROUND_TIMEOUT) {
            _sessionState.value = SessionState.INACTIVE
        } else if (_sessionState.value == SessionState.ACTIVE) {
            // Resume session timer
            startSessionTimer()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppInForeground = false

        if (_sessionState.value == SessionState.ACTIVE) {
            cancelAllTimers()
            startBackgroundTimer()
        }
    }

    // Activity Lifecycle Callbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        activityCount++
        if (activityCount == 1) {
            // App came to foreground
            onStart(ProcessLifecycleOwner.get())
        }
    }

    override fun onActivityResumed(activity: Activity) {
        recordActivity()
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        activityCount--
        if (activityCount == 0) {
            // App went to background
            onStop(ProcessLifecycleOwner.get())
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    /**
     * Get session statistics
     */
    fun getSessionStats(): SessionStats {
        val currentTime = System.currentTimeMillis()
        val sessionDuration = if (_sessionState.value == SessionState.ACTIVE) {
            currentTime - (lastActivityTime.get() - SESSION_TIMEOUT + _sessionTimeRemaining.value)
        } else 0L

        return SessionStats(
            isActive = _sessionState.value == SessionState.ACTIVE,
            timeRemaining = _sessionTimeRemaining.value,
            sessionDuration = sessionDuration,
            lastActivity = lastActivityTime.get(),
            isInForeground = isAppInForeground
        )
    }
}

/**
 * Session state enum
 */
enum class SessionState {
    INACTIVE,           // No active session
    ACTIVE,            // Session is active
    WARNING,           // Session about to expire (1 min warning)
    EXPIRED,           // Session has expired
    BACKGROUND_TIMEOUT // App was in background too long
}

/**
 * Session statistics
 */
data class SessionStats(
    val isActive: Boolean,
    val timeRemaining: Long,
    val sessionDuration: Long,
    val lastActivity: Long,
    val isInForeground: Boolean
)

/**
 * Session event sealed class
 */
sealed class SessionEvent {
    object SessionStarted : SessionEvent()
    object SessionExtended : SessionEvent()
    object SessionWarning : SessionEvent()
    object SessionExpired : SessionEvent()
    object BackgroundTimeout : SessionEvent()
    data class ActivityRecorded(val timestamp: Long) : SessionEvent()
}