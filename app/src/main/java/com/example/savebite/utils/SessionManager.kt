package com.example.savebite.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Session Manager - Manage user sessions using DataStore
private val Context.dataStore by preferencesDataStore(name = "session_preferences")

class SessionManager(
    // Inject the application context
    private val context: Context
) {
    // Define keys for user session data
    companion object {
        // Define a key for the logged-in user's ID
        private val USER_ID_KEY = intPreferencesKey("logged_in_user_id")
        // Define a value indicating no user is logged in
        private const val NO_USER = -1
    }

    // Save the user's ID in the DataStore
    suspend fun saveUserSession(userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    // Clear the user's session data from the DataStore
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    val userIdFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY] ?: NO_USER
    }

    // Get the ID of the currently logged-in user from the DataStore
    suspend fun getLoggedInUserId(): Int {
        return userIdFlow.first()
    }
}