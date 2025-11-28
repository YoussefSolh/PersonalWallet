package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.data.local.CurrentUserProvider
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.domain.model.IncomeExpenseSummary
import java.math.BigDecimal
import javax.inject.Inject

class GetIncomeExpenseSummaryUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val currentUserProvider: CurrentUserProvider
) {
    suspend operator fun invoke(startDate: Long, endDate: Long, period: String): Result<IncomeExpenseSummary> {
        return try {
            val userId = currentUserProvider.getCurrentUserId()
            val summary = transactionDao.getIncomeExpenseSummary(userId, startDate, endDate)

            val totalIncome = BigDecimal(summary.totalIncome.toString())
            val totalExpense = BigDecimal(summary.totalExpense.toString())

            val result = IncomeExpenseSummary(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netIncome = totalIncome - totalExpense,
                period = period
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
