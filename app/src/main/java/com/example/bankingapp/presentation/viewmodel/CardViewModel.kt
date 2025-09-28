package com.example.bankingapp.presentation.viewmodel


import androidx.lifecycle.viewModelScope
import com.example.bankingapp.domain.model.PaymentCard
import com.example.bankingapp.domain.usecase.GetCardDetailsUseCase
import com.example.bankingapp.utils.NetworkManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.bankingapp.domain.model.Result


/**
 * ViewModel for Card Details Screen
 */
class CardViewModel(
    private val getCardDetailsUseCase: GetCardDetailsUseCase,
    networkManager: NetworkManager
) : BaseViewModel(networkManager) {

    // Initialize StateFlows BEFORE calling parent constructor
    // Card data
    private val _cards = MutableStateFlow<List<PaymentCard>>(emptyList())
    val cards: StateFlow<List<PaymentCard>> = _cards.asStateFlow()

    // Biometric authentication state
    private val _isBiometricRequired = MutableStateFlow(true)
    val isBiometricRequired: StateFlow<Boolean> = _isBiometricRequired.asStateFlow()

    private val _isBiometricAuthenticated = MutableStateFlow(false)
    val isBiometricAuthenticated: StateFlow<Boolean> = _isBiometricAuthenticated.asStateFlow()

    // Combined UI state - Split into multiple combines to avoid compiler limitation
    val uiState: StateFlow<CardUiState> = combine(
        combine(
            isConnected,
            isLoading,
            error,
            lastUpdated
        ) { connected, loading, error, updated ->
            NetworkState(connected, loading, error, updated)
        },
        combine(
            _cards,
            _isBiometricRequired,
            _isBiometricAuthenticated
        ) { cards, biometricRequired, biometricAuth ->
            CardState(cards, biometricRequired, biometricAuth)
        }
    ) { networkState, cardState ->
        CardUiState(
            cards = cardState.cards,
            isConnected = networkState.isConnected,
            isLoading = networkState.isLoading,
            error = networkState.error,
            lastUpdated = networkState.lastUpdated,
            requiresBiometric = cardState.requiresBiometric,
            isBiometricAuthenticated = cardState.isBiometricAuthenticated
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CardUiState()
    )

    init {
        // Cards require biometric authentication
        checkBiometricAuthentication()
    }

    /**
     * Check if biometric authentication is required
     */
    private fun checkBiometricAuthentication() {
        // In a real app, this would check with SecurityManager
        _isBiometricRequired.value = true
        _isBiometricAuthenticated.value = false
    }

    /**
     * Handle biometric authentication success
     */
    fun onBiometricAuthenticationSuccess() {
        _isBiometricAuthenticated.value = true
        _isBiometricRequired.value = false
        loadCards()
    }

    /**
     * Handle biometric authentication failure
     */
    fun onBiometricAuthenticationFailed() {
        _isBiometricAuthenticated.value = false
        _error.value = "Biometric authentication failed"
    }

    /**
     * Load cards (only after biometric authentication)
     */
    fun loadCards(forceRefresh: Boolean = false) {
        if (!_isBiometricAuthenticated.value) {
            _error.value = "Biometric authentication required"
            return
        }

        viewModelScope.launch {
            getCardDetailsUseCase(forceRefresh).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _cards.value = result.data
                        _lastUpdated.value = System.currentTimeMillis()
                        _isLoading.value = false
                    }
                    is Result.Error -> {
                        if (_cards.value.isEmpty()) {
                            _error.value = result.exception.message
                        }
                        _isLoading.value = false
                    }
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }

    /**
     * Toggle card status (enable/disable)
     */
    fun toggleCard(cardId: String, isActive: Boolean) {
        viewModelScope.launch {
            executeOperation(
                operation = {
                    // In real implementation, this would call repository
                    Result.Success(Unit)
                },
                onSuccess = {
                    // Update local card state
                    _cards.value = _cards.value.map { card ->
                        if (card.id == cardId) {
                            card.copy(isActive = isActive)
                        } else {
                            card
                        }
                    }
                }
            )
        }
    }

    /**
     * Refresh cards from network
     */
    override fun refresh() {
        if (_isBiometricAuthenticated.value) {
            loadCards(forceRefresh = true)
        }
    }

    override fun onNetworkStatusChanged(isConnected: Boolean) {
        super.onNetworkStatusChanged(isConnected)

        // Safe approach: only refresh if everything is properly initialized
        try {
            if (isConnected && _isBiometricAuthenticated.value) {
                refresh()
            }
        } catch (e: Exception) {
            // Ignore if not yet initialized
        }
    }

    /**
     * Reset authentication state (for security)
     */
    fun resetAuthentication() {
        _isBiometricAuthenticated.value = false
        _isBiometricRequired.value = true
        _cards.value = emptyList()
    }
}

// Rest of the code remains the same...

/**
 * Helper data classes for state combination
 */
private data class NetworkState(
    val isConnected: Boolean,
    val isLoading: Boolean,
    val error: String?,
    val lastUpdated: Long?
)

private data class CardState(
    val cards: List<PaymentCard>,
    val requiresBiometric: Boolean,
    val isBiometricAuthenticated: Boolean
)

/**
 * UI State for Card Details Screen
 */
data class CardUiState(
    val cards: List<PaymentCard> = emptyList(),
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long? = null,
    val requiresBiometric: Boolean = true,
    val isBiometricAuthenticated: Boolean = false
) {
    val showOfflineIndicator: Boolean
        get() = !isConnected && cards.isNotEmpty()

    val connectionStatusText: String
        get() = if (isConnected) "Online - Data is current" else "Offline - Showing cached data"

    val showCards: Boolean
        get() = isBiometricAuthenticated && !requiresBiometric

    val isEmpty: Boolean
        get() = cards.isEmpty() && !isLoading && isBiometricAuthenticated

    val activeCards: List<PaymentCard>
        get() = cards.filter { it.isActive }

    val inactiveCards: List<PaymentCard>
        get() = cards.filter { !it.isActive }
}