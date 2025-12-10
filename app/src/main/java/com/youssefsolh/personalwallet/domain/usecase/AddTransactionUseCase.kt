package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.CurrencyRepository
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import java.math.BigDecimal
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val currencyRepository: CurrencyRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return try {
            transactionRepository.insertTransaction(transaction)

            when (transaction.type) {
                TransactionType.INCOME -> {
                    transaction.toWalletId?.let { walletId ->
                        updateWalletBalance(walletId, transaction.amount)
                    }
                }
                TransactionType.EXPENSE -> {
                    transaction.fromWalletId?.let { walletId ->
                        updateWalletBalance(walletId, transaction.amount.negate())
                    }
                }
                TransactionType.TRANSFER -> {
                    // Handle cross-currency transfers
                    val fromWallet = transaction.fromWalletId?.let { walletRepository.getWalletById(it) }
                    val toWallet = transaction.toWalletId?.let { walletRepository.getWalletById(it) }

                    if (fromWallet != null && toWallet != null) {
                        // Deduct from source wallet (always use transaction amount)
                        updateWalletBalance(fromWallet.id, transaction.amount.negate())

                        // Add to destination wallet (convert if currencies differ)
                        val amountToAdd = if (fromWallet.currency != toWallet.currency) {
                            // Calculate converted amount
                            val fromCurrency = currencyRepository.getCurrencyByCode(fromWallet.currency)
                            val toCurrency = currencyRepository.getCurrencyByCode(toWallet.currency)

                            if (fromCurrency != null && toCurrency != null) {
                                // Convert: source amount -> default currency -> destination currency
                                val amountInDefault = transaction.amount.divide(
                                    fromCurrency.exchangeRateToDefault,
                                    10,
                                    BigDecimal.ROUND_HALF_UP
                                )
                                amountInDefault.multiply(toCurrency.exchangeRateToDefault)
                                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                            } else {
                                // Fallback to same amount if currencies not found
                                transaction.amount
                            }
                        } else {
                            // Same currency, use same amount
                            transaction.amount
                        }
                        updateWalletBalance(toWallet.id, amountToAdd)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateWalletBalance(walletId: String, amount: BigDecimal) {
        val wallet = walletRepository.getWalletById(walletId)
        wallet?.let {
            val updatedWallet = it.copy(
                balance = it.balance + amount,
                updatedAt = System.currentTimeMillis()
            )
            walletRepository.updateWallet(updatedWallet)
        }
    }
}