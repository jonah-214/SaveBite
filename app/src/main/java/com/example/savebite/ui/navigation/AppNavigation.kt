package com.example.savebite.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.ShoppingRepository
import com.example.savebite.ui.screen.AddInventoryScreen
import com.example.savebite.ui.screen.AddShoppingItemScreen
import com.example.savebite.ui.screen.ChangePasswordScreen
import com.example.savebite.ui.screen.DashboardScreen
import com.example.savebite.ui.screen.EditProfileScreen
import com.example.savebite.ui.screen.InventoryDetailScreen
import com.example.savebite.ui.screen.InventoryList
import com.example.savebite.ui.screen.LoginScreen
import com.example.savebite.ui.screen.ManageStorageScreen
import com.example.savebite.ui.screen.ProfileScreen
import com.example.savebite.ui.screen.ReportScreen
import com.example.savebite.ui.screen.ShoppingItemToInventoryScreen
import com.example.savebite.ui.screen.ShoppingListScreen
import com.example.savebite.ui.screen.SignUpScreen
import com.example.savebite.ui.screen.SplashScreen
import com.example.savebite.ui.viewmodel.AuthViewModel
import com.example.savebite.ui.viewmodel.DashboardViewModel
import com.example.savebite.ui.viewmodel.DashboardViewModelFactory
import com.example.savebite.ui.viewmodel.InventoryViewModel
import com.example.savebite.ui.viewmodel.ProfileViewModel
import com.example.savebite.ui.viewmodel.ProfileViewModelFactory
import com.example.savebite.ui.viewmodel.ReportViewModel
import com.example.savebite.ui.viewmodel.ShoppingViewModel
import com.example.savebite.utils.SessionManager

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: AuthViewModel,
    sessionManager: SessionManager,
    dashboardViewModelFactory: DashboardViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Define which screens should show the Bottom Navigation Bar
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
                    onSessionFound = {
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.SPLASH) { inclusive = true }
                        }
                    },
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
                    onLoginSuccess = {
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToSignup = {
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
                    onSignUpSuccess = {
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.SIGNUP) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
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
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = dashboardViewModelFactory
                )
                DashboardScreen(
                    navController = navController,
                    dashboardViewModel = dashboardViewModel
                )
            }

            composable(AppRoutes.REMINDER) {
                PlaceholderScreen("Reminders")
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
                    onNavigateToAddInventory = {
                        navController.navigate(AppRoutes.ADD_INVENTORY) {
                            launchSingleTop = true
                        }
                    },
                    onItemClick = { item ->
                        navController.navigate("${AppRoutes.INVENTORY_DETAILS}/${item.id}") {
                            launchSingleTop = true
                        }
                    },
                    onEditClick = { item ->
                        navController.navigate("${AppRoutes.ADD_INVENTORY}?itemId=${item.id}") {
                            launchSingleTop = true
                        }
                    },
                    onDeleteClick = { item -> inventoryViewModel.deleteItem(item) },
                    onNavigateToManageStorage = {
                        navController.navigate(AppRoutes.MANAGE_STORAGE) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = AppRoutes.ADD_INVENTORY_PATTERN,
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
                route = AppRoutes.INVENTORY_DETAILS_PATTERN,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val inventoryViewModel: InventoryViewModel = viewModel()
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                val itemState by inventoryViewModel.getItemById(itemId).collectAsState(initial = null)

                itemState?.let { item ->
                    InventoryDetailScreen(
                        detail = item,
                        onBackClick = { navController.popBackStack() },
                        onEditClick = {
                            navController.navigate("${AppRoutes.ADD_INVENTORY}?itemId=${item.id}") {
                                launchSingleTop = true
                            }
                        },
                        onDeleteClick = {
                            inventoryViewModel.deleteItem(item)
                            navController.popBackStack()
                        },
                        onWasteClick = {
                            inventoryViewModel.markAsWaste(item)
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(AppRoutes.MANAGE_STORAGE) {
                val inventoryViewModel: InventoryViewModel = viewModel()
                val storageList by inventoryViewModel.storageList.collectAsState()
                ManageStorageScreen(
                    storages = storageList,
                    onBackClick = { navController.popBackStack() },
                    onAddStorageClick = { inventoryViewModel.addStorage(it) },
                    onDeleteStorageClick = { inventoryViewModel.deleteStorage(it) }
                )
            }

            composable(AppRoutes.SHOPPING) {
                val context = navController.context
                val db = AppDatabase.getDatabase(context)
                val inventoryRepository = remember { InventoryRepository(db.inventoryDao(), db.storageDao(), db.wastedItemDao()) }
                val shoppingRepository = remember { ShoppingRepository(db.shoppingDao()) }

                val shoppingViewModel: ShoppingViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return ShoppingViewModel(shoppingRepository, inventoryRepository) as T
                        }
                    }
                )

                ShoppingListScreen(
                    viewModel = shoppingViewModel,
                    onNavigateToAddItem = {
                        navController.navigate(AppRoutes.ADD_SHOPPING_ITEM) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToEditItem = { item ->
                        navController.navigate("${AppRoutes.ADD_SHOPPING_ITEM}?itemId=${item.id}") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToAddToInventory = {
                        navController.navigate(AppRoutes.ADD_TO_INVENTORY) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = AppRoutes.ADD_SHOPPING_ITEM_PATTERN,
                arguments = listOf(navArgument("itemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.SHOPPING) }
                val shoppingViewModel: ShoppingViewModel = viewModel(viewModelStoreOwner = parentEntry)
                val itemId = backStackEntry.arguments?.getString("itemId")

                AddShoppingItemScreen(
                    itemId = itemId,
                    viewModel = shoppingViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.ADD_TO_INVENTORY) {
                val parentEntry = remember(it) { navController.getBackStackEntry(AppRoutes.SHOPPING) }
                val shoppingViewModel: ShoppingViewModel = viewModel(viewModelStoreOwner = parentEntry)

                ShoppingItemToInventoryScreen(
                    viewModel = shoppingViewModel,
                    onBackClick = { navController.popBackStack() },
                    onSuccess = {
                        // Navigate straight to Inventory Screen and clear backstack up to Dashboard
                        navController.navigate(AppRoutes.INVENTORY) {
                            popUpTo(AppRoutes.SHOPPING) { inclusive = true }
                        }
                    }
                )
            }

            composable(AppRoutes.RECIPE) {
                PlaceholderScreen("Recipes")
            }

            composable(AppRoutes.REPORTS) {
                val context = navController.context
                val db = AppDatabase.getDatabase(context)
                val reportViewModel: ReportViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ReportViewModel(db.wastedItemDao()) as T
                        }
                    }
                )
                ReportScreen(viewModel = reportViewModel)
            }

            composable(AppRoutes.PROFILE) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = profileViewModelFactory
                )
                ProfileScreen(
                    navController = navController,
                    profileViewModel = profileViewModel
                )
            }

            composable(AppRoutes.EDIT_PROFILE) { backStackEntry ->
                // Share ProfileViewModel with ProfileScreen
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoutes.PROFILE)
                }
                val profileViewModel: ProfileViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = profileViewModelFactory
                )
                EditProfileScreen(
                    navController = navController,
                    profileViewModel = profileViewModel
                )
            }

            composable(AppRoutes.CHANGE_PASSWORD) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.PROFILE) }
                val profileViewModel: ProfileViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = profileViewModelFactory
                )
                ChangePasswordScreen(
                    navController = navController,
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("✅ $title reached — navigation works!")
    }
}
