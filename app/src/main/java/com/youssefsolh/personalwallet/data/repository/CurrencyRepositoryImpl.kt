package com.youssefsolh.personalwallet.data.repository

import com.youssefsolh.personalwallet.data.local.dao.CurrencyDao
import com.youssefsolh.personalwallet.data.local.entity.toDomain
import com.youssefsolh.personalwallet.data.local.entity.toEntity
import com.youssefsolh.personalwallet.domain.model.Currency
import com.youssefsolh.personalwallet.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val currencyDao: CurrencyDao
) : CurrencyRepository {

    override fun getAllCurrencies(): Flow<List<Currency>> {
        return currencyDao.getAllCurrencies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDefaultCurrency(): Flow<Currency?> {
        return currencyDao.getDefaultCurrency().map { it?.toDomain() }
    }

    override suspend fun getDefaultCurrencySync(): Currency? {
        return currencyDao.getDefaultCurrencySync()?.toDomain()
    }

    override suspend fun getCurrencyByCode(code: String): Currency? {
        return currencyDao.getCurrencyByCode(code)?.toDomain()
    }

    override suspend fun insertCurrency(currency: Currency) {
        currencyDao.insertCurrency(currency.toEntity())
    }

    override suspend fun updateCurrency(currency: Currency) {
        currencyDao.updateCurrency(currency.toEntity())
    }

    override suspend fun deleteCurrency(code: String): Result<Unit> {
        if (currencyDao.countWalletsUsingCurrency(code) > 0) {
            return Result.failure(IllegalStateException("$code is used by a wallet and cannot be deleted"))
        }
        return if (currencyDao.deleteCurrency(code) > 0) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("$code cannot be deleted"))
        }
    }
}
