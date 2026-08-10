package com.example.savebite.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.ui.screen.ExpiryItem
import com.example.savebite.ui.screen.RecipeSuggestion
import com.example.savebite.utils.SessionManager
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _username = mutableStateOf("User")
    val username: State<String> = _username

    init {
        loadUsername()
    }

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

    // Temporary hardcoded values - replace with real DAO/repository call
    val inventoryCount = 10
    val shoppingListCount = 5
    val savedThisMonth = 45

    val expiringItems = listOf(
        ExpiryItem(
            name = "Milk 1L",
            quantity = "1 bottle",
            daysLeft = 2,
        ),
        ExpiryItem(
            name = "Apple",
            quantity = "4 pcs",
            daysLeft = 1,
        )
    )

    val recipeSuggestions = listOf(
        RecipeSuggestion("Cheese Toast", "Uses 1 expiring items"),
        RecipeSuggestion("Tomato Salad", "Uses 2 expiring items")
    )

    val wasteTrackerData = listOf(
        3,1,6,2
    )
}