package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.WalletRepository
import java.math.BigDecimal
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return try {
            // Reverse the transaction's effect on wallet balances
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

            // Mark transaction as deleted (soft delete)
            transactionRepository.updateTransaction(
                transaction.copy(
                    isDeleted = true,
                    updatedAt = System.currentTimeMillis()
                )
            )

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
