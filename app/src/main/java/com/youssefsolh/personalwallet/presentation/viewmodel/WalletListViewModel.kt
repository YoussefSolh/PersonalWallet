package com.youssefsolh.personalwallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youssefsolh.personalwallet.domain.model.Currency
import com.youssefsolh.personalwallet.domain.model.User
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.AuthRepository
import com.youssefsolh.personalwallet.domain.repository.CurrencyRepository
import com.youssefsolh.personalwallet.domain.usecase.GetAllWalletsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class WalletListViewModel @Inject constructor(
    private val getAllWalletsUseCase: GetAllWalletsUseCase,
    private val authRepository: AuthRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletListUiState())
    val uiState: StateFlow<WalletListUiState> = _uiState.asStateFlow()

    init {
        loadWallets()
        loadCurrentUser()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                combine(
                    getAllWalletsUseCase(),
                    currencyRepository.getAllCurrencies(),
                    currencyRepository.getDefaultCurrency()
                ) { wallets, currencies, defaultCurrency ->
                    Triple(wallets, currencies, defaultCurrency)
                }.collect { (wallets, currencies, defaultCurrency) ->
                    val totalBalance = calculateTotalBalance(wallets, currencies, defaultCurrency)
                    _uiState.value = _uiState.value.copy(
                        wallets = wallets,
                        isLoading = false,
                        totalBalance = totalBalance,
                        defaultCurrency = defaultCurrency
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

    /**
     * Calculate total balance by converting all wallet balances to the default currency.
     */
    private fun calculateTotalBalance(
        wallets: List<Wallet>,
        currencies: List<Currency>,
        defaultCurrency: Currency?
    ): BigDecimal {
        if (defaultCurrency == null) {
            // If no default currency, just sum up all balances (fallback)
            return wallets.sumOf { it.balance }
        }

        // Create a map for quick currency lookup
        val currencyMap = currencies.associateBy { it.code }

        return wallets.sumOf { wallet ->
            val walletCurrency = currencyMap[wallet.currency]
            if (walletCurrency == null || wallet.currency == defaultCurrency.code) {
                // Wallet is already in default currency or currency not found
                wallet.balance
            } else {
                // Convert wallet balance to default currency
                // Formula: balance * (1 / exchangeRateToDefault)
                // exchangeRateToDefault is how much of this currency = 1 default currency
                // So to convert to default: balance / exchangeRateToDefault
                wallet.balance.divide(
                    walletCurrency.exchangeRateToDefault,
                    2,
                    BigDecimal.ROUND_HALF_UP
                )
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }
    }

    fun refresh() {
        loadWallets()
    }
}

data class WalletListUiState(
    val wallets: List<Wallet> = emptyList(),
    val totalBalance: BigDecimal = BigDecimal.ZERO,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val defaultCurrency: Currency? = null
)