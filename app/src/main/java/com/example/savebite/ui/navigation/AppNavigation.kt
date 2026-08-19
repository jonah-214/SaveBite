package com.example.savebite.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.UserRepository
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
    userRepository: UserRepository,
    profileViewModelFactory: ProfileViewModelFactory,
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
                val db = AppDatabase.getDatabase(navController.context)
                val inventoryRepository = InventoryRepository(
                    db.inventoryDao(),
                    db.storageDao(),
                    db.wastedItemDao()
                )
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModelFactory(userRepository, inventoryRepository, sessionManager)
                )
                DashboardScreen(
                    navController = navController,
                    dashboardViewModel = dashboardViewModel
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
                    onItemClick = { item -> navController.navigate("inventory_details/${item.id}") },
                    onEditClick = { item -> navController.navigate("add_inventory?itemId=${item.id}") },
                    onDeleteClick = { item -> inventoryViewModel.deleteItem(item) },
                    onNavigateToManageStorage = { navController.navigate(AppRoutes.MANAGE_STORAGE) }
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
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return ShoppingViewModel(shoppingRepository, inventoryRepository) as T
                        }
                    }
                )

                ShoppingListScreen(
                    viewModel = shoppingViewModel,
                    onNavigateToAddItem = { navController.navigate("add_shopping_item") },
                    onNavigateToEditItem = { item -> navController.navigate("add_shopping_item?itemId=${item.id}") },
                    onNavigateToAddToInventory = { navController.navigate("add_to_inventory") }
                )
            }

            composable(
                route = "add_shopping_item?itemId={itemId}",
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

            composable("add_to_inventory") {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(AppRoutes.SHOPPING)
                }
                val shoppingViewModel: ShoppingViewModel = viewModel(viewModelStoreOwner = parentEntry)

                ShoppingItemToInventoryScreen (
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
                PlaceholderScreen("Recipes", viewModel, navController)
            }

            composable(AppRoutes.REPORTS) {
                val context = navController.context
                val db = AppDatabase.getDatabase(context)
                val reportViewModel: ReportViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
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

            composable(AppRoutes.CHANGE_PASSWORD) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(AppRoutes.PROFILE)
                }
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
        }
    }
}
