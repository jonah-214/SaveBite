package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.savebite.ui.screen.ExpiryItem
import com.example.savebite.ui.screen.RecipeSuggestion

class DashboardViewModel : ViewModel() {
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