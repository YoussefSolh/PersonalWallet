package com.youssefsolh.personalwallet.testutil

import com.youssefsolh.personalwallet.domain.model.Currency
import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.model.Wallet
import com.youssefsolh.personalwallet.domain.repository.CurrencyRepository
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.TransactionRunner
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal

/**
 * In-memory stand-in for the Room-backed repositories. [runner] snapshots state and
 * restores it if the block throws, mirroring a rolled-back database transaction.
 */
class InMemoryWalletStore {
    val wallets = linkedMapOf<String, Wallet>()
    val transactions = linkedMapOf<String, Transaction>()
    val currencies = linkedMapOf<String, Currency>()

    /** When set, the next wallet update throws (simulates a failure mid-transaction). */
    var failNextWalletUpdate = false

    /** When set to n > 0, the n-th wallet update from now throws. */
    var failWalletUpdateAfter = 0

    fun addWallet(id: String, balance: String, currency: String = "USD") {
        wallets[id] = Wallet(id = id, name = id, balance = BigDecimal(balance), currency = currency)
    }

    fun addCurrency(code: String, rateToDefault: String, isDefault: Boolean = false) {
        currencies[code] = Currency(
            code = code,
            name = code,
            symbol = code,
            exchangeRateToDefault = BigDecimal(rateToDefault),
            isDefault = isDefault
        )
    }

    fun balanceOf(walletId: String): BigDecimal = wallets.getValue(walletId).balance

    val runner = object : TransactionRunner {
        override suspend fun <R> runInTransaction(block: suspend () -> R): R {
            val walletSnapshot = LinkedHashMap(wallets)
            val transactionSnapshot = LinkedHashMap(transactions)
            try {
                return block()
            } catch (e: Throwable) {
                wallets.clear(); wallets.putAll(walletSnapshot)
                transactions.clear(); transactions.putAll(transactionSnapshot)
                throw e
            }
        }
    }

    val walletRepository = object : WalletRepository {
        override suspend fun getAllWallets(): Flow<List<Wallet>> = flowOf(wallets.values.filter { !it.isDeleted })
        override suspend fun getWalletById(id: String): Wallet? = wallets[id]?.takeIf { !it.isDeleted }
        override suspend fun insertWallet(wallet: Wallet) { wallets[wallet.id] = wallet }
        override suspend fun updateWallet(wallet: Wallet) {
            if (failWalletUpdateAfter > 0 && --failWalletUpdateAfter == 0) {
                failNextWalletUpdate = true
            }
            if (failNextWalletUpdate) {
                failNextWalletUpdate = false
                throw IllegalStateException("simulated failure")
            }
            wallets[wallet.id] = wallet
        }
        override suspend fun deleteWallet(id: String) {
            wallets[id]?.let { wallets[id] = it.copy(isDeleted = true) }
        }
        override suspend fun getWalletBalance(id: String): Flow<BigDecimal> = flowOf(balanceOf(id))
    }

    val transactionRepository = object : TransactionRepository {
        private fun active() = transactions.values.filter { !it.isDeleted }
        override suspend fun getAllTransactions(): Flow<List<Transaction>> = flowOf(active())
        override suspend fun getTransactionsByWallet(walletId: String): Flow<List<Transaction>> =
            flowOf(active().filter { it.fromWalletId == walletId || it.toWalletId == walletId })
        override suspend fun getTransactionsByWalletFiltered(
            walletId: String,
            type: TransactionType?,
            categoryId: String?,
            startDate: Long?,
            endDate: Long?,
            searchQuery: String?
        ): Flow<List<Transaction>> = getTransactionsByWallet(walletId)
        override suspend fun getTransactionById(id: String): Transaction? = transactions[id]?.takeIf { !it.isDeleted }
        override suspend fun insertTransaction(transaction: Transaction) { transactions[transaction.id] = transaction }
        override suspend fun updateTransaction(transaction: Transaction) { transactions[transaction.id] = transaction }
        override suspend fun deleteTransaction(id: String) {
            transactions[id]?.let { transactions[id] = it.copy(isDeleted = true) }
        }
        override suspend fun getDebtTransactions(): Flow<List<Transaction>> =
            flowOf(active().filter { it.isDebt && !it.debtSettled })
        override suspend fun settleDebt(debtTransactionId: String, settlementTransactionId: String) {
            transactions[debtTransactionId]?.let {
                transactions[debtTransactionId] = it.copy(debtSettled = true, relatedTransactionId = settlementTransactionId)
            }
        }
    }

    val currencyRepository = object : CurrencyRepository {
        override fun getAllCurrencies(): Flow<List<Currency>> = flowOf(currencies.values.toList())
        override fun getDefaultCurrency(): Flow<Currency?> = flowOf(currencies.values.firstOrNull { it.isDefault })
        override suspend fun getDefaultCurrencySync(): Currency? = currencies.values.firstOrNull { it.isDefault }
        override suspend fun getCurrencyByCode(code: String): Currency? = currencies[code]
        override suspend fun insertCurrency(currency: Currency) { currencies[currency.code] = currency }
        override suspend fun updateCurrency(currency: Currency) { currencies[currency.code] = currency }
        override suspend fun deleteCurrency(code: String): Result<Unit> {
            currencies.remove(code)
            return Result.success(Unit)
        }
    }
}
