package com.example.savebite.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SessionManager(
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("session_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val USER_ID_KEY = "logged_in_user_id"
        private const val NO_USER = -1
    }

    // Save the user's ID in SharedPreferences
    suspend fun saveUserSession(userId: Int) {
        sharedPreferences.edit {
            putInt(USER_ID_KEY, userId)
        }
    }

    // Clear the user's session data from SharedPreferences
    suspend fun clearUserSession() {
        sharedPreferences.edit {
            remove(USER_ID_KEY)
        }
    }

    // Flow for user ID - maintained for compatibility
    val userIdFlow: Flow<Int> = flow {
        emit(sharedPreferences.getInt(USER_ID_KEY, NO_USER))
    }

    // Get the ID of the currently logged-in user from SharedPreferences
    suspend fun getLoggedInUserId(): Int {
        return sharedPreferences.getInt(USER_ID_KEY, NO_USER)
    }
}