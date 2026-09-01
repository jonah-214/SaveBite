package com.example.savebite.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.example.savebite.data.ai.GeminiRecipeService
import com.example.savebite.data.local.RecipeUserPreferences
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.RecipeRepository
import com.example.savebite.data.repo.ShoppingRepository
import com.example.savebite.model.ReportStatus
import com.example.savebite.ui.screen.AboutUsScreen
import com.example.savebite.ui.screen.AddInventoryScreen
import com.example.savebite.ui.screen.AddShoppingItemScreen
import com.example.savebite.ui.screen.ChangePasswordScreen
import com.example.savebite.ui.screen.DashboardScreen
import com.example.savebite.ui.screen.EditProfileScreen
import com.example.savebite.ui.screen.ForgotPasswordScreen
import com.example.savebite.ui.screen.InventoryDetailScreen
import com.example.savebite.ui.screen.InventoryItemToReportScreen
import com.example.savebite.ui.screen.InventoryList
import com.example.savebite.ui.screen.LoginScreen
import com.example.savebite.ui.screen.ManageStorageScreen
import com.example.savebite.ui.screen.ProfileScreen
import com.example.savebite.ui.screen.RecipeGetStartedScreen
import com.example.savebite.ui.screen.RecipeScreen
import com.example.savebite.ui.screen.ReportItemListScreen
import com.example.savebite.ui.screen.ReportScreen
import com.example.savebite.ui.screen.ShoppingItemToInventoryScreen
import com.example.savebite.ui.screen.ShoppingListScreen
import com.example.savebite.ui.screen.SignUpScreen
import com.example.savebite.ui.screen.SplashScreen
import com.example.savebite.ui.screen.WasteBreakdownDetailScreen
import com.example.savebite.data.repo.ReportRepositoryImpl
import com.example.savebite.ui.viewmodel.AuthViewModel
import com.example.savebite.ui.viewmodel.DashboardViewModel
import com.example.savebite.ui.viewmodel.DashboardViewModelFactory
import com.example.savebite.ui.viewmodel.InventoryViewModel
import com.example.savebite.ui.viewmodel.ProfileViewModel
import com.example.savebite.ui.viewmodel.ProfileViewModelFactory
import com.example.savebite.ui.viewmodel.RecipeViewModel
import com.example.savebite.ui.viewmodel.ReportViewModel
import com.example.savebite.ui.viewmodel.ShoppingViewModel
import com.example.savebite.ui.viewmodel.ThemeViewModel
import com.example.savebite.utils.SessionManager

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: AuthViewModel,
    sessionManager: SessionManager,
    dashboardViewModelFactory: DashboardViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory,
    themeViewModel: ThemeViewModel,
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
                PlaceholderScreen("Reminders")
            }

            // Food Inventory route
            composable(AppRoutes.INVENTORY) {
                val inventoryViewModel: InventoryViewModel = viewModel()
                val inventoryList by inventoryViewModel.inventoryList.collectAsState()
                val storageList by inventoryViewModel.storageList.collectAsState()
                val searchQuery by inventoryViewModel.searchQuery.collectAsState()
                val selectedStorage by inventoryViewModel.selectedStorage.collectAsState()
                val selectedSortOption by inventoryViewModel.selectedSortOption.collectAsState()

                InventoryList(
                    foods = inventoryList,
                    storageList = storageList,
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
                val inventoryViewModel: InventoryViewModel = viewModel(viewModelStoreOwner = parentEntry)

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
                val inventoryViewModel: InventoryViewModel = viewModel()
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
                        // 普通模式/底部的 Back 按钮：返回上一页
                        navController.previousBackStackEntry?.savedStateHandle?.remove<List<Any>>("batch_items")
                        navController.popBackStack()
                    },
                    // 关键补全：传入控制直接跳回 Shopping List 的回调
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
                val inventoryViewModel: InventoryViewModel = viewModel()
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
                val context = navController.context
                val db = AppDatabase.getDatabase(context)
                val inventoryRepository = remember { InventoryRepository(db.inventoryDao(), db.storageDao(), db.reportDao()) }
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

            composable(AppRoutes.RECIPE) {
                val context = LocalContext.current
                val db = AppDatabase.getDatabase(context)
                val scope = androidx.compose.runtime.rememberCoroutineScope()

                // 读取 Recipe 专属的首次运行状态
                val userPreferences = remember { RecipeUserPreferences(context) }
                val isRecipeFirstRun by userPreferences.isRecipeFirstRun.collectAsState(initial = null)

                // 如果仍在读取 Preference 数据，展示 Loading 或占位，防止闪烁
                if (isRecipeFirstRun == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (isRecipeFirstRun == true) {
                    // 首次打开 Recipe，渲染 RecipeGetStartedScreen
                    RecipeGetStartedScreen(
                        onCompleted = {
                            // 点击开始后更新 Preference 标记为 false
                            // 推荐在 ViewModel 中处理异步 update
                            scope.launch {
                                userPreferences.setRecipeFirstRun(false)
                            }
                        }
                    )
                } else {
                    // 非首次打开，正常展示 RecipeScreen
                    val inventoryDao = db.inventoryDao()
                    val recipeDao = db.recipeDao()
                    val allInventoryItems by inventoryDao.getAllInventory().collectAsState(initial = emptyList())

                    val aiService = remember {
                        GeminiRecipeService(apiKey = com.example.savebite.BuildConfig.GEMINI_API_KEY)
                    }

                    val repository = remember {
                        RecipeRepository(aiService, recipeDao)
                    }

                    val recipeViewModel: RecipeViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return RecipeViewModel(repository) as T
                            }
                        }
                    )

                    LaunchedEffect(allInventoryItems) {
                        recipeViewModel.fetchAIRecipes(allInventoryItems)
                    }

                    RecipeScreen(viewModel = recipeViewModel)
                }
            }

            composable(AppRoutes.REPORTS) { backStackEntry ->
                val context = navController.context
                val db = AppDatabase.getDatabase(context)

                val reportViewModel: ReportViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            val repo = ReportRepositoryImpl(db.reportDao())
                            @Suppress("UNCHECKED_CAST")
                            return ReportViewModel(repo) as T
                        }
                    }
                )

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

            // About Us route
            composable(AppRoutes.ABOUT_US) {
                AboutUsScreen(navController = navController)
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