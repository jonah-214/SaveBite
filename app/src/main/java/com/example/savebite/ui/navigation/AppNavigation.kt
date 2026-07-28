package com.example.savebite.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            // Dashboard screen content
        }
    }
}
