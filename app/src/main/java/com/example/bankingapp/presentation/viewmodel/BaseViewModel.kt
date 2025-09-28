package com.example.bankingapp.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankingapp.domain.model.AppError
import com.example.bankingapp.utils.NetworkManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.bankingapp.domain.model.Result

/**
 * Base ViewModel with network status awareness
 */
abstract class BaseViewModel(
    private val networkManager: NetworkManager
) : ViewModel() {

    // Network connectivity state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Loading state
    internal val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    internal val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Last updated timestamp
    internal val _lastUpdated = MutableStateFlow<Long?>(null)
    val lastUpdated: StateFlow<Long?> = _lastUpdated.asStateFlow()

    init {
        // Monitor network connectivity
        viewModelScope.launch {
            networkManager.observeConnectivity().collect { connected ->
                _isConnected.value = connected
                onNetworkStatusChanged(connected)
            }
        }

        // Set initial network status
        _isConnected.value = networkManager.isConnected()
    }

    /**
     * Called when network status changes
     */
    protected open fun onNetworkStatusChanged(isConnected: Boolean) {
        // Override in subclasses if needed
    }

    /**
     * Execute operation with loading and error handling
     */
    protected suspend fun <T> executeOperation(
        operation: suspend () -> Result<T>,
        onSuccess: (T) -> Unit = {},
        onError: (AppError) -> Unit = { _error.value = it.message }
    ) {
        _isLoading.value = true
        _error.value = null

        try {
            when (val result = operation()) {
                is Result.Success -> {
                    onSuccess(result.data)
                    _lastUpdated.value = System.currentTimeMillis()
                }
                is Result.Error -> {
                    onError(result.exception)
                }
                is Result.Loading -> {
                    // Loading state already set
                }
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Unknown error occurred"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Refresh data - to be implemented by subclasses
     */
    abstract fun refresh()
}

/**
 * UI State for network-aware screens
 */
data class NetworkUiState(
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long? = null
) {
    val showOfflineIndicator: Boolean
        get() = !isConnected && lastUpdated != null

    val connectionStatusText: String
        get() = if (isConnected) "Online - Data is current" else "Offline - Showing cached data"
}