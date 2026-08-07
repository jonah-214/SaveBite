package com.example.savebite.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.savebite.ui.screen.AddInventoryScreen
import com.example.savebite.ui.screen.DashboardScreen
import com.example.savebite.ui.screen.InventoryDetailScreen
import com.example.savebite.ui.screen.InventoryList
import com.example.savebite.ui.screen.LoginScreen
import com.example.savebite.ui.screen.SignUpScreen
import com.example.savebite.ui.screen.SplashScreen
import com.example.savebite.ui.viewmodel.AuthViewModel
import com.example.savebite.ui.viewmodel.InventoryViewModel
import com.example.savebite.utils.SessionManager

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: AuthViewModel,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val routesWithBottomBar = listOf(
        AppRoutes.DASHBOARD,
        AppRoutes.INVENTORY,
        AppRoutes.SHOPPING,
        AppRoutes.RECIPE,
        AppRoutes.REPORTS
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute in routesWithBottomBar) {
                AppBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppRoutes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash screen route
            composable(AppRoutes.SPLASH) {
                SplashScreen(
                    sessionManager = sessionManager,
                    // If a session is found, navigate to the dashboard
                    onSessionFound = {
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.SPLASH) { inclusive = true }
                        }
                    },
                    // If no session is found, navigate to the login screen
                    onNoSession = {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            // Login screen route
            composable(AppRoutes.LOGIN) {
                LoginScreen(
                    viewModel = viewModel,
                    // If login is successful, navigate to the dashboard
                    onLoginSuccess = {
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    // If login is canceled, navigate to the signup screen
                    onNavigateToSignup = {
                        // Clear stale errors before leaving, so Signup opens clean
                        viewModel.clearErrors()
                        navController.navigate(AppRoutes.SIGNUP) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Signup screen route
            composable(AppRoutes.SIGNUP) {
                SignUpScreen(
                    viewModel = viewModel,
                    // If signup is successful, navigate to the dashboard
                    onSignUpSuccess = {
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.SIGNUP) { inclusive = true }
                        }
                    },
                    // If signup is canceled, navigate to the login screen
                    onNavigateToLogin = {
                        // Clear stale errors before leaving, so Login opens clean
                        viewModel.clearErrors()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.SIGNUP) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Dashboard screen route
            composable(AppRoutes.DASHBOARD) {
                DashboardScreen(
                    navController = navController
                )
            }

            composable(AppRoutes.REMINDER) {
                PlaceholderScreen("Reminders", viewModel, navController)
            }

            composable(AppRoutes.INVENTORY) {
                val inventoryViewModel: InventoryViewModel = viewModel()
                val inventoryList by inventoryViewModel.inventoryList.collectAsState()
                val storageList by inventoryViewModel.storageList.collectAsState()
                val searchQuery by inventoryViewModel.searchQuery.collectAsState()
                val selectedStorage by inventoryViewModel.selectedStorage.collectAsState()

                InventoryList(
                    foods = inventoryList,
                    storageList = storageList,
                    searchQuery = searchQuery,
                    onQueryChange = { inventoryViewModel.searchQuery.value = it },
                    selectedStorage = selectedStorage,
                    onStorageSelected = { inventoryViewModel.selectedStorage.value = it },
                    onNavigateToAddInventory = { navController.navigate("add_inventory") },
                    onAddStorageClick = { inventoryViewModel.addStorage(it) },
                    onItemClick = { item -> navController.navigate("inventory_details/${item.id}") },
                    onEditClick = { item -> navController.navigate("add_inventory?itemId=${item.id}") },
                    onDeleteClick = { item -> inventoryViewModel.deleteItem(item) }
                )
            }

            composable(
                route = "add_inventory?itemId={itemId}",
                arguments = listOf(navArgument("itemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val inventoryViewModel: InventoryViewModel = viewModel()
                val storageList by inventoryViewModel.storageList.collectAsState()
                val itemId = backStackEntry.arguments?.getString("itemId")

                AddInventoryScreen(
                    itemId = itemId,
                    viewModel = inventoryViewModel,
                    storageLocations = storageList,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { item ->
                        inventoryViewModel.saveItem(item)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "inventory_details/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val inventoryViewModel: InventoryViewModel = viewModel()
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                val itemState by inventoryViewModel.getItemById(itemId).collectAsState(initial = null)

                itemState?.let { item ->
                    InventoryDetailScreen(
                        detail = item,
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { navController.navigate("add_inventory?itemId=${item.id}") },
                        onDeleteClick = {
                            inventoryViewModel.deleteItem(item)
                            navController.popBackStack()
                        },
                        onWasteClick = {
                            inventoryViewModel.deleteItem(item)
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(AppRoutes.SHOPPING) {
                PlaceholderScreen("Shopping List", viewModel, navController)
            }

            composable(AppRoutes.RECIPE) {
                PlaceholderScreen("Recipes", viewModel, navController)
            }

            composable(AppRoutes.REPORTS) {
                PlaceholderScreen("Reports", viewModel, navController)
            }

            composable(AppRoutes.PROFILE) {
                PlaceholderScreen("Profile", viewModel, navController)
            }
        }
    }
}

// Temporary placeholder screen for each module
@Composable
fun PlaceholderScreen(
    title: String,
    viewModel: AuthViewModel,
    navController: NavHostController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✅ $title reached — navigation works!")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                viewModel.logout {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.DASHBOARD) { inclusive = true }
                    }
                }
            }) {
                Text("Logout (test session clear)")
            }
        }
    }
}