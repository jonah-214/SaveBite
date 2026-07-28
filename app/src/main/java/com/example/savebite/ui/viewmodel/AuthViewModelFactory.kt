package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.savebite.data.local.dao.UserDao
import com.example.savebite.utils.SessionManager

class AuthViewModelFactory(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    // Create the AuthViewModel instance
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(userDao, sessionManager) as T
    }
}