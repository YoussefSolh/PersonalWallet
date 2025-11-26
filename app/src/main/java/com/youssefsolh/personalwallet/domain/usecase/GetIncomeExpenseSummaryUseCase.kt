package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.domain.model.IncomeExpenseSummary
import java.math.BigDecimal
import javax.inject.Inject

class GetIncomeExpenseSummaryUseCase @Inject constructor(
    private val transactionDao: TransactionDao
) {
    suspend operator fun invoke(startDate: Long, endDate: Long, period: String): Result<IncomeExpenseSummary> {
        return try {
            val summary = transactionDao.getIncomeExpenseSummary(startDate, endDate)

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
