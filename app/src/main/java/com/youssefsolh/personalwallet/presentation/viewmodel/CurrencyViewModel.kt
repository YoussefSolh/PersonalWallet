package com.youssefsolh.personalwallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youssefsolh.personalwallet.domain.model.Currency
import com.youssefsolh.personalwallet.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    init {
        loadCurrencies()
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            try {
                combine(
                    currencyRepository.getAllCurrencies(),
                    currencyRepository.getDefaultCurrency()
                ) { currencies, defaultCurrency ->
                    currencies to defaultCurrency
                }.collect { (currencies, defaultCurrency) ->
                    _uiState.value = _uiState.value.copy(
                        currencies = currencies,
                        defaultCurrency = defaultCurrency,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load currencies"
                )
            }
        }
    }

    fun loadCurrency(code: String) {
        viewModelScope.launch {
            val currency = currencyRepository.getCurrencyByCode(code)
            _uiState.value = if (currency != null) {
                _uiState.value.copy(loadedCurrency = currency, isEditMode = true)
            } else {
                _uiState.value.copy(error = "Currency not found")
            }
        }
    }

    fun saveCurrency(code: String, name: String, symbol: String, rateText: String) {
        val normalizedCode = code.trim().uppercase()
        val rate = rateText.trim().toBigDecimalOrNull()
        val validationError = when {
            !normalizedCode.matches(Regex("[A-Z]{3}")) -> "Code must be 3 letters (ISO 4217)"
            name.isBlank() -> "Name is required"
            symbol.isBlank() -> "Symbol is required"
            rate == null || rate <= BigDecimal.ZERO -> "Exchange rate must be a positive number"
            else -> null
        }
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(error = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val now = System.currentTimeMillis()
                val existing = _uiState.value.loadedCurrency
                if (_uiState.value.isEditMode && existing != null) {
                    currencyRepository.updateCurrency(
                        existing.copy(
                            name = name.trim(),
                            symbol = symbol.trim(),
                            // The default currency's rate is 1 by definition
                            exchangeRateToDefault = if (existing.isDefault) BigDecimal.ONE else rate!!,
                            isManualRate = true,
                            updatedAt = now,
                            lastRateUpdate = now
                        )
                    )
                } else {
                    if (currencyRepository.getCurrencyByCode(normalizedCode) != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "$normalizedCode already exists"
                        )
                        return@launch
                    }
                    currencyRepository.insertCurrency(
                        Currency(
                            code = normalizedCode,
                            name = name.trim(),
                            symbol = symbol.trim(),
                            exchangeRateToDefault = rate!!,
                            isManualRate = true,
                            createdAt = now,
                            updatedAt = now,
                            lastRateUpdate = now
                        )
                    )
                }
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save currency"
                )
            }
        }
    }

    fun deleteCurrency(code: String) {
        viewModelScope.launch {
            currencyRepository.deleteCurrency(code).onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CurrencyUiState(
    val currencies: List<Currency> = emptyList(),
    val defaultCurrency: Currency? = null,
    val loadedCurrency: Currency? = null,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val error: String? = null
)
