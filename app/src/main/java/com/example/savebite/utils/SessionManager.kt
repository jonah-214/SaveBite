package com.example.savebite.utils

import android.content.Context

class SessionManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun saveSession(userId: Int) {
        sharedPreferences.edit().putInt("logged_in_user_id", userId).apply()
    }

    fun getLoggedInUserId(): Int = sharedPreferences.getInt("logged_in_user_id", -1)

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}