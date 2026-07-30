package com.example.savebite.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

// Navigation routes
object AppRoutes {
    // Splash screen
    const val SPLASH = "splash"
    // Login screen
    const val LOGIN = "login"
    // Signup screen
    const val SIGNUP = "signup"
    // Home screen
    const val DASHBOARD = "dashboard"
    // Food Inventory screen
    const val INVENTORY = "inventory"
    // Shopping List screen
    const val SHOPPING = "shopping"
    // Recipe Suggestions screen
    const val RECIPE = "recipe"
    // Waste Tracker Report screen
    const val REPORTS = "reports"
}

// Navigation items
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// List of navigation items
object BottomNavItems {
    val bottomNavItems = listOf(
        BottomNavItem(
            AppRoutes.DASHBOARD,
            "Home",
            Icons.Default.Home
        ),
        BottomNavItem(
            AppRoutes.INVENTORY,
            "Inventory",
            Icons.Default.Inventory
        ),
        BottomNavItem(
            AppRoutes.SHOPPING,
            "Shopping",
            Icons.Default.ShoppingBag
        ),
        BottomNavItem(
            AppRoutes.RECIPE,
            "Recipe",
            Icons.Default.RestaurantMenu
        ),
        BottomNavItem(
            AppRoutes.REPORTS,
            "Reports",
            Icons.Default.BarChart
        )
    )
}