package com.example.savebite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.savebite.screen.AddInventoryScreen
import com.example.savebite.screen.InventoryDetailScreen
import com.example.savebite.screen.InventoryList
import com.example.savebite.viewmodel.InventoryViewModel

@Composable
fun InventoryNavigation(viewModel: InventoryViewModel) {
    val navController = rememberNavController()
    val inventoryList by viewModel.inventoryList.collectAsState()
    val storageList by viewModel.storageList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStorage by viewModel.selectedStorage.collectAsState()

    NavHost(navController = navController, startDestination = "inventory_list") {

        // --- 1. LIST SCREEN ---
        composable("inventory_list") {
            InventoryList(
                foods = inventoryList,
                storageList = storageList,
                searchQuery = searchQuery,
                onQueryChange = { viewModel.searchQuery.value = it },
                selectedStorage = selectedStorage,
                onStorageSelected = { viewModel.selectedStorage.value = it },
                onNavigateToAddInventory = { navController.navigate("add_inventory") },
                onAddStorageClick = { viewModel.addStorage(it) },
                onItemClick = { item -> navController.navigate("details/${item.id}") },
                onEditClick = { item -> navController.navigate("add_inventory?itemId=${item.id}") },
                onDeleteClick = { item -> viewModel.deleteItem(item) }
            )
        }

        // --- 2. ADD / EDIT SCREEN ---
        composable(
            route = "add_inventory?itemId={itemId}",
            arguments = listOf(navArgument("itemId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            AddInventoryScreen(
                itemId = itemId,
                viewModel = viewModel,
                storageLocations = storageList,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { item ->
                    viewModel.saveItem(item)
                    navController.popBackStack()
                }
            )
        }

        // --- 3. DETAILS SCREEN ---
        composable(
            route = "details/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val itemState by viewModel.getItemById(itemId).collectAsState(initial = null)

            itemState?.let { item ->
                InventoryDetailScreen(
                    detail = item,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate("add_inventory?itemId=${item.id}") },
                    onDeleteClick = {
                        viewModel.deleteItem(item)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}