package com.example.savebite.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.local.dao.UserDao
import com.example.savebite.model.User
import com.example.savebite.utils.PasswordHasher
import com.example.savebite.utils.SessionManager
import kotlinx.coroutines.launch

// ViewModel for authentication
class AuthViewModel(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    // State variables
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // Login user
    fun login(identifier: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (identifier.isBlank() || password.isBlank()) {
                _errorMessage.value = "Please enter your email/phone number and password"
                return@launch
            }

            val user = userDao.getUserByEmailOrPhone(identifier)
            val hashedInput = PasswordHasher.hash(password)

            if (user == null || user.passwordHash != hashedInput) {
                _errorMessage.value = "Invalid email/phone number or password"
            } else {
                _errorMessage.value = null
                sessionManager.saveUserSession(user.id)
                onSuccess()
            }
        }
    }

    // Signup user
    fun signup(username: String, email: String, phone: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (username.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
                _errorMessage.value = "All fields are required"
                return@launch
            }

            if (password.length < 6) {
                _errorMessage.value = "Password must be at least 6 characters"
                return@launch
            }

            if (userDao.getUserByEmail(email) != null) {
                _errorMessage.value = "Email already registered"
                return@launch
            }

            if (userDao.getUserByPhone(phone) != null) {
                _errorMessage.value = "Phone number already registered"
                return@launch
            }

            val newUser = User(
                username = username,
                email = email,
                phone = phone,
                passwordHash = PasswordHasher.hash(password)
            )
            val newUserId = userDao.insertUser(newUser)
            _errorMessage.value = null
            sessionManager.saveUserSession(newUserId.toInt())
            onSuccess()
        }
    }

    // Logout user
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearUserSession()
            onLoggedOut()
        }
    }
}