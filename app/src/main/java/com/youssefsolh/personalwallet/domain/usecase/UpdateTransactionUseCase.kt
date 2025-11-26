package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import java.math.BigDecimal
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(oldTransaction: Transaction, newTransaction: Transaction): Result<Unit> {
        return try {
            // First, reverse the old transaction's effect
            reverseTransactionEffect(oldTransaction)

            // Update the transaction
            transactionRepository.updateTransaction(newTransaction.copy(
                updatedAt = System.currentTimeMillis()
            ))

            // Apply the new transaction's effect
            applyTransactionEffect(newTransaction)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun reverseTransactionEffect(transaction: Transaction) {
        when (transaction.type) {
            TransactionType.INCOME -> {
                transaction.toWalletId?.let { walletId ->
                    updateWalletBalance(walletId, transaction.amount.negate())
                }
            }
            TransactionType.EXPENSE -> {
                transaction.fromWalletId?.let { walletId ->
                    updateWalletBalance(walletId, transaction.amount)
                }
            }
            TransactionType.TRANSFER -> {
                transaction.fromWalletId?.let { fromWalletId ->
                    updateWalletBalance(fromWalletId, transaction.amount)
                }
                transaction.toWalletId?.let { toWalletId ->
                    updateWalletBalance(toWalletId, transaction.amount.negate())
                }
            }
        }
    }

    private suspend fun applyTransactionEffect(transaction: Transaction) {
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
                transaction.fromWalletId?.let { fromWalletId ->
                    updateWalletBalance(fromWalletId, transaction.amount.negate())
                }
                transaction.toWalletId?.let { toWalletId ->
                    updateWalletBalance(toWalletId, transaction.amount)
                }
            }
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
