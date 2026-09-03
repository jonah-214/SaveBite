package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.savebite.data.repo.ProfileRepository
import com.example.savebite.data.repo.SupabaseAuthRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.utils.NotificationPreferenceManager
import com.example.savebite.utils.SessionManager

class ProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val supabaseAuthRepository: SupabaseAuthRepository,
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
    private val notificationPreferenceManager: NotificationPreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProfileViewModel(
            userRepository,
            supabaseAuthRepository,
            profileRepository,
            sessionManager,
            notificationPreferenceManager
        ) as T
    }
}