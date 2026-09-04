package com.example.savebite.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.savebite.R
import com.example.savebite.data.remote.SupabaseClientProvider
import com.example.savebite.utils.SessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

// How long the splash screen should stay on screen at minimum (in milliseconds).
// Sometimes the session check below finishes superfast (like when there's no
// saved login at all), so without this the splash screen would disappear too
// quickly for the user to even see the logo.
private const val MIN_SPLASH_DURATION_MS = 1000L

@Composable
fun SplashScreen(
    sessionManager: SessionManager,
    onSessionFound: () -> Unit,
    onNoSession: () -> Unit
) {
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()

        // Small helper function: waits just enough extra time so the splash screen has
        // been showing for at least MIN_SPLASH_DURATION_MS in total. If the check already
        // took longer than that, remaining will be 0 or negative, so we just skip waiting.
        suspend fun awaitMinimumDuration() {
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = MIN_SPLASH_DURATION_MS - elapsed
            if (remaining > 0) delay(remaining.milliseconds)
        }

        // Check local session first. If no ID is saved, we know straight away the user
        // is logged out, so there's no need to call Supabase at all.
        if (sessionManager.userIdFlow.value == -1) {
            awaitMinimumDuration()
            onNoSession()
            return@LaunchedEffect
        }

        // We have a local ID, so double check with Supabase that the session is still valid
        // (it could have expired or been signed out from another device).
        val status = withTimeoutOrNull(5000L.milliseconds) {
            SupabaseClientProvider.client.auth.sessionStatus
                .first { it !is SessionStatus.Initializing }
        }

        awaitMinimumDuration()

        if (status is SessionStatus.Authenticated) {
            // Still logged in, go straight to the Dashboard
            onSessionFound()
        } else {
            // Session expired or something went wrong, so clear local data and
            // send the user back to the Login screen
            sessionManager.clearUserSession()
            onNoSession()
        }
    }

    // UI: logo + app name in the middle, tagline and loading spinner near the bottom
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.savebite_logo_name),
            contentDescription = stringResource(R.string.content_desc_savebite_logo),
            modifier = Modifier
                .align(Alignment.Center)
                .size(180.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.about_us_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        }
    }
}