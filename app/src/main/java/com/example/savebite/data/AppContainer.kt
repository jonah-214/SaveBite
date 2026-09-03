package com.example.savebite.data

import android.content.Context
import com.example.savebite.data.ai.GeminiRecipeService
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.data.repo.*
import com.example.savebite.ui.viewmodel.*
import com.example.savebite.utils.NotificationPreferenceManager
import com.example.savebite.utils.SessionManager
import com.example.savebite.utils.ThemePreferenceManager

/**
 * Dependency Injection container at the application level.
 * This class acts as a "Central Supply Room" for all the tools the app needs.
 */
interface AppContainer {
    val supabaseDataRepository: SupabaseDataRepository
    val userRepository: UserRepository
    val inventoryRepository: InventoryRepository
    val shoppingRepository: ShoppingRepository
    val reportRepository: ReportRepository
    val supabaseAuthRepository: SupabaseAuthRepository
    val profileRepository: ProfileRepository
    val recipeRepository: RecipeRepository
    
    val sessionManager: SessionManager
    val themePreferenceManager: ThemePreferenceManager
    val notificationPreferenceManager: NotificationPreferenceManager

    // ViewModel Factories
    val authViewModelFactory: AuthViewModelFactory
    val dashboardViewModelFactory: DashboardViewModelFactory
    val reminderViewModelFactory: ReminderViewModelFactory
    val profileViewModelFactory: ProfileViewModelFactory
    val themeViewModelFactory: ThemeViewModelFactory
    val inventoryViewModelFactory: InventoryViewModelFactory
    val shoppingViewModelFactory: ShoppingViewModelFactory
    val recipeViewModelFactory: RecipeViewModelFactory
    val reportViewModelFactory: ReportViewModelFactory
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    override val themePreferenceManager: ThemePreferenceManager by lazy {
        ThemePreferenceManager(context)
    }

    override val notificationPreferenceManager: NotificationPreferenceManager by lazy {
        NotificationPreferenceManager(context)
    }

    override val supabaseDataRepository: SupabaseDataRepository by lazy {
        SupabaseDataRepository()
    }

    override val userRepository: UserRepository by lazy {
        UserRepository(database.userDao())
    }

    override val inventoryRepository: InventoryRepository by lazy {
        InventoryRepository(database.inventoryDao(), database.storageDao(), database.reportDao(), supabaseDataRepository)
    }

    override val shoppingRepository: ShoppingRepository by lazy {
        ShoppingRepository(database.shoppingDao(), supabaseDataRepository)
    }

    override val reportRepository: ReportRepository by lazy {
        ReportRepositoryImpl(database.reportDao(), supabaseDataRepository)
    }

    override val supabaseAuthRepository: SupabaseAuthRepository by lazy {
        SupabaseAuthRepository(userRepository)
    }

    override val profileRepository: ProfileRepository by lazy {
        ProfileRepository()
    }

    override val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl(
            GeminiRecipeService(apiKey = "AQ.Ab8RN6JQAkJ2paYga254AgbcKuU_Osr1sw9xjIHZ6GYzmlzWWw"),
            database.recipeDao()
        )
    }

    // Factories
    override val authViewModelFactory: AuthViewModelFactory by lazy {
        AuthViewModelFactory(userRepository, supabaseAuthRepository, profileRepository, sessionManager)
    }

    override val dashboardViewModelFactory: DashboardViewModelFactory by lazy {
        DashboardViewModelFactory(userRepository, inventoryRepository, shoppingRepository, reportRepository, recipeRepository, sessionManager)
    }

    override val reminderViewModelFactory: ReminderViewModelFactory by lazy {
        ReminderViewModelFactory(inventoryRepository)
    }

    override val profileViewModelFactory: ProfileViewModelFactory by lazy {
        ProfileViewModelFactory(userRepository, supabaseAuthRepository, profileRepository, sessionManager, notificationPreferenceManager)
    }

    override val themeViewModelFactory: ThemeViewModelFactory by lazy {
        ThemeViewModelFactory(themePreferenceManager)
    }

    override val inventoryViewModelFactory: InventoryViewModelFactory by lazy {
        InventoryViewModelFactory(inventoryRepository)
    }

    override val shoppingViewModelFactory: ShoppingViewModelFactory by lazy {
        ShoppingViewModelFactory(shoppingRepository, inventoryRepository)
    }

    override val recipeViewModelFactory: RecipeViewModelFactory by lazy {
        RecipeViewModelFactory(recipeRepository, sessionManager)
    }

    override val reportViewModelFactory: ReportViewModelFactory by lazy {
        ReportViewModelFactory(reportRepository)
    }
}
