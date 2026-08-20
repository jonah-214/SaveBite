package com.example.savebite.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

// Define Theme Modes
enum class ThemeMode {
    LIGHT, DARK, SYSTEM;

    fun displayName(): String = when (this) {
        LIGHT -> "Light"
        DARK -> "Dark"
        SYSTEM -> "System (Default)"
    }
}

class ThemePreferenceManager(context: Context) {
    private val themePreference: SharedPreferences =
        context.getSharedPreferences(
            "theme_prefs",
            Context.MODE_PRIVATE
        )

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }

    fun getThemeMode(): ThemeMode {
        val name = themePreference.getString(
            KEY_THEME_MODE,
            ThemeMode.SYSTEM.name
        )
        return ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
    }

    fun setThemeMode(mode: ThemeMode) {
        themePreference.edit { putString(KEY_THEME_MODE, mode.name) }
    }
}