package com.example.savebite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.savebite.model.Inventory
import com.example.savebite.screen.AddInventoryScreen
import com.example.savebite.screen.InventoryList
import com.example.savebite.screen.InventoryCard
import com.example.savebite.screen.InventoryDetailScreen

// Navigation Routes
sealed class Screen(val route: String) {
    object InventoryList : Screen("inventory_list")
    object AddInventory : Screen("add_inventory")
}

@Composable
fun InventoryNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Starting mock data defined directly here
    val initialItems = listOf(
        Inventory(
            name = "Milk",
            description = "Fresh Whole Milk",
            category = "Dairy",
            storage = "Refrigerator",
            quantity = 1,
            daysLeft = 3,
            purchaseDate = "30 Jul 2026",
            expiry = "05 Aug 2026",
            notes = "Keep chilled"
        ),
        Inventory(
            name = "Apples",
            description = "Fuji Apples",
            category = "Produce",
            storage = "Pantry",
            quantity = 6,
            daysLeft = 8,
            purchaseDate = "01 Aug 2026",
            expiry = "10 Aug 2026",
            notes = ""
        )
    )

    // State list initialized with the mock items above
    val inventoryItems = remember { mutableStateListOf<Inventory>().apply { addAll(initialItems) } }

    NavHost(
        navController = navController,
        startDestination = Screen.InventoryList.route
    ) {
        // 1. Inventory List Screen
        composable(Screen.InventoryList.route) {
            InventoryList(
                foods = inventoryItems,
                onNavigateToAddInventory = {
                    navController.navigate(Screen.AddInventory.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 2. Add Inventory Screen
        composable(Screen.AddInventory.route) {
            AddInventoryScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveClick = { newItem ->
                    // Adds the newly created item to the top of the list
                    inventoryItems.add(0, newItem)
                    navController.popBackStack()
                }
            )
        }
    }
}