package com.example.savebite.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.dao.UserDao
import com.example.savebite.model.User
import kotlinx.coroutines.launch

// ViewModel for authentication
class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    // State variables for login and signup errors
    private val _authError = mutableStateOf<String?>(null)
    val authError: State<String?> = _authError

    // Login function
    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Check if user exists and password is correct
            val user = userDao.getUserByUsername(username)
            // Update state variables based on login result
            if (user == null) {
                _authError.value = "Username not found"
            } else if (user.password != password) {
                _authError.value = "Incorrect password"
            } else {
                _authError.value = null
                onSuccess()
            }
        }
    }

    // Signup function
    fun signup(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Check if username already exists
            val existingUser = userDao.getUserByUsername(username)
            // Update state variables based on signup result
            if (existingUser != null) {
                _authError.value = "Username already exists"
            } else {
                _authError.value = null
                userDao.insertUser(User(username = username, password = password))
                onSuccess()
            }
        }
    }
}