package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.ReportRepository
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Supported time frames for filtering reports
enum class TimeFrame { WEEKLY, MONTHLY, YEARLY }

// Data model representing a category-based breakdown of report data
data class CategoryBreakdown(
    val category: String,
    val count: Int,
    val percentage: Float,
    val totalPrice: Double = 0.0
)

// Data model representing an individual food item's performance in the report
data class TopWastedItem(
    val name: String,
    val count: Int,
    val percentage: Float,
    val totalPrice: Double = 0.0
)

// Data model representing a specific reason for food waste
data class ReasonBreakdown(
    val reason: String,
    val count: Int,
    val percentage: Float
)

// UI State for the Report screen
// Contains aggregated data, breakdowns, and time filter information
data class ReportUiState(
    val selectedTimeFrame: TimeFrame = TimeFrame.MONTHLY,
    val dateDisplay: String = "",
    val currentDate: Calendar = Calendar.getInstance(),

    val totalWastedItems: Int = 0,
    val totalWastedCost: Double = 0.0,
    val mostWastedName: String = "-",
    val mostWastedCount: Int = 0,
    val wastedBreakdowns: List<CategoryBreakdown> = emptyList(),
    val topWastedItems: List<TopWastedItem> = emptyList(),
    val reasonBreakdowns: List<ReasonBreakdown> = emptyList(),
    val totalConsumedItems: Int = 0,
    val totalSavedCost: Double = 0.0,
    val consumedBreakdowns: List<CategoryBreakdown> = emptyList(),
    val topConsumedItems: List<TopWastedItem> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    // Internal state flow tracking current TimeFrame and target anchor Calendar
    private val _timeFilter = MutableStateFlow(
        Pair(TimeFrame.WEEKLY, Calendar.getInstance())
    )

    init {
        // Attempt background cloud synchronization; gracefully fallback to local cache on error
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.syncFromCloud()
            } catch (e: Exception) {
                // Ignore network exceptions and rely on local offline storage
                // Ignore network exceptions, fallback to local offline data
            }
        }
    }

    // Observable stream of the current report state
    val uiState: StateFlow<ReportUiState> = _timeFilter
        .flatMapLatest { (timeFrame, calendar) ->
            val range = getTimeRange(timeFrame, calendar)
            val dateDisplay = formatDateDisplay(timeFrame, range.first, range.second)

            repository.getReportItemsInRange(range.first, range.second).map { items ->
                calculateReport(items, timeFrame, dateDisplay, calendar)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportUiState())


    // Updates the time window (Weekly/Monthly/Yearly) for the report
    fun setTimeFrame(timeFrame: TimeFrame) {
        _timeFilter.value = Pair(timeFrame, Calendar.getInstance())
    }

    // Navigates the report filter to the previous time period
    fun navigatePrevious() {
        val (timeFrame, cal) = _timeFilter.value
        val newCal = cal.clone() as Calendar
        when (timeFrame) {
            TimeFrame.WEEKLY -> newCal.add(Calendar.WEEK_OF_YEAR, -1)
            TimeFrame.MONTHLY -> newCal.add(Calendar.MONTH, -1)
            TimeFrame.YEARLY -> newCal.add(Calendar.YEAR, -1)
        }
        _timeFilter.value = Pair(timeFrame, newCal)
    }

    // Navigates the report filter to the next time period
    fun navigateNext() {
        val (timeFrame, cal) = _timeFilter.value
        val newCal = cal.clone() as Calendar
        when (timeFrame) {
            TimeFrame.WEEKLY -> newCal.add(Calendar.WEEK_OF_YEAR, 1)
            TimeFrame.MONTHLY -> newCal.add(Calendar.MONTH, 1)
            TimeFrame.YEARLY -> newCal.add(Calendar.YEAR, 1)
        }
        _timeFilter.value = Pair(timeFrame, newCal)
    }

    private fun getTimeRange(timeFrame: TimeFrame, calendar: Calendar): Pair<Long, Long> {
        val startCal = calendar.clone() as Calendar
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)

        val endCal = calendar.clone() as Calendar

        when (timeFrame) {
            TimeFrame.WEEKLY -> {
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                endCal.timeInMillis = startCal.timeInMillis
                endCal.add(Calendar.DAY_OF_WEEK, 6)
            }
            TimeFrame.MONTHLY -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                endCal.timeInMillis = startCal.timeInMillis
                endCal.add(Calendar.MONTH, 1)
                endCal.add(Calendar.MILLISECOND, -1)
                return Pair(startCal.timeInMillis, endCal.timeInMillis)
            }
            TimeFrame.YEARLY -> {
                startCal.set(Calendar.DAY_OF_YEAR, 1)
                endCal.timeInMillis = startCal.timeInMillis
                endCal.add(Calendar.YEAR, 1)
                endCal.add(Calendar.MILLISECOND, -1)
                return Pair(startCal.timeInMillis, endCal.timeInMillis)
            }
        }

        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        endCal.set(Calendar.MILLISECOND, 999)

        return Pair(startCal.timeInMillis, endCal.timeInMillis)
    }

    private fun formatDateDisplay(timeFrame: TimeFrame, startMs: Long, endMs: Long): String {
        val sdfWeekly = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val sdfMonthly = SimpleDateFormat("MM.yyyy", Locale.getDefault())
        val sdfYearly = SimpleDateFormat("yyyy", Locale.getDefault())

        return when (timeFrame) {
            TimeFrame.WEEKLY -> "${sdfWeekly.format(startMs)} ~ ${sdfWeekly.format(endMs)}"
            TimeFrame.MONTHLY -> sdfMonthly.format(startMs)
            TimeFrame.YEARLY -> sdfYearly.format(startMs)
        }
    }

    private fun calculateReport(
        items: List<ReportItem>,
        timeFrame: TimeFrame,
        dateDisplay: String,
        currentCal: Calendar
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
                CategoryBreakdown(cat, catCount, if (totalConsumed > 0) ((totalConsumed.toFloat() / totalConsumed) * 100) else 0f, list.sumOf { it.price * it.quantity })
            }.sortedByDescending { it.count }

        val topConsumedList = consumedItems.groupBy { normalizeName(it.name) }
            .map { (displayName, list) ->
                val itemCount = list.sumOf { it.quantity }
                TopWastedItem(displayName, itemCount, if (totalConsumed > 0) ((itemCount.toFloat() / totalConsumed) * 100) else 0f, list.sumOf { it.price * it.quantity })
            }.sortedByDescending { it.count }

        return ReportUiState(
            selectedTimeFrame = timeFrame,
            dateDisplay = dateDisplay,
            currentDate = currentCal,
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

    private fun normalizeName(name: String): String {
        return name.trim().lowercase().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}