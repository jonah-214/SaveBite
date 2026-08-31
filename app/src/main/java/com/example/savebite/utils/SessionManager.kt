package com.example.savebite.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(
    context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("session_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val USER_ID_KEY = "logged_in_user_id"
        private const val NO_USER = -1
    }

    private val _userIdFlow = MutableStateFlow(sharedPreferences.getInt(USER_ID_KEY, NO_USER))
    val userIdFlow: StateFlow<Int> = _userIdFlow.asStateFlow()

    // Save the user's ID in SharedPreferences
    fun saveUserSession(userId: Int) {
        sharedPreferences.edit {
            putInt(USER_ID_KEY, userId)
        }
        _userIdFlow.value = userId
    }

    // Clear the user's session data from SharedPreferences
    fun clearUserSession() {
        sharedPreferences.edit {
            remove(USER_ID_KEY)
        }
        _userIdFlow.value = NO_USER
    }
}