package com.youssefsolh.personalwallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youssefsolh.personalwallet.domain.model.Currency
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.CurrencyRepository
import com.youssefsolh.personalwallet.domain.usecase.AddTransactionUseCase
import com.youssefsolh.personalwallet.domain.usecase.GetAllWalletsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getAllWalletsUseCase: GetAllWalletsUseCase,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                getAllWalletsUseCase(),
                currencyRepository.getAllCurrencies()
            ) { wallets, currencies ->
                Pair(wallets, currencies)
            }.collect { (wallets, currencies) ->
                _uiState.value = _uiState.value.copy(
                    wallets = wallets,
                    currencies = currencies
                )
                // Recalculate converted amount when currencies are loaded
                calculateConvertedAmount()
            }
        }
    }

    /**
     * Calculate the converted amount for the destination wallet based on exchange rates.
     */
    private fun calculateConvertedAmount() {
        val state = _uiState.value
        val fromWallet = state.fromWallet
        val toWallet = state.toWallet
        val amount = state.amount.toBigDecimalOrNull()

        if (fromWallet == null || toWallet == null || amount == null || amount <= BigDecimal.ZERO) {
            _uiState.value = state.copy(convertedAmount = null)
            return
        }

        // If same currency, no conversion needed
        if (fromWallet.currency == toWallet.currency) {
            _uiState.value = state.copy(convertedAmount = null)
            return
        }

        // Find currencies
        val fromCurrency = state.currencies.find { it.code == fromWallet.currency }
        val toCurrency = state.currencies.find { it.code == toWallet.currency }

        if (fromCurrency == null || toCurrency == null) {
            _uiState.value = state.copy(convertedAmount = null)
            return
        }

        // Convert: amount in fromCurrency -> default currency -> toCurrency
        // Step 1: Convert from source to default currency
        val amountInDefault = amount.divide(
            fromCurrency.exchangeRateToDefault,
            10,
            BigDecimal.ROUND_HALF_UP
        )

        // Step 2: Convert from default currency to destination
        val convertedAmount = amountInDefault.multiply(toCurrency.exchangeRateToDefault)
            .setScale(2, BigDecimal.ROUND_HALF_UP)

        _uiState.value = state.copy(convertedAmount = convertedAmount)
    }

    fun onFromWalletSelected(wallet: Wallet) {
        _uiState.value = _uiState.value.copy(
            fromWallet = wallet,
            errorMessage = null
        )
        calculateConvertedAmount()
    }

    fun onToWalletSelected(wallet: Wallet) {
        _uiState.value = _uiState.value.copy(
            toWallet = wallet,
            errorMessage = null
        )
        calculateConvertedAmount()
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(
            amount = amount,
            errorMessage = null
        )
        calculateConvertedAmount()
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description,
            errorMessage = null
        )
    }

    fun onDebtToggled(isDebt: Boolean) {
        _uiState.value = _uiState.value.copy(isDebt = isDebt)
    }

    fun onTransfer(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validation
        if (state.fromWallet == null) {
            _uiState.value = state.copy(errorMessage = "Please select source wallet")
            return
        }

        if (state.toWallet == null) {
            _uiState.value = state.copy(errorMessage = "Please select destination wallet")
            return
        }

        if (state.fromWallet.id == state.toWallet.id) {
            _uiState.value = state.copy(errorMessage = "Source and destination must be different")
            return
        }

        val amount = state.amount.toBigDecimalOrNull()
        if (amount == null || amount <= BigDecimal.ZERO) {
            _uiState.value = state.copy(errorMessage = "Please enter valid amount")
            return
        }

        if (amount > state.fromWallet.balance && !state.isDebt) {
            _uiState.value = state.copy(errorMessage = "Insufficient balance")
            return
        }

        if (state.description.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter description")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                type = TransactionType.TRANSFER,
                description = state.description,
                categoryId = "transfer", // Default transfer category
                fromWalletId = state.fromWallet.id,
                toWalletId = state.toWallet.id,
                isDebt = state.isDebt,
                debtSettled = false
            )

            val result = addTransactionUseCase(transaction)

            if (result.isSuccess) {
                _uiState.value = TransferUiState()
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Transfer failed"
                )
            }
        }
    }
}

data class TransferUiState(
    val wallets: List<Wallet> = emptyList(),
    val currencies: List<Currency> = emptyList(),
    val fromWallet: Wallet? = null,
    val toWallet: Wallet? = null,
    val amount: String = "",
    val description: String = "",
    val isDebt: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val convertedAmount: BigDecimal? = null
)
