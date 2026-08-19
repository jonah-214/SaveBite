package com.example.savebite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.savebite.ui.navigation.AppNavigation
import com.example.savebite.ui.theme.SaveBiteTheme
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.ShoppingRepository
import com.example.savebite.data.repo.SupabaseAuthRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.ui.viewmodel.AuthViewModel
import com.example.savebite.ui.viewmodel.AuthViewModelFactory
import com.example.savebite.ui.viewmodel.DashboardViewModelFactory
import com.example.savebite.ui.viewmodel.ProfileViewModelFactory
import com.example.savebite.utils.SessionManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize the database, session manager, and repositories
        val database = AppDatabase.getDatabase(this)
        val sessionManager = SessionManager(this)

        val userRepository = UserRepository(database.userDao())
        val inventoryRepository = InventoryRepository(database.inventoryDao(), database.storageDao(), database.wastedItemDao())
        val shoppingRepository = ShoppingRepository(database.shoppingDao())
        val supabaseAuthRepository = SupabaseAuthRepository(userRepository)

        val authViewModelFactory = AuthViewModelFactory(userRepository, supabaseAuthRepository, sessionManager)
        val dashboardViewModelFactory = DashboardViewModelFactory(userRepository, inventoryRepository, shoppingRepository, sessionManager)
        val profileViewModelFactory = ProfileViewModelFactory(userRepository, supabaseAuthRepository, sessionManager)

        enableEdgeToEdge()
        setContent {
            SaveBiteTheme {
                val navController = rememberNavController()
                // Get the main AuthViewModel
                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
                // Start the app's navigation
                AppNavigation(
                    navController = navController,
                    viewModel = authViewModel,
                    sessionManager = sessionManager,
                    dashboardViewModelFactory = dashboardViewModelFactory,
                    profileViewModelFactory = profileViewModelFactory,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}