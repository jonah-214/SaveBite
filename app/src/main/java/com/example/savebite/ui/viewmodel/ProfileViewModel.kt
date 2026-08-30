package com.example.savebite.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.SupabaseAuthRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.model.User
import com.example.savebite.utils.SessionManager
import com.example.savebite.utils.Validators
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val supabaseAuthRepository: SupabaseAuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // User information
    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    // Edit Profile Errors - Username
    private val _usernameError = mutableStateOf<String?>(null)
    val usernameError: State<String?> = _usernameError

    // Edit Profile Errors - Email
    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    // Edit Profile Errors - Phone
    private val _phoneError = mutableStateOf<String?>(null)
    val phoneError: State<String?> = _phoneError

    // Edit Profile Success
    private val _updateSuccess = mutableStateOf(value = false)
    val updateSuccess: State<Boolean> = _updateSuccess

    // Change Password Errors - Current Password
    private val _currentPasswordError = mutableStateOf<String?>(null)
    val currentPasswordError: State<String?> = _currentPasswordError

    // Change Password Errors - New Password
    private val _newPasswordError = mutableStateOf<String?>(null)
    val newPasswordError: State<String?> = _newPasswordError

    // Change Password Errors - Confirm New Password
    private val _confirmNewPasswordError = mutableStateOf<String?>(null)
    val confirmNewPasswordError: State<String?> = _confirmNewPasswordError

    // Change Password Success
    private val _passwordChangeSuccess = mutableStateOf(value = false)
    val changePasswordSuccess: State<Boolean> = _passwordChangeSuccess

    // Loading State
    private val _isLoading = mutableStateOf(value = false)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadUser()
    }

    // Load user information from the database
    private fun loadUser() {
        viewModelScope.launch {
            sessionManager.userIdFlow.collect { userId ->
                if (userId != -1) {
                    _isLoading.value = true
                    _user.value = userRepository.getUserById(userId)
                    _isLoading.value = false
                } else {
                    _user.value = null
                }
            }
        }
    }

    // Clear all errors
    fun clearErrors() {
        _usernameError.value = null
        _emailError.value = null
        _phoneError.value = null
        _currentPasswordError.value = null
        _newPasswordError.value = null
        _confirmNewPasswordError.value = null
    }

    // Clear individual Edit Profile errors as the user retypes
    fun clearUsernameError() { _usernameError.value = null }
    fun clearEmailError() { _emailError.value = null }
    fun clearPhoneError() { _phoneError.value = null }

    // Clear individual Change Password errors as the user retypes
    fun clearCurrentPasswordError() { _currentPasswordError.value = null }
    fun clearNewPasswordError() { _newPasswordError.value = null }
    fun clearConfirmNewPasswordError() { _confirmNewPasswordError.value = null }

    // Edit User Profile
    fun updateProfile(
        newUserName: String,
        newEmail: String,
        newPhone: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val trimmedUsername = newUserName.trim()
            val trimmedEmail = newEmail.trim()
            val trimmedPhone = newPhone.trim()

            _usernameError.value = null
            _emailError.value = null
            _phoneError.value = null

            // Validate inputs
            val usernameError = Validators.validateUsername(trimmedUsername)
            val emailError = Validators.validateEmail(trimmedEmail)
            val phoneError = Validators.validatePhone(trimmedPhone)

            _usernameError.value = usernameError
            _emailError.value = emailError
            _phoneError.value = phoneError

            if ((usernameError != null || emailError != null ||
                phoneError != null)
                ) {
                _isLoading.value = false
                return@launch
            }

            val currentUser = _user.value
            val uid = currentUser?.supabaseUid
            if (currentUser == null || uid == null) {
                _emailError.value = "User not loaded. Please try again."
                _isLoading.value = false
                return@launch
            }

            // Supabase is the source of truth: check + update there first
            val result = supabaseAuthRepository.updateProfile(
                uid,
                trimmedUsername,
                trimmedEmail,
                trimmedPhone
            )

            result.onSuccess {
                // Only mirror into Room once Supabase confirms the update was successful
                val updatedUser = currentUser.copy(
                    username = trimmedUsername,
                    email = trimmedEmail,
                    phone = trimmedPhone
                )
                userRepository.updateUser(updatedUser)
                _user.value = updatedUser
                _updateSuccess.value = true
            }.onFailure { error ->
                val message = error.message.orEmpty()
                when (message) {
                    "CONFLICT_USERNAME" -> _usernameError.value = "Username is already taken"
                    "CONFLICT_EMAIL" -> _emailError.value = "Email is already taken"
                    "CONFLICT_PHONE" -> _phoneError.value = "Phone number is already taken"
                    else -> _emailError.value = "Failed to update profile. Please try again."
                }
            }

            _isLoading.value = false
        }
    }

    // Reset update success
    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

    // Change Password
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentPasswordError.value = null
            _newPasswordError.value = null
            _confirmNewPasswordError.value = null

            val currentUser = _user.value
            if (currentUser == null) {
                _currentPasswordError.value = "User not loaded"
                _isLoading.value = false
                return@launch
            }

            // Validate new password format (Local check before any network call)
            val newPasswordFormatError = Validators.validatePassword(newPassword)
            if (newPasswordFormatError != null) {
                _newPasswordError.value = newPasswordFormatError
                _isLoading.value = false
                return@launch
            }

            // New password must be different from current password
            if (newPassword == currentPassword) {
                _newPasswordError.value = "New password must be different from current password"
                _isLoading.value = false
                return@launch
            }

            // Confirm password must match new password
            if (newPassword != confirmPassword) {
                _confirmNewPasswordError.value = "Passwords do not match"
                _isLoading.value = false
                return@launch
            }

            // Update password in Supabase
            val result = supabaseAuthRepository.changePassword(
                email = currentUser.email,
                currentPassword = currentPassword,
                newPassword = newPassword
            )

            result.onSuccess {
                // passwordHash column is no longer used for auth, kept blank for clarity
                val updatedUser = currentUser.copy(passwordHash = "")
                userRepository.updateUser(updatedUser)
                _user.value = updatedUser
                _passwordChangeSuccess.value = true
            }.onFailure {
                _currentPasswordError.value = "Current password is incorrect"
            }
            _isLoading.value = false
        }
    }

    // Reset password change success
    fun resetPasswordChangeSuccess() {
        _passwordChangeSuccess.value = false
    }

    // Logout user
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            supabaseAuthRepository.logout()
            sessionManager.clearUserSession()
            _isLoading.value = false
            onLoggedOut()
        }
    }
}