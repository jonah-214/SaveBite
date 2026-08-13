package com.example.savebite.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.model.User
import com.example.savebite.utils.SessionManager
import com.example.savebite.utils.Validators
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    private val _usernameError = mutableStateOf<String?>(null)
    val usernameError: State<String?> = _usernameError

    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    private val _phoneError = mutableStateOf<String?>(null)
    val phoneError: State<String?> = _phoneError

    private val _updateSuccess = mutableStateOf(false)
    val updateSuccess: State<Boolean> = _updateSuccess

    init {
        loadUser()
    }

    // Load user information from the database
    private fun loadUser() {
        viewModelScope.launch {
            val userId = sessionManager.getLoggedInUserId()
            if (userId != -1) {
                _user.value = userRepository.getUserById(userId)
            }
        }
    }

    // Clear all errors
    fun clearErrors() {
        _usernameError.value = null
        _emailError.value = null
        _phoneError.value = null
    }

    // Edit User Profile
    fun updateProfile(newUserName: String, newEmail: String, newPhone: String) {
        viewModelScope.launch {
            _usernameError.value = null
            _emailError.value = null
            _phoneError.value = null

            // Validation checks all fields
            val usernameFormatError = Validators.validateUsername(newUserName)
            val emailFormatError = Validators.validateEmail(newEmail)
            val phoneFormatError = Validators.validatePhone(newPhone)

            _usernameError.value = usernameFormatError
            _emailError.value = emailFormatError
            _phoneError.value = phoneFormatError

            if (usernameFormatError != null || emailFormatError != null ||
                phoneFormatError != null
            ) {
                return@launch
            }

            // Username, Email and Phone check
            if (userRepository.getUserByUsernameExcludingId(newUserName, _user.value?.id ?: -1) != null) {
                _usernameError.value = "Username is already taken"
                return@launch
            }
            if (userRepository.getUserByEmailExcludingId(newEmail, _user.value?.id ?: -1) != null) {
                _emailError.value = "Email is already taken"
                return@launch
            }
            if (userRepository.getUserByPhoneExcludingId(newPhone, _user.value?.id ?: -1) != null) {
                _phoneError.value = "Phone number is already taken"
                return@launch
            }

            // Update user
            val updatedUser = _user.value?.copy(
                username = newUserName,
                email = newEmail,
                phone = newPhone
            )
            updatedUser?.let {
                userRepository.updateUser(it)
                _user.value = it
                _updateSuccess.value = true
            }
        }
    }

    // Reset update success state
    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

    // Logout user
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearUserSession()
            onLoggedOut()
        }
    }
}