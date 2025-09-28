package com.example.bankingapp.presentation.viewmodel


import androidx.lifecycle.viewModelScope
import com.example.bankingapp.domain.model.BankAccount
import com.example.bankingapp.domain.usecase.GetAccountBalanceUseCase
import com.example.bankingapp.domain.usecase.GetAccountDetailsUseCase
import com.example.bankingapp.domain.usecase.RefreshDataUseCase
import com.example.bankingapp.utils.NetworkManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.bankingapp.domain.model.Result

/**
 * ViewModel for Account Balance Screen
 */
class AccountViewModel(
    private val getAccountBalanceUseCase: GetAccountBalanceUseCase,
    private val getAccountDetailsUseCase: GetAccountDetailsUseCase,
    private val refreshDataUseCase: RefreshDataUseCase,
    networkManager: NetworkManager
) : BaseViewModel(networkManager) {

    // Account data
    private val _accountData = MutableStateFlow<BankAccount?>(null)
    val accountData: StateFlow<BankAccount?> = _accountData.asStateFlow()

    // Combined UI state
    val uiState: StateFlow<AccountUiState> = combine(
        isConnected,
        isLoading,
        error,
        lastUpdated,
        _accountData
    ) { connected, loading, error, updated, account ->
        AccountUiState(
            account = account,
            isConnected = connected,
            isLoading = loading,
            error = error,
            lastUpdated = updated
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountUiState()
    )

    init {
        loadAccountBalance()
    }

    /**
     * Load account balance (offline-first)
     */
    fun loadAccountBalance(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            getAccountBalanceUseCase(forceRefresh).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _accountData.value = result.data
                    }
                    is Result.Error -> {
                        // If we don't have cached data, show error
                        if (_accountData.value == null) {
                            _error.value = result.exception.message
                        }
                    }
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }

    /**
     * Load account details
     */
    fun loadAccountDetails() {
        viewModelScope.launch {
            getAccountDetailsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _accountData.value = result.data
                    }
                    is Result.Error -> {
                        if (_accountData.value == null) {
                            _error.value = result.exception.message
                        }
                    }
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }

    /**
     * Refresh all data from network
     */
    override fun refresh() {
        loadAccountBalance(forceRefresh = true)
    }

    /**
     * Refresh all app data
     */
    fun refreshAllData() {
        viewModelScope.launch {
            executeOperation(
                operation = { refreshDataUseCase() },
                onSuccess = {
                    // Reload account data after successful refresh
                    loadAccountBalance()
                }
            )
        }
    }

    override fun onNetworkStatusChanged(isConnected: Boolean) {
        super.onNetworkStatusChanged(isConnected)
        if (isConnected) {
            // Auto-refresh when coming back online
            refresh()
        }
    }
}

/**
 * UI State for Account Balance Screen
 */
data class AccountUiState(
    val account: BankAccount? = null,
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long? = null
) {
    val showOfflineIndicator: Boolean
        get() = !isConnected && account != null

    val connectionStatusText: String
        get() = if (isConnected) "Online - Data is current" else "Offline - Showing cached data"

    val balanceText: String
        get() = account?.balance?.amount?.toString() ?: "0.00"

    val currencyText: String
        get() = account?.currency?.code ?: "USD"

    val accountTypeText: String
        get() = when (account?.accountType?.name) {
            "CHECKING" -> "Checking Account"
            "SAVINGS" -> "Savings Account"
            "CREDIT" -> "Credit Account"
            "BUSINESS" -> "Business Account"
            else -> "Account"
        }

    val maskedAccountNumber: String
        get() = account?.accountNumber?.let {
            "**** **** **** ${it.takeLast(4)}"
        } ?: "**** **** **** ****"

    val isActive: Boolean
        get() = account?.isActive ?: false
}