package com.youssefsolh.personalwallet.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.data.local.dao.CategorySpendingEntity
import com.youssefsolh.personalwallet.data.local.CurrentUserProvider
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import com.youssefsolh.personalwallet.domain.model.Category
import com.youssefsolh.personalwallet.domain.model.TransactionType
import com.youssefsolh.personalwallet.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for GetSpendingByCategoryUseCase
 * Tests category spending analytics with percentage calculations
 */
class GetSpendingByCategoryUseCaseTest {

    private lateinit var getSpendingByCategoryUseCase: GetSpendingByCategoryUseCase
    private val transactionDao: TransactionDao = mockk()
    private val currentUserProvider: CurrentUserProvider = mockk {
        coEvery { getCurrentUserId() } returns "user1"
    }
    private val categoryRepository: CategoryRepository = mockk()

    private val categories = listOf(
        Category("cat1", "Food", "🍔", "#FF0000", TransactionType.EXPENSE),
        Category("cat2", "Transport", "🚗", "#00FF00", TransactionType.EXPENSE),
        Category("cat3", "Shopping", "🛍️", "#0000FF", TransactionType.EXPENSE)
    )

    @Before
    fun setup() {
        getSpendingByCategoryUseCase = GetSpendingByCategoryUseCase(
            transactionDao = transactionDao,
            categoryRepository = categoryRepository,
            currentUserProvider = currentUserProvider
        )
    }

    @Test
    fun `invoke should calculate category spending with correct percentages`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val spendingData = listOf(
            CategorySpendingEntity("cat1", 500.0, 10),
            CategorySpendingEntity("cat2", 300.0, 5),
            CategorySpendingEntity("cat3", 200.0, 3)
        )
        // Total = 1000, percentages: 50%, 30%, 20%

        coEvery { transactionDao.getSpendingByCategory("user1", startDate, endDate) } returns spendingData
        coEvery { categoryRepository.getAllCategories() } returns flowOf(categories)

        // When
        val result = getSpendingByCategoryUseCase(startDate, endDate)

        // Then
        assertThat(result.isSuccess).isTrue()
        val spending = result.getOrNull()!!
        assertThat(spending).hasSize(3)

        // Check first category
        assertThat(spending[0].categoryId).isEqualTo("cat1")
        assertThat(spending[0].categoryName).isEqualTo("Food")
        assertThat(spending[0].totalAmount).isEqualTo(BigDecimal("500.0"))
        assertThat(spending[0].transactionCount).isEqualTo(10)
        assertThat(spending[0].percentage).isWithin(0.01f).of(50f)

        // Check second category
        assertThat(spending[1].categoryId).isEqualTo("cat2")
        assertThat(spending[1].categoryName).isEqualTo("Transport")
        assertThat(spending[1].totalAmount).isEqualTo(BigDecimal("300.0"))
        assertThat(spending[1].transactionCount).isEqualTo(5)
        assertThat(spending[1].percentage).isWithin(0.01f).of(30f)

