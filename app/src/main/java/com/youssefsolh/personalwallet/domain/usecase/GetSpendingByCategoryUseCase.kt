package com.youssefsolh.personalwallet.domain.usecase

import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.domain.model.CategorySpending
import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import javax.inject.Inject

class GetSpendingByCategoryUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(startDate: Long, endDate: Long): Result<List<CategorySpending>> {
        return try {
            val spendingData = transactionDao.getSpendingByCategory(startDate, endDate)
            val categories = categoryRepository.getAllCategories().first()

            val totalSpent = spendingData.sumOf { it.total }

            val categorySpending = spendingData.map { data ->
                val category = categories.find { it.id == data.categoryId }
                CategorySpending(
                    categoryId = data.categoryId,
                    categoryName = category?.name ?: "Unknown",
                    totalAmount = BigDecimal(data.total.toString()),
                    transactionCount = data.count,
                    percentage = if (totalSpent > 0) ((data.total / totalSpent) * 100).toFloat() else 0f
                )
            }

            Result.success(categorySpending)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
