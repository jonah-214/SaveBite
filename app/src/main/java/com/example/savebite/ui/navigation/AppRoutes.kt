package com.example.savebite.ui.navigation

import com.example.savebite.R

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
    // Recipe Suggestions screen, with an optional pre-filled search query
    const val RECIPE_PATTERN = "recipe?searchQuery={searchQuery}"
    // Recipe Detail screen — recipeIndex is this recipe's position in the current
    // RecipeViewModel.uiState.recipes list (recipes aren't individually persisted with
    // their own id, so the index is what identifies "which one" across the navigation call)
    const val RECIPE_DETAIL = "recipe_detail"
    const val RECIPE_DETAIL_PATTERN = "recipe_detail/{recipeIndex}"
    // Waste Tracker Report screen
    const val REPORTS = "reports"
    // Profile & Settings screen
    const val PROFILE = "profile"
    // Edit Profile screen
    const val EDIT_PROFILE = "edit_profile"
    // Change Password screen
    const val CHANGE_PASSWORD = "change_password"
    // Deactivate Account screen
    const val DEACTIVATE_ACCOUNT = "deactivate_account"
    // Edit Recipe Preferences screen (Diet / Allergies / Household Type)
    const val EDIT_RECIPE_PREFERENCES = "edit_recipe_preferences"
    // About Us screen
    const val ABOUT_US = "about_us"

    // Report Detail screens
    const val WASTE_BREAKDOWN = "waste_breakdown"
    const val WASTED_ITEMS = "wasted_items"
    const val CONSUMED_ITEMS = "consumed_items"

    // Routes with arguments
    const val ADD_INVENTORY = "add_inventory"
    const val ADD_INVENTORY_PATTERN = "add_inventory?itemId={itemId}"

    const val INVENTORY_DETAILS = "inventory_details"
    const val INVENTORY_DETAILS_PATTERN = "inventory_details/{itemId}"

    const val INVENTORY_TO_REPORT = "inventory_to_report"
    const val INVENTORY_TO_REPORT_PATTERN = "inventory_to_report/{status}"

    const val ADD_SHOPPING_ITEM = "add_shopping_item"
    const val ADD_SHOPPING_ITEM_PATTERN = "add_shopping_item?itemId={itemId}"

    const val ADD_TO_INVENTORY = "add_to_inventory"
}

// Bottom Navigation items
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: Int
)

// List of bottom navigation items
object BottomNavItems {
    val bottomNavItems = listOf(
        BottomNavItem(
            AppRoutes.DASHBOARD,
            "Home",
            R.drawable.home
        ),
        BottomNavItem(
            AppRoutes.SHOPPING,
            "Shopping",
            R.drawable.shopping_bag
        ),
        BottomNavItem(
            AppRoutes.INVENTORY,
            "Inventory",
            R.drawable.inventory_2
        ),
        BottomNavItem(
            AppRoutes.RECIPE,
            "Recipe",
            R.drawable.chef_hat
        ),
        BottomNavItem(
            AppRoutes.REPORTS,
            "Reports",
            R.drawable.bar_chart
        )
    )
}