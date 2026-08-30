package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.local.dao.ReportDao
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*

data class CategoryBreakdown(
    val category: String,
    val count: Int,
    val percentage: Float,
    val totalPrice: Double = 0.0 // 可选：记录该分类的总金额
)

data class TopWastedItem(
    val name: String,
    val count: Int,
    val percentage: Float,
    val totalPrice: Double = 0.0 // 新增：该项食材的总金额
)

data class ReasonBreakdown(
    val reason: String,
    val count: Int,
    val percentage: Float
)

data class ReportUiState(
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),

    // Wasted Stats
    val totalWastedItems: Int = 0,
    val totalWastedCost: Double = 0.0, // 新增：总浪费金额
    val mostWastedName: String = "-",
    val mostWastedCount: Int = 0,
    val wastedBreakdowns: List<CategoryBreakdown> = emptyList(),
    val topWastedItems: List<TopWastedItem> = emptyList(),
    val reasonBreakdowns: List<ReasonBreakdown> = emptyList(),

    // Consumed Stats
    val totalConsumedItems: Int = 0,
    val totalSavedCost: Double = 0.0, // 新增：通过消耗节省的总金额
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

        // 计算总金额 (单价 * 数量)
        val wastedCost = wastedItems.sumOf { it.price * it.quantity }
        val savedCost = consumedItems.sumOf { it.price * it.quantity }

        // 1. 最多浪费项目
        val topWasted = wastedItems.groupBy { normalizeName(it.name) }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .maxByOrNull { it.value }

        // 2. 浪费类别占比
        val wastedCategories = wastedItems.groupBy { it.category.ifBlank { "Others" } }
            .map { (cat, list) ->
                val catCount = list.sumOf { it.quantity }
                val catCost = list.sumOf { it.price * it.quantity }
                CategoryBreakdown(
                    category = cat,
                    count = catCount,
                    percentage = if (totalWasted > 0) ((catCount.toFloat() / totalWasted) * 100) else 0f,
                    totalPrice = catCost
                )
            }
            .sortedByDescending { it.count }

        // 3. 浪费 Food List（带价格计算）
        val topWastedList = wastedItems.groupBy { normalizeName(it.name) }
            .map { (displayName, list) ->
                val itemCount = list.sumOf { it.quantity }
                val itemCost = list.sumOf { it.price * it.quantity }
                TopWastedItem(
                    name = displayName,
                    count = itemCount,
                    percentage = if (totalWasted > 0) ((itemCount.toFloat() / totalWasted) * 100) else 0f,
                    totalPrice = itemCost
                )
            }
            .sortedByDescending { it.count }

        // 4. 原因占比
        val reasonBreakdown = wastedItems.groupBy { it.reason.trim() }
            .map { (reason, list) ->
                val reasonCount = list.sumOf { it.quantity }
                ReasonBreakdown(reason, reasonCount, if (totalWasted > 0) ((reasonCount.toFloat() / totalWasted) * 100) else 0f)
            }
            .sortedByDescending { it.count }

        // 5. 消耗类别占比
        val consumedCategories = consumedItems.groupBy { it.category.trim() }
            .map { (cat, list) ->
                val catCount = list.sumOf { it.quantity }
                val catCost = list.sumOf { it.price * it.quantity }
                CategoryBreakdown(
                    category = cat,
                    count = catCount,
                    percentage = if (totalConsumed > 0) ((catCount.toFloat() / totalConsumed) * 100) else 0f,
                    totalPrice = catCost
                )
            }
            .sortedByDescending { it.count }

        // 6. 消耗 Food List（带价格计算）
        val topConsumedList = consumedItems.groupBy { normalizeName(it.name) }
            .map { (displayName, list) ->
                val itemCount = list.sumOf { it.quantity }
                val itemCost = list.sumOf { it.price * it.quantity }
                TopWastedItem(
                    name = displayName,
                    count = itemCount,
                    percentage = if (totalConsumed > 0) ((itemCount.toFloat() / totalConsumed) * 100) else 0f,
                    totalPrice = itemCost
                )
            }
            .sortedByDescending { it.count }

        return ReportUiState(
            selectedMonth = month,
            selectedYear = year,
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