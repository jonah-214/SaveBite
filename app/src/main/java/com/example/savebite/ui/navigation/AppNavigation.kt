package com.example.savebite.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.savebite.SaveBiteApp
import com.example.savebite.data.local.RecipeUserPreferences
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.model.ReportStatus
import com.example.savebite.ui.screen.AboutUsScreen
import com.example.savebite.ui.screen.AddInventoryScreen
import com.example.savebite.ui.screen.AddShoppingItemScreen
import com.example.savebite.ui.screen.ChangePasswordScreen
import com.example.savebite.ui.screen.DashboardScreen
import com.example.savebite.ui.screen.DeactivateAccountScreen
import com.example.savebite.ui.screen.EditProfileScreen
import com.example.savebite.ui.screen.EditRecipePreferencesScreen
import com.example.savebite.ui.screen.ForgotPasswordScreen
import com.example.savebite.ui.screen.InventoryDetailScreen
import com.example.savebite.ui.screen.InventoryItemToReportScreen
import com.example.savebite.ui.screen.InventoryList
import com.example.savebite.ui.screen.LoginScreen
import com.example.savebite.ui.screen.ManageStorageScreen
import com.example.savebite.ui.screen.ProfileScreen
import com.example.savebite.ui.screen.RecipeDetailScreen
import com.example.savebite.ui.screen.RecipeGetStartedScreen
import com.example.savebite.ui.screen.RecipeScreen
import com.example.savebite.ui.screen.ReminderScreen
import com.example.savebite.ui.screen.ReportItemListScreen
import com.example.savebite.ui.screen.ReportScreen
import com.example.savebite.ui.screen.ShoppingItemToInventoryScreen
import com.example.savebite.ui.screen.ShoppingListScreen
import com.example.savebite.ui.screen.SignUpScreen
import com.example.savebite.ui.screen.SplashScreen
import com.example.savebite.ui.screen.WasteBreakdownDetailScreen
import com.example.savebite.ui.viewmodel.AuthViewModel
import com.example.savebite.ui.viewmodel.DashboardViewModel
import com.example.savebite.ui.viewmodel.DashboardViewModelFactory
import com.example.savebite.ui.viewmodel.InventoryViewModel
import com.example.savebite.ui.viewmodel.InventoryViewModelFactory
import com.example.savebite.ui.viewmodel.ProfileViewModel
import com.example.savebite.ui.viewmodel.ProfileViewModelFactory
import com.example.savebite.ui.viewmodel.RecipeViewModel
import com.example.savebite.ui.viewmodel.RecipeViewModelFactory
import com.example.savebite.ui.viewmodel.ReminderViewModel
import com.example.savebite.ui.viewmodel.ReminderViewModelFactory
import com.example.savebite.ui.viewmodel.ReportViewModel
import com.example.savebite.ui.viewmodel.ReportViewModelFactory
import com.example.savebite.ui.viewmodel.ShoppingViewModel
import com.example.savebite.ui.viewmodel.ShoppingViewModelFactory
import com.example.savebite.ui.viewmodel.ThemeViewModel
import com.example.savebite.utils.SessionManager
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: AuthViewModel,
    sessionManager: SessionManager,
    dashboardViewModelFactory: DashboardViewModelFactory,
    reminderViewModelFactory: ReminderViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory,
    themeViewModel: ThemeViewModel,
    inventoryViewModelFactory: InventoryViewModelFactory,
    shoppingViewModelFactory: ShoppingViewModelFactory,
    recipeViewModelFactory: RecipeViewModelFactory,
    reportViewModelFactory: ReportViewModelFactory,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Define which screens should show the Bottom Navigation Bar
    // Note: RECIPE_PATTERN (not RECIPE) is used here because that's the actual route string
    // the Recipe screen is registered under (it carries an optional ?searchQuery= argument),
    // and currentRoute reports the registered pattern, not the plain "recipe" path.
    val routesWithBottomBar = listOf(
        AppRoutes.DASHBOARD,
        AppRoutes.INVENTORY,
        AppRoutes.SHOPPING,
        AppRoutes.RECIPE_PATTERN,
        AppRoutes.REPORTS
    )

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                            launchSingleTop = true
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(AppRoutes.FORGOT_PASSWORD) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Forgot Password screen route
            composable(AppRoutes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    viewModel = viewModel,
                    onBackToLogin = {
                        viewModel.clearErrors()
                        navController.popBackStack()
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

            // Expiry Reminder route
            composable(AppRoutes.REMINDER) {
                val reminderViewModel: ReminderViewModel = viewModel(
                    factory = reminderViewModelFactory
                )
                ReminderScreen(
                    navController = navController,
                    reminderViewModel = reminderViewModel
                )
            }

            // Food Inventory route
            composable(AppRoutes.INVENTORY) {
                val inventoryViewModel: InventoryViewModel = viewModel(factory = inventoryViewModelFactory)
                val inventoryList by inventoryViewModel.inventoryList.collectAsState()
                val storageList by inventoryViewModel.storageList.collectAsState()
                val searchQuery by inventoryViewModel.searchQuery.collectAsState()
                val selectedStorage by inventoryViewModel.selectedStorage.collectAsState()
                val selectedSortOption by inventoryViewModel.selectedSortOption.collectAsState()
                val isOffline by inventoryViewModel.isOffline.collectAsState()

                InventoryList(
                    foods = inventoryList,
                    storageList = storageList,
                    isOffline = isOffline,
                    searchQuery = searchQuery,
                    onQueryChange = { inventoryViewModel.searchQuery.value = it },
                    selectedStorage = selectedStorage,
                    onStorageSelected = { inventoryViewModel.selectedStorage.value = it },
                    selectedSortOption = selectedSortOption,
                    onSortOptionSelected = { inventoryViewModel.onSortOptionSelected(it) },
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
                    },
                    onToggleConsume = { item ->
                        inventoryViewModel.toggleConsumed(item)
                    },
                    onMoveConsumedToReport = { status ->
                        navController.navigate("${AppRoutes.INVENTORY_TO_REPORT}/$status") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Inventory to Report Confirmation route
            composable(
                route = "${AppRoutes.INVENTORY_TO_REPORT}/{status}",
                arguments = listOf(navArgument("status") { type = NavType.StringType })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.INVENTORY) }
                val inventoryViewModel: InventoryViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = inventoryViewModelFactory
                )

                val statusStr = backStackEntry.arguments?.getString("status") ?: "CONSUMED"
                val targetStatus = if (statusStr == "WASTED") ReportStatus.WASTED else ReportStatus.CONSUMED

                InventoryItemToReportScreen(
                    viewModel = inventoryViewModel,
                    targetStatus = targetStatus,
                    onBackClick = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(AppRoutes.REPORTS) {
                            popUpTo(AppRoutes.INVENTORY)
                        }
                    }
                )
            }

            // Add Food Inventory route
            composable(
                route = AppRoutes.ADD_INVENTORY_PATTERN,
                arguments = listOf(navArgument("itemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val inventoryViewModel: InventoryViewModel = viewModel(factory = inventoryViewModelFactory)
                val storageList by inventoryViewModel.storageList.collectAsState()
                val itemId = backStackEntry.arguments?.getString("itemId")

                val batchItems = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<List<com.example.savebite.model.ShoppingItem>>("batch_items")

                val shoppingEntry = remember(backStackEntry) {
                    try {
                        navController.getBackStackEntry(AppRoutes.SHOPPING)
                    } catch (e: Exception) {
                        null
                    }
                }
                val shoppingViewModel: ShoppingViewModel? = shoppingEntry?.let {
                    viewModel(viewModelStoreOwner = it)
                }

                AddInventoryScreen(
                    itemId = itemId,
                    viewModel = inventoryViewModel,
                    batchItems = batchItems,
                    storageLocations = storageList,
                    onBackClick = {
                        navController.previousBackStackEntry?.savedStateHandle?.remove<List<Any>>("batch_items")
                        navController.popBackStack()
                    },
                    onNavigateToShoppingList = {
                        navController.previousBackStackEntry?.savedStateHandle?.remove<List<Any>>("batch_items")
                        navController.popBackStack(
                            route = AppRoutes.SHOPPING,
                            inclusive = false
                        )
                    },
                    onSaveClick = { itemList ->
                        itemList.forEach { item ->
                            inventoryViewModel.saveItem(item)
                        }

                        if (!batchItems.isNullOrEmpty()) {
                            batchItems.forEach { shoppingItem ->
                                shoppingViewModel?.deleteItem(shoppingItem)
                            }
                            navController.previousBackStackEntry?.savedStateHandle?.remove<List<Any>>("batch_items")

                            navController.navigate(AppRoutes.INVENTORY) {
                                popUpTo(AppRoutes.SHOPPING) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                )
            }

            // Food Inventory Details route
            composable(
                route = AppRoutes.INVENTORY_DETAILS_PATTERN,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val inventoryViewModel: InventoryViewModel = viewModel(factory = inventoryViewModelFactory)
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
                        onConsumedClick = { qty ->
                            inventoryViewModel.consumeItemQuantity(item, qty)
                            navController.popBackStack()
                        },
                        onWasteClick = { qty, reason ->
                            inventoryViewModel.markAsWaste(item, qty, reason)
                            navController.popBackStack()
                        }
                    )
                }
            }

            // Manage Food Storage route
            composable(AppRoutes.MANAGE_STORAGE) {
                val inventoryViewModel: InventoryViewModel = viewModel(factory = inventoryViewModelFactory)
                val storageList by inventoryViewModel.storageList.collectAsState()
                ManageStorageScreen(
                    storages = storageList,
                    onBackClick = { navController.popBackStack() },
                    onAddStorageClick = { inventoryViewModel.addStorage(it) },
                    onDeleteStorageClick = { inventoryViewModel.deleteStorage(it) }
                )
            }

            // Shopping Items Routes
            composable(AppRoutes.SHOPPING) {
                val shoppingViewModel: ShoppingViewModel = viewModel(factory = shoppingViewModelFactory)

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

            // Add Shopping Items route
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

            // Add Shopping Item into Inventory Route
            composable(AppRoutes.ADD_TO_INVENTORY) {
                val parentEntry = remember(it) { navController.getBackStackEntry(AppRoutes.SHOPPING) }
                val shoppingViewModel: ShoppingViewModel = viewModel(viewModelStoreOwner = parentEntry)

                ShoppingItemToInventoryScreen(
                    viewModel = shoppingViewModel,
                    onBackClick = { navController.popBackStack() },
                    onStartBatchAdd = { batchItems ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("batch_items", batchItems)
                        navController.navigate(AppRoutes.ADD_INVENTORY) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = AppRoutes.RECIPE_PATTERN,
                arguments = listOf(navArgument("searchQuery") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val initialSearchQuery = backStackEntry.arguments?.getString("searchQuery")

                val userPreferences = remember { RecipeUserPreferences(context) }
                val isRecipeFirstRun by userPreferences.isRecipeFirstRun.collectAsState(initial = null)

                if (isRecipeFirstRun == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (isRecipeFirstRun == true) {
                    RecipeGetStartedScreen(
                        onCompleted = {
                            scope.launch { userPreferences.setRecipeFirstRun(false) }
                        }
                    )
                } else {
                    val inventoryRepository = (context.applicationContext as SaveBiteApp).container.inventoryRepository
                    val allInventoryItems by inventoryRepository.allInventory.collectAsState(initial = emptyList())

                    val dietType by userPreferences.dietType.collectAsState(initial = "None")
                    val allergies by userPreferences.allergies.collectAsState(initial = emptySet())
                    val householdType by userPreferences.householdType.collectAsState(initial = "Student")

                    val recipeViewModel: RecipeViewModel = viewModel(factory = recipeViewModelFactory)

                    LaunchedEffect(allInventoryItems, dietType, allergies, householdType) {
                        recipeViewModel.fetchAIRecipes(allInventoryItems, dietType, allergies, householdType)
                    }

                    RecipeScreen(
                        viewModel = recipeViewModel,
                        initialSearchQuery = initialSearchQuery ?: "",
                        onRecipeClick = { index ->
                            navController.navigate("${AppRoutes.RECIPE_DETAIL}/$index")
                        }
                    )
                }
            }

            composable(
                route = AppRoutes.RECIPE_DETAIL_PATTERN,
                arguments = listOf(navArgument("recipeIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val recipeIndex = backStackEntry.arguments?.getInt("recipeIndex") ?: 0

                val parentEntry = remember(backStackEntry) {
                    try {
                        navController.getBackStackEntry(AppRoutes.RECIPE_PATTERN)
                    } catch (e: Exception) {
                        null
                    }
                }

                val recipeViewModel: RecipeViewModel = if (parentEntry != null) {
                    viewModel(viewModelStoreOwner = parentEntry, factory = recipeViewModelFactory)
                } else {
                    viewModel(factory = recipeViewModelFactory)
                }

                RecipeDetailScreen(
                    recipeIndex = recipeIndex,
                    viewModel = recipeViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Recipe Detail route
            composable(
                route = AppRoutes.RECIPE_DETAIL_PATTERN,
                arguments = listOf(navArgument("recipeIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val recipeIndex = backStackEntry.arguments?.getInt("recipeIndex") ?: 0
                val recipeViewModel: RecipeViewModel = viewModel(factory = recipeViewModelFactory)

                RecipeDetailScreen(
                    recipeIndex = recipeIndex,
                    viewModel = recipeViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.REPORTS) { backStackEntry ->
                val reportViewModel: ReportViewModel = viewModel(factory = reportViewModelFactory)

                ReportScreen(
                    viewModel = reportViewModel,
                    onNavigateToCategoryBreakdown = { navController.navigate(AppRoutes.WASTE_BREAKDOWN) },
                    onNavigateToWastedItems = { navController.navigate(AppRoutes.WASTED_ITEMS) },
                    onNavigateToConsumedItems = { navController.navigate(AppRoutes.CONSUMED_ITEMS) }
                )
            }

            composable(AppRoutes.WASTE_BREAKDOWN) { backStackEntry ->
                // Share the parent route/level ReportViewModel
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoutes.REPORTS)
                }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)

                WasteBreakdownDetailScreen(
                    viewModel = reportViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.WASTED_ITEMS) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoutes.REPORTS)
                }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)

                ReportItemListScreen(
                    type = ReportStatus.WASTED,
                    viewModel = reportViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.CONSUMED_ITEMS) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoutes.REPORTS)
                }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)

                ReportItemListScreen(
                    type = ReportStatus.CONSUMED,
                    viewModel = reportViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Profile & Settings route
            composable(AppRoutes.PROFILE) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = profileViewModelFactory
                )
                ProfileScreen(
                    navController = navController,
                    profileViewModel = profileViewModel,
                    themeViewModel = themeViewModel
                )
            }

            // Edit Profile route
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

            // Change Password route
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

            // Deactivate Account route
            composable(AppRoutes.DEACTIVATE_ACCOUNT) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.PROFILE) }
                val profileViewModel: ProfileViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = profileViewModelFactory
                )
                DeactivateAccountScreen(
                    navController = navController,
                    profileViewModel = profileViewModel
                )
            }

            // Edit Recipe Preferences route
            composable(AppRoutes.EDIT_RECIPE_PREFERENCES) {
                EditRecipePreferencesScreen(navController = navController)
            }

            // About Us route
            composable(AppRoutes.ABOUT_US) {
                AboutUsScreen(navController = navController)
            }
        }
    }
}