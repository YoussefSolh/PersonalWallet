package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.TransactionRunner
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val balanceUpdater: WalletBalanceUpdater,
    private val transactionRunner: TransactionRunner
) {
    suspend operator fun invoke(oldTransaction: Transaction, newTransaction: Transaction): Result<Unit> {
        return try {
            transactionRunner.runInTransaction {
                // Reverse what is actually stored, not what the UI last saw
                val stored = transactionRepository.getTransactionById(oldTransaction.id)
                    ?: throw IllegalStateException("Transaction ${oldTransaction.id} not found")
                balanceUpdater.reverse(stored)

                val keepsTransferLegs = newTransaction.type == TransactionType.TRANSFER &&
                    stored.type == TransactionType.TRANSFER &&
                    stored.amount.compareTo(newTransaction.amount) == 0 &&
                    stored.fromWalletId == newTransaction.fromWalletId &&
                    stored.toWalletId == newTransaction.toWalletId

                val updated = balanceUpdater.withDestinationAmount(
                    newTransaction.copy(
                        // Keep the original conversion when the transfer legs are unchanged;
                        // otherwise convert again at current rates.
                        destinationAmount = if (keepsTransferLegs) stored.destinationAmount else null,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                transactionRepository.updateTransaction(updated)
                balanceUpdater.apply(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
