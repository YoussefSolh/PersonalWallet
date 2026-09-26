package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.CurrencyRepository
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Applies or reverses a transaction's effect on wallet balances.
 *
 * Callers must run these inside a [com.youssefsolh.personalwallet.domain.repository.TransactionRunner]
 * so the read-modify-write of each balance is atomic with the transaction row change.
 */
class WalletBalanceUpdater @Inject constructor(
    private val walletRepository: WalletRepository,
    private val currencyRepository: CurrencyRepository
) {

    /**
     * Returns [transaction] with [Transaction.destinationAmount] filled in for transfers,
     * converting between the two wallets' currencies at the current rates.
     */
    suspend fun withDestinationAmount(transaction: Transaction): Transaction {
        if (transaction.type != TransactionType.TRANSFER || transaction.destinationAmount != null) {
            return transaction
        }
        val fromWallet = transaction.fromWalletId?.let { walletRepository.getWalletById(it) }
        val toWallet = transaction.toWalletId?.let { walletRepository.getWalletById(it) }
        if (fromWallet == null || toWallet == null || fromWallet.currency == toWallet.currency) {
            return transaction.copy(destinationAmount = transaction.amount)
        }
        val fromCurrency = currencyRepository.getCurrencyByCode(fromWallet.currency)
        val toCurrency = currencyRepository.getCurrencyByCode(toWallet.currency)
        val converted = if (fromCurrency != null && toCurrency != null) {
            toCurrency.fromDefault(fromCurrency.toDefault(transaction.amount))
                .setScale(2, RoundingMode.HALF_UP)
        } else {
            transaction.amount
        }
        return transaction.copy(destinationAmount = converted)
    }

    suspend fun apply(transaction: Transaction) = adjust(transaction, sign = BigDecimal.ONE)

    suspend fun reverse(transaction: Transaction) = adjust(transaction, sign = BigDecimal.ONE.negate())

    private suspend fun adjust(transaction: Transaction, sign: BigDecimal) {
        when (transaction.type) {
            TransactionType.INCOME -> {
                transaction.toWalletId?.let { addToBalance(it, transaction.amount * sign) }
            }
            TransactionType.EXPENSE -> {
                transaction.fromWalletId?.let { addToBalance(it, transaction.amount.negate() * sign) }
            }
            TransactionType.TRANSFER -> {
                transaction.fromWalletId?.let { addToBalance(it, transaction.amount.negate() * sign) }
                // Credit exactly what was credited originally so cross-currency
                // transfers reverse cleanly. Legacy rows without it fall back to amount.
                val credited = transaction.destinationAmount ?: transaction.amount
                transaction.toWalletId?.let { addToBalance(it, credited * sign) }
            }
        }
    }

    private suspend fun addToBalance(walletId: String, delta: BigDecimal) {
        val wallet = walletRepository.getWalletById(walletId) ?: return
        walletRepository.updateWallet(
            wallet.copy(
                balance = wallet.balance + delta,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
