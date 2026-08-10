package com.example.savebite.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.ui.screen.ExpiryItem
import com.example.savebite.ui.screen.RecipeSuggestion
import com.example.savebite.utils.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userRepository: UserRepository,
    private val inventoryRepository: InventoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _username = mutableStateOf("User")
    val username: State<String> = _username

    init {
        loadUsername()
    }

    // Load username from session
    private fun loadUsername() {
        viewModelScope.launch {
            val userId = sessionManager.getLoggedInUserId()
            if (userId != -1) {
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    _username.value = user.username
                }
            }
        }
    }

    // Inventory count KPI
    val inventoryCount = inventoryRepository.allInventory
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Expiring Item Section
    val expiringItems = inventoryRepository.allInventory
        .map { list ->
            list.sortedBy { it.daysLeft }
                .take(3)
                .map { item ->
                    ExpiryItem(
                        name = item.name,
                        quantity = "${item.quantity}",
                        daysLeft = item.daysLeft
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Hardcoded values - waiting on Shopping/Waste/Recipe modules
    val shoppingListCount = 5
    val savedThisMonth = 45

    val recipeSuggestions = listOf(
        RecipeSuggestion("Cheese Toast", "Uses 1 expiring items"),
        RecipeSuggestion("Tomato Salad", "Uses 2 expiring items")
    )

    val wasteTrackerData = listOf(3, 1, 6, 2)
}