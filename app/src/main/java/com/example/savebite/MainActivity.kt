package com.example.savebite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.savebite.data.local.AppDatabase
import com.example.savebite.ui.navigation.AppNavigation
import com.example.savebite.ui.theme.SaveBiteTheme
import com.example.savebite.ui.viewmodel.AuthViewModelFactory
import com.example.savebite.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependencies
        val database = AppDatabase.getDatabase(this)
        val sessionManager = SessionManager(this)
        val authViewModelFactory = AuthViewModelFactory(database.userDao(), sessionManager)

        enableEdgeToEdge()
        setContent {
            SaveBiteTheme {
                val navController = rememberNavController()
                val authViewModel: com.example.savebite.ui.viewmodel.AuthViewModel = viewModel(factory = authViewModelFactory)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        viewModel = authViewModel,
                        sessionManager = sessionManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}