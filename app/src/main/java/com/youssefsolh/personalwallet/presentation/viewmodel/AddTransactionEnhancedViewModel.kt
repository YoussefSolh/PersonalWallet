package com.youssefsolh.personalwallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.usecase.AddTransactionUseCase
import com.youssefsolh.personalwallet.domain.usecase.GetAllWalletsUseCase
import com.youssefsolh.personalwallet.domain.usecase.GetCategoriesUseCase
import com.youssefsolh.personalwallet.domain.usecase.GetTransactionByIdUseCase
import com.youssefsolh.personalwallet.domain.usecase.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddTransactionEnhancedViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getAllWalletsUseCase: GetAllWalletsUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionEnhancedUiState())
    val uiState: StateFlow<AddTransactionEnhancedUiState> = _uiState.asStateFlow()

    fun loadData(currentWalletId: String, transactionType: TransactionType) {
        viewModelScope.launch {
            try {
                // Load categories
                if (transactionType != TransactionType.TRANSFER) {
                    getCategoriesUseCase(transactionType).collect { categories ->
                        _uiState.value = _uiState.value.copy(categories = categories)
                    }
                }

                // Load other wallets for transfer
                if (transactionType == TransactionType.TRANSFER) {
                    getAllWalletsUseCase().collect { allWallets ->
                        val otherWallets = allWallets.filter { it.id != currentWalletId }
                        _uiState.value = _uiState.value.copy(otherWallets = otherWallets)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun addTransaction(
        fromWalletId: String,
        toWalletId: String?,
        amount: Double,
        description: String,
        type: TransactionType,
        categoryId: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = BigDecimal.valueOf(amount),
                    type = type,
                    description = description,
                    categoryId = categoryId,
                    fromWalletId = when (type) {
                        TransactionType.EXPENSE, TransactionType.TRANSFER -> fromWalletId
                        else -> null
                    },
                    toWalletId = when (type) {
                        TransactionType.INCOME -> fromWalletId
                        TransactionType.TRANSFER -> toWalletId
                        else -> null
                    },
                    timestamp = timestamp,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val result = addTransactionUseCase(transaction)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to add transaction"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to add transaction"
                )
            }
        }
    }

    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val transaction = getTransactionByIdUseCase(transactionId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    transaction = transaction
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load transaction"
                )
            }
        }
    }

    fun updateTransaction(
        transactionId: String,
        fromWalletId: String,
        toWalletId: String?,
        amount: Double,
        description: String,
        type: TransactionType,
        categoryId: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val oldTransaction = _uiState.value.transaction
                if (oldTransaction == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Original transaction not found"
                    )
                    return@launch
                }

                val newTransaction = Transaction(
                    id = transactionId,
                    amount = BigDecimal.valueOf(amount),
                    type = type,
                    description = description,
                    categoryId = categoryId,
                    fromWalletId = when (type) {
                        TransactionType.EXPENSE, TransactionType.TRANSFER -> fromWalletId
                        else -> null
                    },
                    toWalletId = when (type) {
                        TransactionType.INCOME -> fromWalletId
                        TransactionType.TRANSFER -> toWalletId
                        else -> null
                    },
                    timestamp = timestamp,
                    createdAt = oldTransaction.createdAt,
                    updatedAt = System.currentTimeMillis()
                )

                val result = updateTransactionUseCase(oldTransaction, newTransaction)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to update transaction"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update transaction"
                )
            }
        }
    }
}

data class AddTransactionEnhancedUiState(
    val categories: List<Category> = emptyList(),
    val otherWallets: List<Wallet> = emptyList(),
    val transaction: Transaction? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
