package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.RecipeRepository
import com.example.savebite.data.repo.ReportRepository
import com.example.savebite.data.repo.ShoppingRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.model.ExpiryItem
import com.example.savebite.model.RecipeSuggestion
import com.example.savebite.model.ReportStatus
import com.example.savebite.model.SyncStatus
import com.example.savebite.model.WastePeriod
import com.example.savebite.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(
    private val userRepository: UserRepository,
    private val inventoryRepository: InventoryRepository,
    private val shoppingRepository: ShoppingRepository,
    private val reportRepository: ReportRepository,
    private val recipeRepository: RecipeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val RECIPE_SUGGESTION_LIMIT = 5
        private const val EXPIRY_WINDOW_DAYS = 7
        
        private const val WEEK_COUNT = 4
        private const val MONTH_COUNT = 6
        private const val YEAR_COUNT = 3
        private const val MILLIS_PER_WEEK = 1000L * 60 * 60 * 24 * 7
    }

    // Get current user info
    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentUser = sessionManager.userIdFlow.flatMapLatest { userId ->
        if (userId == -1) flowOf(null) else userRepository.getUserByIdFlow(userId)
    }

    val username = currentUser
        .map { it?.username ?: "User" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), "User")

    val avatarUrl = currentUser
        .map { it?.avatarUrl }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    // Sync status from cloud
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    init {
        syncFromCloud()
    }

    // Refresh data from cloud
    fun syncFromCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            _syncStatus.value = SyncStatus.Syncing
            
            val deferreds = listOf(
                async { inventoryRepository.syncFromCloud() },
                async { shoppingRepository.syncFromCloud() },
                async { reportRepository.syncFromCloud() }
            )
            
            val results = deferreds.awaitAll()
            val firstFailure = results.firstOrNull { it.isFailure }
            
            _syncStatus.value = if (firstFailure != null) {
                SyncStatus.Error(firstFailure.exceptionOrNull()?.message ?: "Couldn't refresh from the cloud")
            } else {
                SyncStatus.Idle
            }
        }
    }

    // Expiring items list
    val expiringItems = inventoryRepository.allInventory
        .map { items ->
            items.filter { it.daysLeft <= EXPIRY_WINDOW_DAYS }
                .sortedBy { it.daysLeft }
                .map { item ->
                    ExpiryItem(
                        id = item.id,
                        name = item.name,
                        quantity = "${item.quantity} ${item.unit}",
                        daysLeft = item.daysLeft,
                        category = item.category
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    // Totals for inventory and shopping list
    val inventoryCount = inventoryRepository.allInventory
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    val shoppingListCount = shoppingRepository.allShoppingItems
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    // Money saved this month
    val savedThisMonth = reportRepository.getReportItemsInRange(getCurrentMonthStart(), Long.MAX_VALUE)
        .map { items ->
            items.filter { it.status == ReportStatus.CONSUMED }
                .sumOf { it.price * it.quantity }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Waste report time period
    private val _wastePeriod = MutableStateFlow(WastePeriod.WEEKLY)
    val wastePeriod = _wastePeriod.asStateFlow()

    // Waste chart data
    @OptIn(ExperimentalCoroutinesApi::class)
    val wasteTrackerData = _wastePeriod
        .flatMapLatest { period ->
            val bucketCount = when (period) {
                WastePeriod.WEEKLY -> WEEK_COUNT
                WastePeriod.MONTHLY -> MONTH_COUNT
                WastePeriod.YEARLY -> YEAR_COUNT
            }
            val sinceTimestamp = getStartTimestampFor(period)

            reportRepository.getReportItemsSince(sinceTimestamp).map { items ->
                val wasted = items.filter { it.status == ReportStatus.WASTED }
                val buckets = MutableList(bucketCount) { 0 }
                val now = Calendar.getInstance()

                wasted.forEach { item ->
                    val itemCal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
                    val index = getBucketIndexFor(period, now, itemCal)
                    if (index in 0 until bucketCount) {
                        buckets[bucketCount - 1 - index] += item.quantity
                    }
                }
                buckets
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun getCurrentMonthStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartTimestampFor(period: WastePeriod): Long {
        val cal = Calendar.getInstance()
        when (period) {
            WastePeriod.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, -WEEK_COUNT)
            WastePeriod.MONTHLY -> cal.add(Calendar.MONTH, -MONTH_COUNT)
            WastePeriod.YEARLY -> cal.add(Calendar.YEAR, -YEAR_COUNT)
        }
        return cal.timeInMillis
    }

    private fun startOfWeek(source: Calendar): Calendar {
        val cal = source.clone() as Calendar
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    private fun getBucketIndexFor(period: WastePeriod, now: Calendar, itemCal: Calendar): Int {
        return when (period) {
            WastePeriod.WEEKLY -> {
                val diffMillis = startOfWeek(now).timeInMillis - startOfWeek(itemCal).timeInMillis
                (diffMillis / MILLIS_PER_WEEK).toInt()
            }
            WastePeriod.MONTHLY -> {
                val nowMonths = now.get(Calendar.YEAR) * 12 + now.get(Calendar.MONTH)
                val itemMonths = itemCal.get(Calendar.YEAR) * 12 + itemCal.get(Calendar.MONTH)
                nowMonths - itemMonths
            }
            WastePeriod.YEARLY -> now.get(Calendar.YEAR) - itemCal.get(Calendar.YEAR)
        }
    }

    fun onWastePeriodSelected(period: WastePeriod) {
        _wastePeriod.value = period
    }

    // Get recipe suggestions - scoped to whichever user is currently logged in
    // (cachedRecipes is now keyed per-user, see RecipeDao / RecipeRepositoryImpl).
    @OptIn(ExperimentalCoroutinesApi::class)
    val recipeSuggestions = sessionManager.userIdFlow
        .flatMapLatest { userId ->
            if (userId == -1) flowOf(emptyList()) else recipeRepository.cachedRecipes(userId)
        }
        .map { recipes ->
            recipes.take(RECIPE_SUGGESTION_LIMIT).map { recipe ->
                RecipeSuggestion(
                    name = recipe.title,
                    expiringCount = recipe.usedExpiringIngredients.size
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())
}