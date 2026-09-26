package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import com.youssefsolh.personalwallet.domain.repository.TransactionRunner
import java.util.UUID
import javax.inject.Inject

class SettleDebtUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val transactionRunner: TransactionRunner
) {
    suspend operator fun invoke(debtTransaction: Transaction): Result<Unit> {
        return try {
            transactionRunner.runInTransaction {
                // Create reverse transfer to settle the debt. Swapping the legs of the
                // original transfer returns exactly what was moved in each currency.
                val settlementTransaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = debtTransaction.destinationAmount ?: debtTransaction.amount,
                    destinationAmount = debtTransaction.amount,
                    type = TransactionType.TRANSFER,
                    description = "Debt Settlement: ${debtTransaction.description}",
                    categoryId = "settlement",
                    fromWalletId = debtTransaction.toWalletId,
                    toWalletId = debtTransaction.fromWalletId,
                    isDebt = false,
                    debtSettled = false,
                    relatedTransactionId = debtTransaction.id
                )

                // Only mark the debt settled if the settlement was recorded
                addTransactionUseCase(settlementTransaction).getOrThrow()

                transactionRepository.settleDebt(
                    debtTransactionId = debtTransaction.id,
                    settlementTransactionId = settlementTransaction.id
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
