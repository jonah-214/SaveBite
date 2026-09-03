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
        // These are constants (values that don't change)
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val RECIPE_SUGGESTION_LIMIT = 5
        private const val EXPIRY_WINDOW_DAYS = 7 // Items expiring in 7 days or less
        
        // Settings for the Waste Chart
        private const val WEEK_COUNT = 4
        private const val MONTH_COUNT = 6
        private const val YEAR_COUNT = 3
        private const val MILLIS_PER_WEEK = 1000L * 60 * 60 * 24 * 7
    }

    // Header — user's name/avatar, same StateFlow + WhileSubscribed pattern as
    // every other piece of Dashboard state, so the Screen collects everything
    // the same way (collectAsStateWithLifecycle).
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

    // Cloud sync status — Dashboard is usually the first screen after login, so it
    // triggers the same Inventory/Shopping/Report syncFromCloud() each of those screens
    // already runs on their own load. The Room-backed flows below never fail on their
    // own (they only ever read the local cache); this just tracks whether the *sync*
    // that fills that cache succeeded, so the UI can show a "showing saved data" hint
    // on failure instead of a silently-empty section.
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    init {
        syncFromCloud()
    }

    fun syncFromCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            _syncStatus.value = SyncStatus.Syncing
            val results = listOf(
                inventoryRepository.syncFromCloud(),
                shoppingRepository.syncFromCloud(),
                reportRepository.syncFromCloud()
            )
            val firstFailure = results.firstOrNull { it.isFailure }
            _syncStatus.value = if (firstFailure != null) {
                SyncStatus.Error(firstFailure.exceptionOrNull()?.message ?: "Couldn't refresh from the cloud")
            } else {
                SyncStatus.Idle
            }
        }
    }

    // Expiring Items - Look at the inventory and filter items that expire soon.
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

    // Inventory & Shopping count KPI
    val inventoryCount = inventoryRepository.allInventory
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    val shoppingListCount = shoppingRepository.allShoppingItems
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    // Monthly Savings - Calculate how much money was saved by eating food instead of wasting it.
    val savedThisMonth = reportRepository.getReportItemsInRange(getCurrentMonthStart(), Long.MAX_VALUE)
        .map { items ->
            items.filter { it.status == ReportStatus.CONSUMED }
                .sumOf { it.price * it.quantity }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Which time range the bar chart is grouped by (Weekly / Monthly / Yearly)
    private val _wastePeriod = MutableStateFlow(WastePeriod.WEEKLY)
    val wastePeriod = _wastePeriod.asStateFlow()

    // Waste Tracker - Prepares data for the bar chart based on the selected period (Weekly/Monthly/Yearly).
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

    // Helper function to find the start of the current month
    private fun getCurrentMonthStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // Helper function to find how far back we should look for waste data
    private fun getStartTimestampFor(period: WastePeriod): Long {
        val cal = Calendar.getInstance()
        when (period) {
            WastePeriod.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, -WEEK_COUNT)
            WastePeriod.MONTHLY -> cal.add(Calendar.MONTH, -MONTH_COUNT)
            WastePeriod.YEARLY -> cal.add(Calendar.YEAR, -YEAR_COUNT)
        }
        return cal.timeInMillis
    }

    // Aligns a Calendar to midnight on the first day of its calendar week, so two
    // timestamps in the same Sun-Sat (or Mon-Sun, per locale) week always diff to
    // an exact multiple of a week — unlike raw "now minus timestamp" millis math,
    // which drifts depending on what time of day "now" happens to be.
    private fun startOfWeek(source: Calendar): Calendar {
        val cal = source.clone() as Calendar
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    // Helper function to group items into "buckets" for the chart
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

    // Recipe Suggestions card — the same AI-generated recipes cached by the
    // Recipe feature (RecipeRepository), so Dashboard and the Recipe screen
    // never disagree. Dashboard only reads the cache; it never triggers a
    // new AI call itself (that stays a user action on the Recipe screen).
    val recipeSuggestions = recipeRepository.cachedRecipes
        .map { recipes ->
            recipes.take(RECIPE_SUGGESTION_LIMIT).map { recipe ->
                RecipeSuggestion(
                    name = recipe.title,
                    usesText = "Uses ${recipe.usedExpiringIngredients.size} expiring item" +
                        if (recipe.usedExpiringIngredients.size == 1) "" else "s"
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())
}