package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.local.dao.WastedItemDao
import com.example.savebite.model.WastedItem
import kotlinx.coroutines.flow.*
import java.util.*

data class CategoryBreakdown(val category: String, val count: Int, val percentage: Float)
data class TopWastedItem(val name: String, val count: Int, val percentage: Float)

data class ReportUiState(
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val totalItems: Int = 0,
    val mostWastedName: String = "-",
    val mostWastedCount: Int = 0,
    val breakdowns: List<CategoryBreakdown> = emptyList(),
    val topItems: List<TopWastedItem> = emptyList()
)

class ReportViewModel(private val wastedDao: WastedItemDao) : ViewModel() {

    private val _selectedMonthYear = MutableStateFlow(
        Pair(
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.YEAR)
        )
    )
    val selectedMonthYear: StateFlow<Pair<Int, Int>> = _selectedMonthYear.asStateFlow()

    val uiState: StateFlow<ReportUiState> = _selectedMonthYear
        .flatMapLatest { (month, year) ->
            val range = getMonthRange(month, year)
            wastedDao.getWastedInRange(range.first, range.second).map { items ->
                calculateReport(items, month, year)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportUiState())

    fun selectMonth(month: Int, year: Int) {
        _selectedMonthYear.value = Pair(month, year)
    }

    private fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val end = calendar.timeInMillis
        
        return Pair(start, end)
    }

    private fun calculateReport(items: List<WastedItem>, month: Int, year: Int): ReportUiState {
        val totalCount = items.sumOf { it.quantity }
        if (totalCount == 0) return ReportUiState(selectedMonth = month, selectedYear = year)

        // Top Wasted Single Item
        val topItem = items.groupBy { it.name }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .maxByOrNull { it.value }

        // Category Breakdown
        val categories = items.groupBy { it.category }
            .map { (cat, list) ->
                val catCount = list.sumOf { it.quantity }
                CategoryBreakdown(cat, catCount, ((catCount.toFloat() / totalCount) * 100))
            }

        // Item List
        val topItems = items.groupBy { it.name }
            .map { (name, list) ->
                val itemCount = list.sumOf { it.quantity }
                TopWastedItem(name, itemCount, ((itemCount.toFloat() / totalCount) * 100))
            }
            .sortedByDescending { it.count }

        return ReportUiState(
            selectedMonth = month,
            selectedYear = year,
            totalItems = totalCount,
            mostWastedName = topItem?.key ?: "-",
            mostWastedCount = topItem?.value ?: 0,
            breakdowns = categories,
            topItems = topItems
        )
    }
}
