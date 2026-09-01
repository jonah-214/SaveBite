package com.example.savebite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.savebite.ui.navigation.AppNavigation
import com.example.savebite.ui.theme.SaveBiteTheme
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.ReportRepositoryImpl
import com.example.savebite.data.repo.ShoppingRepository
import com.example.savebite.data.repo.SupabaseAuthRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.ui.viewmodel.AuthViewModel
import com.example.savebite.ui.viewmodel.AuthViewModelFactory
import com.example.savebite.ui.viewmodel.DashboardViewModelFactory
import com.example.savebite.ui.viewmodel.ProfileViewModelFactory
import com.example.savebite.ui.viewmodel.ReminderViewModelFactory
import com.example.savebite.ui.viewmodel.ThemeViewModel
import com.example.savebite.ui.viewmodel.ThemeViewModelFactory
import com.example.savebite.utils.SessionManager
import com.example.savebite.utils.ThemeMode
import com.example.savebite.utils.ThemePreferenceManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize the database, session manager, and repositories
        val database = AppDatabase.getDatabase(this)
        val sessionManager = SessionManager(this)
        val themePreferenceManager = ThemePreferenceManager(this)

        val userRepository = UserRepository(database.userDao())
        val inventoryRepository = InventoryRepository(database.inventoryDao(), database.storageDao(), database.reportDao())
        val shoppingRepository = ShoppingRepository(database.shoppingDao())
        val reportRepository = ReportRepositoryImpl(database.reportDao())
        val supabaseAuthRepository = SupabaseAuthRepository(userRepository)

        val authViewModelFactory = AuthViewModelFactory(userRepository, supabaseAuthRepository, sessionManager)
        val dashboardViewModelFactory = DashboardViewModelFactory(userRepository, inventoryRepository, shoppingRepository, reportRepository, sessionManager)
        val reminderViewModelFactory = ReminderViewModelFactory(inventoryRepository)
        val profileViewModelFactory = ProfileViewModelFactory(userRepository, supabaseAuthRepository, sessionManager)
        val themeViewModelFactory = ThemeViewModelFactory(themePreferenceManager)

        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel(factory = themeViewModelFactory)
            val themeMode by themeViewModel.themeMode.collectAsState()

            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            SaveBiteTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
                AppNavigation(
                    navController = navController,
                    viewModel = authViewModel,
                    sessionManager = sessionManager,
                    dashboardViewModelFactory = dashboardViewModelFactory,
                    reminderViewModelFactory = reminderViewModelFactory,
                    profileViewModelFactory = profileViewModelFactory,
                    themeViewModel = themeViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}