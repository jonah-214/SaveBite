package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.savebite.utils.ThemeMode
import com.example.savebite.utils.ThemePreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel(
    private val themePreferenceManager: ThemePreferenceManager
) : ViewModel() {
    private val _themeMode = MutableStateFlow(themePreferenceManager.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        themePreferenceManager.setThemeMode(mode)
    }
}

class ThemeViewModelFactory(
    private val themePreferenceManager: ThemePreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED CAST")
        return ThemeViewModel(themePreferenceManager) as T
    }
}