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
    // Forgot Password screen
    const val FORGOT_PASSWORD = "forgot_password"
    // Signup screen
    const val SIGNUP = "signup"
    // Home screen
    const val DASHBOARD = "dashboard"
    // Expiry Reminder screen
    const val REMINDER = "reminder"
    // Food Inventory screen
    const val INVENTORY = "inventory"
    // Manage Storage screen
    const val MANAGE_STORAGE = "manage_storage"
    // Shopping List screen
    const val SHOPPING = "shopping"
    // Recipe Suggestions screen
    const val RECIPE = "recipe"
    // Waste Tracker Report screen
    const val REPORTS = "reports"
    // Profile & Settings screen
    const val PROFILE = "profile"
    // Edit Profile screen
    const val EDIT_PROFILE = "edit_profile"
    // Change Password screen
    const val CHANGE_PASSWORD = "change_password"
    // About Us screen
    const val ABOUT_US = "about_us"

    // Routes with arguments
    const val ADD_INVENTORY = "add_inventory"
    const val ADD_INVENTORY_PATTERN = "add_inventory?itemId={itemId}"

    const val INVENTORY_DETAILS = "inventory_details"
    const val INVENTORY_DETAILS_PATTERN = "inventory_details/{itemId}"

    const val ADD_SHOPPING_ITEM = "add_shopping_item"
    const val ADD_SHOPPING_ITEM_PATTERN = "add_shopping_item?itemId={itemId}"

    const val ADD_TO_INVENTORY = "add_to_inventory"
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