package com.youssefsolh.personalwallet.presentation.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youssefsolh.personalwallet.data.util.CsvExporter
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import com.youssefsolh.personalwallet.domain.usecase.DeleteTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletDetailViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val csvExporter: CsvExporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletDetailUiState())
    val uiState: StateFlow<WalletDetailUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow<TransactionType?>(null)

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    uiState.value.wallet?.let { wallet ->
                        loadTransactions(wallet.id, query, _filterType.value)
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onFilterTypeChanged(type: TransactionType?) {
        _filterType.value = type
        _uiState.value = _uiState.value.copy(filterType = type)
        uiState.value.wallet?.let { wallet ->
            loadTransactions(wallet.id, _searchQuery.value, type)
        }
    }

    fun loadWallet(walletId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val wallet = walletRepository.getWalletById(walletId)

                if (wallet != null) {
                    _uiState.value = _uiState.value.copy(wallet = wallet)
                    loadTransactions(walletId, _searchQuery.value, _filterType.value)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Wallet not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun loadTransactions(walletId: String, searchQuery: String, filterType: TransactionType?) {
        viewModelScope.launch {
            try {
                transactionRepository.getTransactionsByWalletFiltered(
                    walletId = walletId,
                    type = filterType,
                    searchQuery = searchQuery.ifBlank { null }
                ).collect { transactions ->
                    // Load categories for each transaction
                    val transactionsWithCategories = transactions.map { transaction ->
                        val category = categoryRepository.getCategoryById(transaction.categoryId)
                        TransactionWithCategory(
                            transaction = transaction,
                            category = category,
                            currentWalletId = walletId
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        transactionsWithCategories = transactionsWithCategories,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction).onSuccess {
                // Reload wallet to get updated balance
                uiState.value.wallet?.let { loadWallet(it.id) }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message ?: "Failed to delete transaction"
                )
            }
        }
    }

    fun refresh() {
        uiState.value.wallet?.let { wallet ->
            loadWallet(wallet.id)
        }
    }

    fun exportToCsv(detailed: Boolean = false): Intent? {
        return try {
            val transactions = _uiState.value.transactionsWithCategories
            val walletName = _uiState.value.wallet?.name

            if (transactions.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    error = "No transactions to export"
                )
                return null
            }

            val result = if (detailed) {
                csvExporter.exportTransactionsToDetailedCsv(transactions, walletName)
            } else {
                csvExporter.exportTransactionsToCsv(transactions, walletName)
            }

            result.fold(
                onSuccess = { uri ->
                    _uiState.value = _uiState.value.copy(
                        exportSuccess = "CSV exported successfully"
                    )
                    csvExporter.createShareIntent(uri)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Export failed: ${error.message}"
                    )
                    null
                }
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Export failed: ${e.message}"
            )
            null
        }
    }

    fun clearExportSuccess() {
        _uiState.value = _uiState.value.copy(exportSuccess = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TransactionWithCategory(
    val transaction: Transaction,
    val category: Category?,
    val currentWalletId: String
) {
    // Helper to determine if amount should be positive or negative for display
    val displayAmount: String
        get() = when (transaction.type) {
            TransactionType.INCOME -> "+$${transaction.amount}"
            TransactionType.EXPENSE -> "-$${transaction.amount}"
            TransactionType.TRANSFER -> {
                // If current wallet is the source, show negative (money out)
                // If current wallet is the destination, show positive (money in)
                if (transaction.fromWalletId == currentWalletId) {
                    "-$${transaction.amount}"
                } else {
                    "+$${transaction.amount}"
                }
            }
        }

    val displayColor: Boolean
        get() = when (transaction.type) {
            TransactionType.INCOME -> true // positive color
            TransactionType.EXPENSE -> false // negative color
            TransactionType.TRANSFER -> transaction.toWalletId == currentWalletId // positive if receiving
        }
}

data class WalletDetailUiState(
    val wallet: Wallet? = null,
    val transactionsWithCategories: List<TransactionWithCategory> = emptyList(),
    val searchQuery: String = "",
    val filterType: TransactionType? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val exportSuccess: String? = null
)