        // Check third category
        assertThat(spending[2].categoryId).isEqualTo("cat3")
        assertThat(spending[2].categoryName).isEqualTo("Shopping")
        assertThat(spending[2].totalAmount).isEqualTo(BigDecimal("200.0"))
        assertThat(spending[2].transactionCount).isEqualTo(3)
        assertThat(spending[2].percentage).isWithin(0.01f).of(20f)
    }

    @Test
    fun `invoke should handle unknown category`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val spendingData = listOf(
            CategorySpendingEntity("unknown", 100.0, 1)
        )

        coEvery { transactionDao.getSpendingByCategory("user1", startDate, endDate) } returns spendingData
        coEvery { categoryRepository.getAllCategories() } returns flowOf(categories)

        // When
        val result = getSpendingByCategoryUseCase(startDate, endDate)

        // Then
        assertThat(result.isSuccess).isTrue()
        val spending = result.getOrNull()!!
        assertThat(spending).hasSize(1)
        assertThat(spending[0].categoryName).isEqualTo("Unknown")
        assertThat(spending[0].percentage).isEqualTo(100f)
    }

    @Test
    fun `invoke should return empty list when no spending data`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L

        coEvery { transactionDao.getSpendingByCategory("user1", startDate, endDate) } returns emptyList()
        coEvery { categoryRepository.getAllCategories() } returns flowOf(categories)

        // When
        val result = getSpendingByCategoryUseCase(startDate, endDate)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()
    }

    @Test
    fun `invoke should handle zero total spending`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val spendingData = listOf(
            CategorySpendingEntity("cat1", 0.0, 0)
        )

        coEvery { transactionDao.getSpendingByCategory("user1", startDate, endDate) } returns spendingData
        coEvery { categoryRepository.getAllCategories() } returns flowOf(categories)

        // When
        val result = getSpendingByCategoryUseCase(startDate, endDate)

        // Then
        assertThat(result.isSuccess).isTrue()
        val spending = result.getOrNull()!!
        assertThat(spending).hasSize(1)
        assertThat(spending[0].percentage).isEqualTo(0f)
    }

    @Test
    fun `invoke should handle single category with 100 percent`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val spendingData = listOf(
            CategorySpendingEntity("cat1", 500.0, 10)
        )

        coEvery { transactionDao.getSpendingByCategory("user1", startDate, endDate) } returns spendingData
        coEvery { categoryRepository.getAllCategories() } returns flowOf(categories)

        // When
        val result = getSpendingByCategoryUseCase(startDate, endDate)

        // Then
        assertThat(result.isSuccess).isTrue()
        val spending = result.getOrNull()!!
        assertThat(spending).hasSize(1)
        assertThat(spending[0].percentage).isWithin(0.01f).of(100f)
    }

    @Test
    fun `invoke should handle decimal amounts correctly`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val spendingData = listOf(
            CategorySpendingEntity("cat1", 123.45, 1),
            CategorySpendingEntity("cat2", 678.55, 2)
        )
        // Total = 802.0

        coEvery { transactionDao.getSpendingByCategory("user1", startDate, endDate) } returns spendingData
        coEvery { categoryRepository.getAllCategories() } returns flowOf(categories)

        // When
        val result = getSpendingByCategoryUseCase(startDate, endDate)

        // Then
        assertThat(result.isSuccess).isTrue()
        val spending = result.getOrNull()!!
        assertThat(spending[0].totalAmount).isEqualTo(BigDecimal("123.45"))
        assertThat(spending[1].totalAmount).isEqualTo(BigDecimal("678.55"))
    }

    @Test
    fun `invoke should calculate percentages that sum to 100`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val spendingData = listOf(
            CategorySpendingEntity("cat1", 333.33, 1),
            CategorySpendingEntity("cat2", 333.33, 1),
            CategorySpendingEntity("cat3", 333.34, 1)
        )
        // Total = 1000.0

        coEvery { transactionDao.getSpendingByCategory("user1", startDate, endDate) } returns spendingData
        coEvery { categoryRepository.getAllCategories() } returns flowOf(categories)

        // When
        val result = getSpendingByCategoryUseCase(startDate, endDate)

        // Then
        assertThat(result.isSuccess).isTrue()
        val spending = result.getOrNull()!!
        val totalPercentage = spending.sumOf { it.percentage.toDouble() }
        assertThat(totalPercentage).isWithin(0.1).of(100.0)
    }

    @Test
    fun `invoke should return failure when DAO throws exception`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val exception = RuntimeException("Database error")

        coEvery { transactionDao.getSpendingByCategory("user1", startDate, endDate) } throws exception

        // When
        val result = getSpendingByCategoryUseCase(startDate, endDate)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should return failure when category repository throws exception`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val spendingData = listOf(CategorySpendingEntity("cat1", 100.0, 1))
        val exception = RuntimeException("Repository error")

        coEvery { transactionDao.getSpendingByCategory("user1", startDate, endDate) } returns spendingData
        coEvery { categoryRepository.getAllCategories() } throws exception

        // When
        val result = getSpendingByCategoryUseCase(startDate, endDate)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should handle large transaction counts`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val spendingData = listOf(
            CategorySpendingEntity("cat1", 1000.0, 500),
            CategorySpendingEntity("cat2", 500.0, 1000)
        )

        coEvery { transactionDao.getSpendingByCategory("user1", startDate, endDate) } returns spendingData
        coEvery { categoryRepository.getAllCategories() } returns flowOf(categories)

        // When
        val result = getSpendingByCategoryUseCase(startDate, endDate)

        // Then
        assertThat(result.isSuccess).isTrue()
        val spending = result.getOrNull()!!
        assertThat(spending[0].transactionCount).isEqualTo(500)
        assertThat(spending[1].transactionCount).isEqualTo(1000)
    }

    @Test
    fun `invoke should handle different date ranges`() = runTest {
        // Given - Different date ranges
        val startDate1 = 0L
        val endDate1 = 1000L
        val startDate2 = 5000L
        val endDate2 = 10000L

        val spendingData1 = listOf(CategorySpendingEntity("cat1", 100.0, 1))
        val spendingData2 = listOf(CategorySpendingEntity("cat2", 200.0, 2))

        coEvery { transactionDao.getSpendingByCategory("user1", startDate1, endDate1) } returns spendingData1
        coEvery { transactionDao.getSpendingByCategory("user1", startDate2, endDate2) } returns spendingData2
        coEvery { categoryRepository.getAllCategories() } returns flowOf(categories)

        // When
        val result1 = getSpendingByCategoryUseCase(startDate1, endDate1)
        val result2 = getSpendingByCategoryUseCase(startDate2, endDate2)

        // Then
        assertThat(result1.getOrNull()!![0].categoryId).isEqualTo("cat1")
        assertThat(result2.getOrNull()!![0].categoryId).isEqualTo("cat2")
    }
}
