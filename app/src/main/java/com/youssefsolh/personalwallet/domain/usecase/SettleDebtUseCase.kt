package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.domain.model.Transaction
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject

class SettleDebtUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) {
    suspend operator fun invoke(debtTransaction: Transaction): Result<Unit> {
        return try {
            // Create reverse transfer to settle the debt
            val settlementTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = debtTransaction.amount,
                type = TransactionType.TRANSFER,
                description = "Debt Settlement: ${debtTransaction.description}",
                categoryId = "settlement",
                fromWalletId = debtTransaction.toWalletId,
                toWalletId = debtTransaction.fromWalletId,
                isDebt = false,
                debtSettled = false,
                relatedTransactionId = debtTransaction.id
            )

            // Add the settlement transaction
            addTransactionUseCase(settlementTransaction)

            // Mark original debt as settled
            transactionRepository.settleDebt(
                debtTransactionId = debtTransaction.id,
                settlementTransactionId = settlementTransaction.id
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
