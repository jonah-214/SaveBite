package com.example.savebite.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.savebite.data.remote.SupabaseClientProvider
import com.example.savebite.utils.SessionManager
import io.github.jan.supabase.auth.auth

@Composable
fun SplashScreen(
    sessionManager: SessionManager,
    onSessionFound: () -> Unit,
    onNoSession: () -> Unit
) {
    // Check for an existing user session
    LaunchedEffect(Unit) {
        val supabaseUser = SupabaseClientProvider.client.auth.currentUserOrNull()
        if (supabaseUser != null) {
            onSessionFound()
        } else {
            sessionManager.clearUserSession()
            onNoSession()
        }
    }

    // Display a loading indicator while checking for the session
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}