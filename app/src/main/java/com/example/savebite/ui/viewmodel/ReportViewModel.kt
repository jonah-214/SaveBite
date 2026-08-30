package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.ReportRepository
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*

enum class TimeFrame { MONTHLY, WEEKLY }

data class CategoryBreakdown(
    val category: String,
    val count: Int,
    val percentage: Float,
    val totalPrice: Double = 0.0
)

data class TopWastedItem(
    val name: String,
    val count: Int,
    val percentage: Float,
    val totalPrice: Double = 0.0
)

data class ReasonBreakdown(
    val reason: String,
    val count: Int,
    val percentage: Float
)

data class ReportUiState(
    val selectedTimeFrame: TimeFrame = TimeFrame.MONTHLY,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedWeek: Int = 1,

    // Wasted Stats
    val totalWastedItems: Int = 0,
    val totalWastedCost: Double = 0.0,
    val mostWastedName: String = "-",
    val mostWastedCount: Int = 0,
    val wastedBreakdowns: List<CategoryBreakdown> = emptyList(),
    val topWastedItems: List<TopWastedItem> = emptyList(),
    val reasonBreakdowns: List<ReasonBreakdown> = emptyList(),

    // Consumed Stats
    val totalConsumedItems: Int = 0,
    val totalSavedCost: Double = 0.0,
    val consumedBreakdowns: List<CategoryBreakdown> = emptyList(),
    val topConsumedItems: List<TopWastedItem> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    private val _filterParams = MutableStateFlow(
        Triple(
            TimeFrame.MONTHLY,
            Pair(Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR)),
            1
        )
    )

    val uiState: StateFlow<ReportUiState> = _filterParams
        .flatMapLatest { (timeFrame, monthYear, week) ->
            val range = if (timeFrame == TimeFrame.WEEKLY) {
                getWeekRange(monthYear.second, monthYear.first, week)
            } else {
                getMonthRange(monthYear.first, monthYear.second)
            }
            repository.getReportItemsInRange(range.first, range.second).map { items ->
                calculateReport(items, timeFrame, monthYear.first, monthYear.second, week)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportUiState())

    fun setTimeFrame(timeFrame: TimeFrame) {
        val current = _filterParams.value
        _filterParams.value = Triple(timeFrame, current.second, current.third)
    }

    fun selectMonthYear(month: Int, year: Int) {
        val current = _filterParams.value
        _filterParams.value = Triple(current.first, Pair(month, year), current.third)
    }

    fun selectWeek(week: Int) {
        val current = _filterParams.value
        _filterParams.value = Triple(current.first, current.second, week)
    }

    private fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        return Pair(start, calendar.timeInMillis)
    }

    private fun getWeekRange(year: Int, month: Int, week: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.WEEK_OF_MONTH, week)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        return Pair(start, calendar.timeInMillis)
    }

    private fun normalizeName(name: String): String {
        return name.trim().lowercase().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    private fun calculateReport(
        items: List<ReportItem>,
        timeFrame: TimeFrame,
        month: Int,
        year: Int,
        week: Int
    ): ReportUiState {
        val wastedItems = items.filter { it.status == ReportStatus.WASTED }
        val consumedItems = items.filter { it.status == ReportStatus.CONSUMED }

        val totalWasted = wastedItems.sumOf { it.quantity }
        val totalConsumed = consumedItems.sumOf { it.quantity }

        val wastedCost = wastedItems.sumOf { it.price * it.quantity }
        val savedCost = consumedItems.sumOf { it.price * it.quantity }

        val topWasted = wastedItems.groupBy { normalizeName(it.name) }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .maxByOrNull { it.value }

        val wastedCategories = wastedItems.groupBy { it.category.trim() }
            .map { (cat, list) ->
                val catCount = list.sumOf { it.quantity }
                CategoryBreakdown(cat, catCount, if (totalWasted > 0) ((catCount.toFloat() / totalWasted) * 100) else 0f, list.sumOf { it.price * it.quantity })
            }.sortedByDescending { it.count }

        val topWastedList = wastedItems.groupBy { normalizeName(it.name) }
            .map { (displayName, list) ->
                val itemCount = list.sumOf { it.quantity }
                TopWastedItem(displayName, itemCount, if (totalWasted > 0) ((itemCount.toFloat() / totalWasted) * 100) else 0f, list.sumOf { it.price * it.quantity })
            }.sortedByDescending { it.count }

        val reasonBreakdown = wastedItems.groupBy { it.reason.trim() }
            .map { (reason, list) ->
                val reasonCount = list.sumOf { it.quantity }
                ReasonBreakdown(reason, reasonCount, if (totalWasted > 0) ((reasonCount.toFloat() / totalWasted) * 100) else 0f)
            }.sortedByDescending { it.count }

        val consumedCategories = consumedItems.groupBy { it.category.trim() }
            .map { (cat, list) ->
                val catCount = list.sumOf { it.quantity }
                CategoryBreakdown(cat, catCount, if (totalConsumed > 0) ((catCount.toFloat() / totalConsumed) * 100) else 0f, list.sumOf { it.price * it.quantity })
            }.sortedByDescending { it.count }

        val topConsumedList = consumedItems.groupBy { normalizeName(it.name) }
            .map { (displayName, list) ->
                val itemCount = list.sumOf { it.quantity }
                TopWastedItem(displayName, itemCount, if (totalConsumed > 0) ((itemCount.toFloat() / totalConsumed) * 100) else 0f, list.sumOf { it.price * it.quantity })
            }.sortedByDescending { it.count }

        return ReportUiState(
            selectedTimeFrame = timeFrame,
            selectedMonth = month,
            selectedYear = year,
            selectedWeek = week,
            totalWastedItems = totalWasted,
            totalWastedCost = wastedCost,
            mostWastedName = topWasted?.key ?: "-",
            mostWastedCount = topWasted?.value ?: 0,
            wastedBreakdowns = wastedCategories,
            topWastedItems = topWastedList,
            reasonBreakdowns = reasonBreakdown,
            totalConsumedItems = totalConsumed,
            totalSavedCost = savedCost,
            consumedBreakdowns = consumedCategories,
            topConsumedItems = topConsumedList
        )
    }
}