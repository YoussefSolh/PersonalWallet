package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.TransactionRunner
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val balanceUpdater: WalletBalanceUpdater,
    private val transactionRunner: TransactionRunner
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return try {
            transactionRunner.runInTransaction {
                // Reverse what is actually stored. Deleted rows are not returned, so a
                // repeated delete is a no-op instead of reversing the balance twice.
                val stored = transactionRepository.getTransactionById(transaction.id)
                    ?: return@runInTransaction
                balanceUpdater.reverse(stored)

                // Mark transaction as deleted (soft delete)
                transactionRepository.updateTransaction(
                    stored.copy(
                        isDeleted = true,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
