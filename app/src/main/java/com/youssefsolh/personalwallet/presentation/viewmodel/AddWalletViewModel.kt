package com.youssefsolh.personalwallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.usecase.CreateWalletUseCase
import com.youssefsolh.personalwallet.domain.usecase.GetWalletByIdUseCase
import com.youssefsolh.personalwallet.domain.usecase.UpdateWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class AddWalletViewModel @Inject constructor(
    private val createWalletUseCase: CreateWalletUseCase,
    private val getWalletByIdUseCase: GetWalletByIdUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWalletUiState())
    val uiState: StateFlow<AddWalletUiState> = _uiState.asStateFlow()

    fun loadWallet(walletId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val wallet = getWalletByIdUseCase(walletId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    wallet = wallet
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load wallet"
                )
            }
        }
    }

    fun createWallet(name: String, initialBalance: Double, currency: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                createWalletUseCase(
                    name = name,
                    initialBalance = BigDecimal.valueOf(initialBalance),
                    currency = currency
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create wallet"
                )
            }
        }
    }

    fun updateWallet(id: String, name: String, balance: Double, currency: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val wallet = Wallet(
                    id = id,
                    name = name,
                    balance = BigDecimal.valueOf(balance),
                    currency = currency,
                    createdAt = System.currentTimeMillis() // Will be preserved by Room
                )
                updateWalletUseCase(wallet)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update wallet"
                )
            }
        }
    }
}

data class AddWalletUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val wallet: Wallet? = null
)
