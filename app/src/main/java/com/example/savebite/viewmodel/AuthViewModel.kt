package com.example.savebite.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.repo.UserRepository
import kotlinx.coroutines.launch

// ViewModel for authentication
class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    // State variables
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // Login user
    fun login(identifier: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Check if user exists and password is correct
            val user = repository.login(identifier, password)
            if (user == null) {
                _errorMessage.value = "Invalid email/phone or password"
            } else {
                _errorMessage.value = null
                onSuccess()
            }
        }
    }

    // Signup user
    fun signup(username: String, email: String, phone: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Check if email or phone number is already registered
            val result = repository.signup(username, email, phone, password)
            result.onSuccess {
                _errorMessage.value = null
                onSuccess()
            }.onFailure { e ->
                _errorMessage.value = e.message
            }
        }
    }
}