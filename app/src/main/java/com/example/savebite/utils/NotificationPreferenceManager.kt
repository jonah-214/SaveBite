package com.example.savebite.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

// Stores whether the user wants expiry reminder notifications.
// Same SharedPreferences pattern as ThemePreferenceManager, so the two
// preference screens (Appearance, Notification) behave consistently.
class NotificationPreferenceManager(context: Context) {
    private val notificationPreference: SharedPreferences =
        context.getSharedPreferences(
            "notification_prefs",
            Context.MODE_PRIVATE
        )

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    fun isNotificationEnabled(): Boolean {
        return notificationPreference.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationEnabled(enabled: Boolean) {
        notificationPreference.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }
    }
}
