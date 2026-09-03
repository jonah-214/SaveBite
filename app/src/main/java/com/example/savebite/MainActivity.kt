package com.example.savebite

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.savebite.ui.navigation.AppNavigation
import com.example.savebite.ui.theme.SaveBiteTheme
import com.example.savebite.ui.viewmodel.AuthViewModel
import com.example.savebite.ui.viewmodel.ThemeViewModel
import com.example.savebite.utils.ThemeMode
import com.example.savebite.notification.ExpiryReminderWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the "Central Supply Room" (AppContainer) from our Application class
        val container = (application as SaveBiteApp).container
        val themeViewModelFactory = container.themeViewModelFactory

        // Step 1: Schedule a background task (Worker) to run every day.
        // This ensures the app checks for expiring food even when you aren't using it.
        val expiryReminderRequest = PeriodicWorkRequestBuilder<ExpiryReminderWorker>(
            1, TimeUnit.DAYS
        ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiry_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            expiryReminderRequest
        )

        enableEdgeToEdge()
        setContent {
            // Step 2: Request "Notification Permission" from the user.
            // Modern Android requires users to click "Allow" before notifications can show up.
            val notificationPermissionLauncher =
                rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    Log.d("SaveBite", "Notification permission granted")
                } else {
                    Log.d("SaveBite", "Notification permission denied — expiry reminders won't show")
                }
            }

            // This block runs when the app first opens
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // ThemeViewModel handles Dark Mode / Light Mode settings
            val themeViewModel: ThemeViewModel = viewModel(factory = themeViewModelFactory)
            val themeMode by themeViewModel.themeMode.collectAsState()

            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // Apply the app's visual style
            SaveBiteTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController() // Handles moving between screens
                
                // Get ViewModels and Factories from the Central Supply Room (Container)
                val authViewModel: AuthViewModel = viewModel(factory = container.authViewModelFactory)

                AppNavigation(
                    navController = navController,
                    viewModel = authViewModel,
                    sessionManager = container.sessionManager,
                    dashboardViewModelFactory = container.dashboardViewModelFactory,
                    reminderViewModelFactory = container.reminderViewModelFactory,
                    profileViewModelFactory = container.profileViewModelFactory,
                    themeViewModel = themeViewModel,
                    inventoryViewModelFactory = container.inventoryViewModelFactory,
                    shoppingViewModelFactory = container.shoppingViewModelFactory,
                    recipeViewModelFactory = container.recipeViewModelFactory,
                    reportViewModelFactory = container.reportViewModelFactory,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}