package com.youssefsolh.personalwallet.domain.repository

import com.youssefsolh.personalwallet.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    fun getAllCurrencies(): Flow<List<Currency>>
    fun getDefaultCurrency(): Flow<Currency?>
    suspend fun getDefaultCurrencySync(): Currency?
    suspend fun getCurrencyByCode(code: String): Currency?
    suspend fun insertCurrency(currency: Currency)
    suspend fun updateCurrency(currency: Currency)

    /** Deletes a currency. Fails for the default/system currency or one still used by a wallet. */
    suspend fun deleteCurrency(code: String): Result<Unit>
}
