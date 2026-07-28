package com.example.savebite.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.savebite.ui.screen.LoginScreen
import com.example.savebite.ui.screen.SignUpScreen
import com.example.savebite.ui.screen.SplashScreen
import com.example.savebite.ui.viewmodel.AuthViewModel
import com.example.savebite.utils.SessionManager

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: AuthViewModel,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    // Navigation graph
    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH,
        modifier = modifier
    ) {
        // Splash screen route
        composable(AppRoutes.SPLASH) {
            SplashScreen(
                sessionManager = sessionManager,
                onSessionFound = {
                    navController.navigate(AppRoutes.DASHBOARD) {
                        // Remove splash screen from back stack so back button doesn't return to it
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
                        // Clear Login off the back stack so back button doesn't return to it
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(AppRoutes.SIGNUP)
                }
            )
        }

        // Signup screen route
        composable(AppRoutes.SIGNUP) {
            SignUpScreen(
                viewModel = viewModel,
                onSignUpSuccess = {
                    navController.navigate(AppRoutes.DASHBOARD) {
                        // Clear Signup + Login off the back stack so back button doesn't return to it
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppRoutes.LOGIN)
                }
            )
        }

        // Dashboard screen route
        composable(AppRoutes.DASHBOARD) {
            // TEMP placeholder - replace with real DashboardScreen once you build it
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅ Dashboard reached — navigation works!")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        viewModel.logout {
                            navController.navigate(AppRoutes.LOGIN) {
                                popUpTo(AppRoutes.DASHBOARD) { inclusive = true }
                            }
                        }
                    }) {
                        Text("Logout (test session clear)")
                    }
                }
            }
        }
    }
}
