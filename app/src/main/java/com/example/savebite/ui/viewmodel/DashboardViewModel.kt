package com.example.savebite.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.ReportRepository
import com.example.savebite.data.repo.ShoppingRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import com.example.savebite.ui.screen.ExpiryItem
import com.example.savebite.ui.screen.RecipeSuggestion
import com.example.savebite.utils.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
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
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _username = mutableStateOf("User")
    val username: State<String> = _username

    private val _avatarUrl = mutableStateOf<String?>(null)
    val avatarUrl: State<String?> = _avatarUrl

    init {
        loadUserData()
    }

    // Load user data from Supabase or fallback to local Room database (Offline mode)
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadUserData() {
        viewModelScope.launch {
            sessionManager.userIdFlow.flatMapLatest { userId ->
                if (userId == -1) {
                    flowOf(null)
                } else {
                    userRepository.getUserByIdFlow(userId)
                }
            }.collect { user ->
                if (user != null) {
                    _username.value = user.username
                    _avatarUrl.value = user.avatarUrl
                } else {
                    _username.value = "User"
                    _avatarUrl.value = null
                }
            }
        }
    }


    // Expiring Item Section
    val expiringItems = inventoryRepository.allInventory
        .map { list ->
            list.filter { it.daysLeft <= 7 }
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Inventory count KPI
    val inventoryCount = inventoryRepository.allInventory
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val shoppingListCount = shoppingRepository.allShoppingItems
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Real Metrics from ReportRepository
    private val currentMonthStart: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    val savedThisMonth = reportRepository.getReportItemsInRange(currentMonthStart, Long.MAX_VALUE)
        .map { items ->
            items.filter { it.status == ReportStatus.CONSUMED }
                .sumOf { it.price * it.quantity }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val wasteTrackerData = reportRepository.getReportItemsSince(
        Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -4) }.timeInMillis
    ).map { items: List<ReportItem> ->
        val wasted = items.filter { it.status == ReportStatus.WASTED }
        val now = Calendar.getInstance()
        val weeks = mutableListOf(0, 0, 0, 0)
        
        wasted.forEach { item: ReportItem ->
            val itemCal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
            val diffMillis = now.timeInMillis - itemCal.timeInMillis
            val diffWeeks = (diffMillis / (1000 * 60 * 60 * 24 * 7)).toInt()
            if (diffWeeks in 0..3) {
                weeks[3 - diffWeeks] += item.quantity
            }
        }
        weeks
    }.stateIn(viewModelScope, SharingStarted.Lazily, listOf(0, 0, 0, 0))

    val recipeSuggestions = listOf(
        RecipeSuggestion("Cheese Toast", "Uses 1 expiring items"),
        RecipeSuggestion("Tomato Salad", "Uses 2 expiring items")
    )
}