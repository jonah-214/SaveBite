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
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.ui.viewmodel.AuthViewModel
import com.example.savebite.ui.viewmodel.AuthViewModelFactory
import com.example.savebite.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the database, session manager, and view model
        val database = AppDatabase.getDatabase(this)
        val sessionManager = SessionManager(this)
        val userRepository = UserRepository(database.userDao())
        val authViewModelFactory = AuthViewModelFactory(userRepository, sessionManager)

        enableEdgeToEdge()
        setContent {
            SaveBiteTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)

                AppNavigation(
                    navController = navController,
                    viewModel = authViewModel,
                    sessionManager = sessionManager,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}