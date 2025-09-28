package com.example.bankingapp.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.bankingapp.domain.model.Transaction
import com.example.bankingapp.domain.usecase.GetTransactionHistoryUseCase
import com.example.bankingapp.utils.NetworkManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import com.example.bankingapp.domain.model.Result

/**
 * ViewModel for Transaction History Screen
 */
class TransactionViewModel(
    private val getTransactionHistoryUseCase: GetTransactionHistoryUseCase,
    networkManager: NetworkManager
) : BaseViewModel(networkManager) {

    // Transaction data
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // Pagination state
    private val _currentPage = MutableStateFlow(0)
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    // Transaction summary
    val transactionSummary: StateFlow<TransactionSummary> = _transactions.map { transactions ->
        calculateSummary(transactions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionSummary()
    )

//    // Combined UI state
//    val uiState: StateFlow<TransactionUiState> = combine(
//        isConnected,
//        isLoading,
//        error,
//        lastUpdated,
//        _transactions,
//        transactionSummary,
//        _hasMoreData
//    ) { connected: Boolean, loading: Boolean, error: String?, updated: Long?, transactions: List<Transaction>, summary: TransactionSummary, hasMore: Boolean ->
//        TransactionUiState(
//            transactions = transactions,
//            summary = summary,
//            isConnected = connected,
//            isLoading = loading,
//            error = error,
//            lastUpdated = updated,
//            hasMoreData = hasMore
//        )
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = TransactionUiState()
//    )

    init {
        loadTransactions()
    }

    /**
     * Load transactions (offline-first)
     */
    fun loadTransactions(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val page = if (forceRefresh) 0 else _currentPage.value

            getTransactionHistoryUseCase(
                page = page,
                pageSize = 20,
                forceRefresh = forceRefresh
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        if (forceRefresh || page == 0) {
                            _transactions.value = result.data
                        } else {
                            _transactions.value = _transactions.value + result.data
                        }

                        _hasMoreData.value = result.data.size == 20
                        _currentPage.value = page
                        _lastUpdated.value = System.currentTimeMillis()
                    }
                    is Result.Error -> {
                        if (_transactions.value.isEmpty()) {
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
     * Load more transactions (pagination)
     */
    fun loadMoreTransactions() {
        if (_hasMoreData.value && !_isLoading.value) {
            _currentPage.value += 1
            loadTransactions()
        }
    }

    /**
     * Refresh transactions from network
     */
    override fun refresh() {
        _currentPage.value = 0
        loadTransactions(forceRefresh = true)
    }

    override fun onNetworkStatusChanged(isConnected: Boolean) {
        super.onNetworkStatusChanged(isConnected)
        if (isConnected) {
            refresh()
        }
    }

    /**
     * Calculate transaction summary
     */
    private fun calculateSummary(transactions: List<Transaction>): TransactionSummary {
        var totalSpent = BigDecimal.ZERO
        var totalReceived = BigDecimal.ZERO

        transactions.forEach { transaction ->
            if (transaction.amount.amount < BigDecimal.ZERO) {
                totalSpent = totalSpent.add(transaction.amount.amount.abs())
            } else {
                totalReceived = totalReceived.add(transaction.amount.amount)
            }
        }

        return TransactionSummary(
            totalSpent = totalSpent,
            totalReceived = totalReceived,
            transactionCount = transactions.size
        )
    }
}

/**
 * Transaction summary data
 */
data class TransactionSummary(
    val totalSpent: BigDecimal = BigDecimal.ZERO,
    val totalReceived: BigDecimal = BigDecimal.ZERO,
    val transactionCount: Int = 0
) {
    val totalSpentFormatted: String
        get() = String.format("%.2f", totalSpent)

    val totalReceivedFormatted: String
        get() = String.format("%.2f", totalReceived)
}

/**
 * UI State for Transaction History Screen
 */
data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val summary: TransactionSummary = TransactionSummary(),
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long? = null,
    val hasMoreData: Boolean = true
) {
    val showOfflineIndicator: Boolean
        get() = !isConnected && transactions.isNotEmpty()

    val connectionStatusText: String
        get() = if (isConnected) "Online - Data is current" else "Offline - Showing cached data"

    val isEmpty: Boolean
        get() = transactions.isEmpty() && !isLoading
}