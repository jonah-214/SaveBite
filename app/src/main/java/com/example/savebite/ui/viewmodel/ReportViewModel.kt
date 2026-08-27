package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.local.dao.ReportDao
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*

data class CategoryBreakdown(val category: String, val count: Int, val percentage: Float)
data class TopWastedItem(val name: String, val count: Int, val percentage: Float)
data class ReasonBreakdown(val reason: String, val count: Int, val percentage: Float)

data class ReportUiState(
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),

    // Wasted Stats
    val totalWastedItems: Int = 0,
    val mostWastedName: String = "-",
    val mostWastedCount: Int = 0,
    val wastedBreakdowns: List<CategoryBreakdown> = emptyList(),
    val topWastedItems: List<TopWastedItem> = emptyList(),
    val reasonBreakdowns: List<ReasonBreakdown> = emptyList(),

    // Consumed Stats
    val totalConsumedItems: Int = 0,
    val consumedBreakdowns: List<CategoryBreakdown> = emptyList(),
    val topConsumedItems: List<TopWastedItem> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel(private val reportDao: ReportDao) : ViewModel() {

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
            reportDao.getReportItemsInRange(range.first, range.second).map { items ->
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

    /**
     * 将名称统一去空、转小写，并输出首字母大写格式
     * 例如：" egg " -> "Egg"
     */
    private fun normalizeName(name: String): String {
        return name.trim().lowercase().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    private fun calculateReport(items: List<ReportItem>, month: Int, year: Int): ReportUiState {
        val wastedItems = items.filter { it.status == ReportStatus.WASTED }
        val consumedItems = items.filter { it.status == ReportStatus.CONSUMED }

        val totalWasted = wastedItems.sumOf { it.quantity }
        val totalConsumed = consumedItems.sumOf { it.quantity }

        // 1. 归一化后计算浪费最多的单个食材
        val topWasted = wastedItems.groupBy { normalizeName(it.name) }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .maxByOrNull { it.value }

        // 2. 类别占比计算 (类别通常固定，亦做去空处理)
        val wastedCategories = wastedItems.groupBy { it.category.trim() }
            .map { (cat, list) ->
                val catCount = list.sumOf { it.quantity }
                CategoryBreakdown(cat, catCount, if (totalWasted > 0) ((catCount.toFloat() / totalWasted) * 100) else 0f)
            }
            .sortedByDescending { it.count }

        // 3. 归一化计算 topWastedItems 列表 (合并 Egg 和 egg)
        val topWastedList = wastedItems.groupBy { normalizeName(it.name) }
            .map { (displayName, list) ->
                val itemCount = list.sumOf { it.quantity }
                TopWastedItem(displayName, itemCount, if (totalWasted > 0) ((itemCount.toFloat() / totalWasted) * 100) else 0f)
            }
            .sortedByDescending { it.count }

        // 4. 浪费原因分析
        val reasonBreakdown = wastedItems.groupBy { it.reason.trim() }
            .map { (reason, list) ->
                val reasonCount = list.sumOf { it.quantity }
                ReasonBreakdown(reason, reasonCount, if (totalWasted > 0) ((reasonCount.toFloat() / totalWasted) * 100) else 0f)
            }
            .sortedByDescending { it.count }

        // 5. 已消耗 categories 计算
        val consumedCategories = consumedItems.groupBy { it.category.trim() }
            .map { (cat, list) ->
                val catCount = list.sumOf { it.quantity }
                CategoryBreakdown(cat, catCount, if (totalConsumed > 0) ((catCount.toFloat() / totalConsumed) * 100) else 0f)
            }
            .sortedByDescending { it.count }

        // 6. 归一化计算 topConsumedList 列表
        val topConsumedList = consumedItems.groupBy { normalizeName(it.name) }
            .map { (displayName, list) ->
                val itemCount = list.sumOf { it.quantity }
                TopWastedItem(displayName, itemCount, if (totalConsumed > 0) ((itemCount.toFloat() / totalConsumed) * 100) else 0f)
            }
            .sortedByDescending { it.count }

        return ReportUiState(
            selectedMonth = month,
            selectedYear = year,
            totalWastedItems = totalWasted,
            mostWastedName = topWasted?.key ?: "-",
            mostWastedCount = topWasted?.value ?: 0,
            wastedBreakdowns = wastedCategories,
            topWastedItems = topWastedList,
            reasonBreakdowns = reasonBreakdown,
            totalConsumedItems = totalConsumed,
            consumedBreakdowns = consumedCategories,
            topConsumedItems = topConsumedList
        )
    }
}