package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.TransactionRunner
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val balanceUpdater: WalletBalanceUpdater,
    private val transactionRunner: TransactionRunner
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return try {
            transactionRunner.runInTransaction {
                val toSave = balanceUpdater.withDestinationAmount(transaction)
                transactionRepository.insertTransaction(toSave)
                balanceUpdater.apply(toSave)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
