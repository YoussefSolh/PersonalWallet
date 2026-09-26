package com.youssefsolh.personalwallet.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.youssefsolh.personalwallet.data.local.dao.IncomeExpenseSummaryEntity
import com.youssefsolh.personalwallet.data.local.CurrentUserProvider
import com.youssefsolh.personalwallet.data.local.dao.TransactionDao
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for GetIncomeExpenseSummaryUseCase
 * Tests income/expense summary calculations
 */
class GetIncomeExpenseSummaryUseCaseTest {

    private lateinit var getIncomeExpenseSummaryUseCase: GetIncomeExpenseSummaryUseCase
    private val transactionDao: TransactionDao = mockk()
    private val currentUserProvider: CurrentUserProvider = mockk {
        coEvery { getCurrentUserId() } returns "user1"
    }

    @Before
    fun setup() {
        getIncomeExpenseSummaryUseCase = GetIncomeExpenseSummaryUseCase(transactionDao, currentUserProvider)
    }

    @Test
    fun `invoke should calculate summary with positive net income`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val period = "January 2024"
        val summaryEntity = IncomeExpenseSummaryEntity(
            totalIncome = 5000.0,
            totalExpense = 3000.0
        )

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", startDate, endDate)
        } returns summaryEntity

        // When
        val result = getIncomeExpenseSummaryUseCase(startDate, endDate, period)

        // Then
        assertThat(result.isSuccess).isTrue()
        val summary = result.getOrNull()!!
        assertThat(summary.totalIncome).isEqualTo(BigDecimal("5000.0"))
        assertThat(summary.totalExpense).isEqualTo(BigDecimal("3000.0"))
        assertThat(summary.netIncome).isEqualTo(BigDecimal("2000.0"))
        assertThat(summary.period).isEqualTo("January 2024")
    }

    @Test
    fun `invoke should calculate summary with negative net income`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val period = "February 2024"
        val summaryEntity = IncomeExpenseSummaryEntity(
            totalIncome = 2000.0,
            totalExpense = 3500.0
        )

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", startDate, endDate)
        } returns summaryEntity

        // When
        val result = getIncomeExpenseSummaryUseCase(startDate, endDate, period)

        // Then
        assertThat(result.isSuccess).isTrue()
        val summary = result.getOrNull()!!
        assertThat(summary.totalIncome).isEqualTo(BigDecimal("2000.0"))
        assertThat(summary.totalExpense).isEqualTo(BigDecimal("3500.0"))
        assertThat(summary.netIncome).isEqualTo(BigDecimal("-1500.0"))
    }

    @Test
    fun `invoke should handle zero income and zero expense`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val period = "March 2024"
        val summaryEntity = IncomeExpenseSummaryEntity(
            totalIncome = 0.0,
            totalExpense = 0.0
        )

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", startDate, endDate)
        } returns summaryEntity

        // When
        val result = getIncomeExpenseSummaryUseCase(startDate, endDate, period)

        // Then
        assertThat(result.isSuccess).isTrue()
        val summary = result.getOrNull()!!
        assertThat(summary.totalIncome).isEqualTo(BigDecimal.ZERO)
        assertThat(summary.totalExpense).isEqualTo(BigDecimal.ZERO)
        assertThat(summary.netIncome).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `invoke should handle only income no expenses`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val period = "April 2024"
        val summaryEntity = IncomeExpenseSummaryEntity(
            totalIncome = 5000.0,
            totalExpense = 0.0
        )

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", startDate, endDate)
        } returns summaryEntity

        // When
        val result = getIncomeExpenseSummaryUseCase(startDate, endDate, period)

        // Then
        assertThat(result.isSuccess).isTrue()
        val summary = result.getOrNull()!!
        assertThat(summary.totalIncome).isEqualTo(BigDecimal("5000.0"))
        assertThat(summary.totalExpense).isEqualTo(BigDecimal.ZERO)
        assertThat(summary.netIncome).isEqualTo(BigDecimal("5000.0"))
    }

    @Test
    fun `invoke should handle only expenses no income`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val period = "May 2024"
        val summaryEntity = IncomeExpenseSummaryEntity(
            totalIncome = 0.0,
            totalExpense = 2000.0
        )

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", startDate, endDate)
        } returns summaryEntity

        // When
        val result = getIncomeExpenseSummaryUseCase(startDate, endDate, period)

        // Then
        assertThat(result.isSuccess).isTrue()
        val summary = result.getOrNull()!!
        assertThat(summary.totalIncome).isEqualTo(BigDecimal.ZERO)
        assertThat(summary.totalExpense).isEqualTo(BigDecimal("2000.0"))
        assertThat(summary.netIncome).isEqualTo(BigDecimal("-2000.0"))
    }

    @Test
    fun `invoke should handle decimal amounts correctly`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val period = "June 2024"
        val summaryEntity = IncomeExpenseSummaryEntity(
            totalIncome = 1234.56,
            totalExpense = 789.12
        )

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", startDate, endDate)
        } returns summaryEntity

        // When
        val result = getIncomeExpenseSummaryUseCase(startDate, endDate, period)

        // Then
        assertThat(result.isSuccess).isTrue()
        val summary = result.getOrNull()!!
        assertThat(summary.totalIncome).isEqualTo(BigDecimal("1234.56"))
        assertThat(summary.totalExpense).isEqualTo(BigDecimal("789.12"))
        // Net income = 1234.56 - 789.12 = 445.44
        assertThat(summary.netIncome).isEqualTo(BigDecimal("445.44"))
    }

    @Test
    fun `invoke should handle large amounts`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val period = "July 2024"
        val summaryEntity = IncomeExpenseSummaryEntity(
            totalIncome = 999999.99,
            totalExpense = 500000.50
        )

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", startDate, endDate)
        } returns summaryEntity

        // When
        val result = getIncomeExpenseSummaryUseCase(startDate, endDate, period)

        // Then
        assertThat(result.isSuccess).isTrue()
        val summary = result.getOrNull()!!
        assertThat(summary.totalIncome).isEqualTo(BigDecimal("999999.99"))
        assertThat(summary.totalExpense).isEqualTo(BigDecimal("500000.50"))
        assertThat(summary.netIncome).isEqualTo(BigDecimal("499999.49"))
    }

    @Test
    fun `invoke should return failure when DAO throws exception`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val period = "August 2024"
        val exception = RuntimeException("Database error")

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", startDate, endDate)
        } throws exception

        // When
        val result = getIncomeExpenseSummaryUseCase(startDate, endDate, period)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `invoke should handle different periods`() = runTest {
        // Given
        val summaryEntity = IncomeExpenseSummaryEntity(
            totalIncome = 1000.0,
            totalExpense = 500.0
        )

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", any(), any())
        } returns summaryEntity

        // When - Test different period strings
        val result1 = getIncomeExpenseSummaryUseCase(0L, 1000L, "Daily")
        val result2 = getIncomeExpenseSummaryUseCase(0L, 1000L, "Weekly")
        val result3 = getIncomeExpenseSummaryUseCase(0L, 1000L, "Monthly")
        val result4 = getIncomeExpenseSummaryUseCase(0L, 1000L, "Yearly")

        // Then
        assertThat(result1.getOrNull()?.period).isEqualTo("Daily")
        assertThat(result2.getOrNull()?.period).isEqualTo("Weekly")
        assertThat(result3.getOrNull()?.period).isEqualTo("Monthly")
        assertThat(result4.getOrNull()?.period).isEqualTo("Yearly")
    }

    @Test
    fun `invoke should handle different date ranges`() = runTest {
        // Given
        val summaryEntity1 = IncomeExpenseSummaryEntity(1000.0, 500.0)
        val summaryEntity2 = IncomeExpenseSummaryEntity(2000.0, 1500.0)

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", 0L, 1000L)
        } returns summaryEntity1
        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", 5000L, 10000L)
        } returns summaryEntity2

        // When
        val result1 = getIncomeExpenseSummaryUseCase(0L, 1000L, "Period 1")
        val result2 = getIncomeExpenseSummaryUseCase(5000L, 10000L, "Period 2")

        // Then
        assertThat(result1.getOrNull()?.totalIncome).isEqualTo(BigDecimal("1000.0"))
        assertThat(result2.getOrNull()?.totalIncome).isEqualTo(BigDecimal("2000.0"))
    }

    @Test
    fun `invoke should calculate net income correctly when income equals expense`() = runTest {
        // Given
        val startDate = 0L
        val endDate = 1000L
        val period = "Break Even"
        val summaryEntity = IncomeExpenseSummaryEntity(
            totalIncome = 5000.0,
            totalExpense = 5000.0
        )

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", startDate, endDate)
        } returns summaryEntity

        // When
        val result = getIncomeExpenseSummaryUseCase(startDate, endDate, period)

        // Then
        assertThat(result.isSuccess).isTrue()
        val summary = result.getOrNull()!!
        assertThat(summary.netIncome).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `invoke should preserve period string exactly as provided`() = runTest {
        // Given
        val summaryEntity = IncomeExpenseSummaryEntity(1000.0, 500.0)
        val customPeriod = "Q1 2024 (January-March)"

        coEvery {
            transactionDao.getIncomeExpenseSummary("user1", any(), any())
        } returns summaryEntity

        // When
        val result = getIncomeExpenseSummaryUseCase(0L, 1000L, customPeriod)

        // Then
        assertThat(result.getOrNull()?.period).isEqualTo(customPeriod)
    }
}
