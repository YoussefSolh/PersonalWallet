package com.youssefsolh.personalwallet.domain.model

import java.math.BigDecimal

data class CategorySpending(
    val categoryId: String,
    val categoryName: String,
    val totalAmount: BigDecimal,
    val transactionCount: Int,
    val percentage: Float = 0f
)

data class IncomeExpenseSummary(
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val netIncome: BigDecimal,
    val period: String
)
