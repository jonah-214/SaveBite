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
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    sessionManager: SessionManager,
    onSessionFound: () -> Unit,
    onNoSession: () -> Unit
) {
    LaunchedEffect(Unit) {
        /**
         * Check local session first. If no ID is saved, we are definitely logged out.
         * This makes the transition to Login screen instant for new/logged-out users.
        **/
        if (sessionManager.userIdFlow.value == -1) {
            onNoSession()
            return@LaunchedEffect
        }

        // If we have a local ID, verify if the remote (Supabase) session is still valid.
        val status = withTimeoutOrNull(5000L.milliseconds) {
            SupabaseClientProvider.client.auth.sessionStatus
                .first { it !is SessionStatus.Initializing }
        }

        if (status is SessionStatus.Authenticated) {
            // Session is valid, go to Dashboard
            onSessionFound()
        } else {
            // Remote session expired or invalid, clear local data and go to Log in
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