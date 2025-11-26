package com.youssefsolh.personalwallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youssefsolh.personalwallet.domain.model.CategorySpending
import com.youssefsolh.personalwallet.domain.model.IncomeExpenseSummary
import com.youssefsolh.personalwallet.domain.usecase.GetIncomeExpenseSummaryUseCase
import com.youssefsolh.personalwallet.domain.usecase.GetSpendingByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getSpendingByCategoryUseCase: GetSpendingByCategoryUseCase,
    private val getIncomeExpenseSummaryUseCase: GetIncomeExpenseSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadMonthlyReport()
    }

    fun loadMonthlyReport() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.timeInMillis

        loadReport(startDate, endDate, "This Month")
    }

    fun loadWeeklyReport() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.timeInMillis

        loadReport(startDate, endDate, "This Week")
    }

    fun loadYearlyReport() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.timeInMillis

        loadReport(startDate, endDate, "This Year")
    }

    private fun loadReport(startDate: Long, endDate: Long, period: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedPeriod = period
            )

            try {
                val spendingResult = getSpendingByCategoryUseCase(startDate, endDate)
                val summaryResult = getIncomeExpenseSummaryUseCase(startDate, endDate, period)

                _uiState.value = _uiState.value.copy(
                    categorySpending = spendingResult.getOrNull() ?: emptyList(),
                    summary = summaryResult.getOrNull(),
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load report"
                )
            }
        }
    }
}

data class ReportsUiState(
    val categorySpending: List<CategorySpending> = emptyList(),
    val summary: IncomeExpenseSummary? = null,
    val selectedPeriod: String = "This Month",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
