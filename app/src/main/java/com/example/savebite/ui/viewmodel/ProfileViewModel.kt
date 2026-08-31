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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val supabaseAuthRepository: SupabaseAuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // User information
    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    // Edit Profile Errors - Avatar Picture
    private val _avatarError = mutableStateOf<String?>(null)
    val avatarError: State<String?> = _avatarError

    // Edit Profile Errors - Username
    private val _usernameError = mutableStateOf<String?>(null)
    val usernameError: State<String?> = _usernameError

    // Edit Profile Errors - Email
    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    // Edit Profile Errors - Phone
    private val _phoneError = mutableStateOf<String?>(null)
    val phoneError: State<String?> = _phoneError

    // Edit Profile Pending Avatar (for preview before upload)
    private val _pendingAvatarBytes = mutableStateOf<ByteArray?>(null)
    val pendingAvatarBytes: State<ByteArray?> = _pendingAvatarBytes

    // Edit Profile Pending Avatar URI (for preview before upload)
    private val _pendingAvatarUri = mutableStateOf<android.net.Uri?>(null)
    val pendingAvatarUri: State<android.net.Uri?> = _pendingAvatarUri

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
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadUser() {
        viewModelScope.launch {
            sessionManager.userIdFlow.flatMapLatest { userId ->
                if (userId != -1) {
                    userRepository.getUserByIdFlow(userId)
                } else {
                    flowOf(null)
                }
            }.collect { user ->
                _isLoading.value = true
                _user.value = user
                _isLoading.value = false
            }
        }
    }

    // Clear all errors and pending states
    fun clearErrors() {
        _usernameError.value = null
        _emailError.value = null
        _phoneError.value = null
        _avatarError.value = null
        _currentPasswordError.value = null
        _newPasswordError.value = null
        _confirmNewPasswordError.value = null
        _updateSuccess.value = false
        _passwordChangeSuccess.value = false
        _pendingAvatarBytes.value = null
        _pendingAvatarUri.value = null
    }

    // Clear individual Edit Profile errors as the user retypes
    fun clearUsernameError() { _usernameError.value = null }
    fun clearEmailError() { _emailError.value = null }
    fun clearPhoneError() { _phoneError.value = null }

    // Clear individual Change Password errors as the user retypes
    fun clearCurrentPasswordError() { _currentPasswordError.value = null }
    fun clearNewPasswordError() { _newPasswordError.value = null }
    fun clearConfirmNewPasswordError() { _confirmNewPasswordError.value = null }

    // Set pending avatar for preview
    fun setPendingAvatar(bytes: ByteArray, uri: android.net.Uri, mimeType: String?) {
        val error = Validators.validateImage(bytes, mimeType)
        if (error != null) {
            _avatarError.value = error
            return
        }
        _pendingAvatarBytes.value = bytes
        _pendingAvatarUri.value = uri
        _avatarError.value = null
    }

    // Clear pending avatar
    fun clearPendingAvatar() {
        _pendingAvatarBytes.value = null
        _pendingAvatarUri.value = null
        _avatarError.value = null
    }

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

            // 1. Handle Avatar Upload if there's a pending one
            var finalAvatarUrl = currentUser.avatarUrl
            _pendingAvatarBytes.value?.let { bytes ->
                val uploadResult = supabaseAuthRepository.uploadAvatar(uid, bytes)
                uploadResult.onSuccess { newUrl ->
                    finalAvatarUrl = newUrl
                }.onFailure {
                    _avatarError.value = "Failed to upload picture. Please try again."
                    _isLoading.value = false
                    return@launch
                }
            }

            // 2. Update Profile Metadata
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
                    phone = trimmedPhone,
                    avatarUrl = finalAvatarUrl
                )
                userRepository.updateUser(updatedUser)
                _user.value = updatedUser
                _pendingAvatarBytes.value = null
                _pendingAvatarUri.value = null
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

    // Upload Avatar - Edit Profile
    fun uploadAvatar(imageBytes: ByteArray) {
        viewModelScope.launch {
            val currentUser = _user.value
            val uid = currentUser?.supabaseUid
            if (currentUser == null || uid == null) {
                _avatarError.value = "User not loaded. Please try again."
                return@launch
            }

            _isLoading.value = true
            _avatarError.value = null

            val result = supabaseAuthRepository.uploadAvatar(uid, imageBytes)
            result.onSuccess { url ->
                val updatedUser = currentUser.copy(avatarUrl = url)
                userRepository.updateUser(updatedUser)
                _user.value = updatedUser
            }.onFailure {
                _avatarError.value = "Failed to upload picture. Please try again."
            }

            _isLoading.value = false
        }
    }

    // Remove Avatar - Edit Profile
    fun removeAvatar() {
        viewModelScope.launch {
            val currentUser = _user.value
            val uid = currentUser?.supabaseUid
            if (currentUser == null || uid == null) return@launch

            _isLoading.value = true
            
            // Clear pending if any
            _pendingAvatarBytes.value = null
            _pendingAvatarUri.value = null
            
            val result = supabaseAuthRepository.removeAvatar(uid)
            result.onSuccess {
                val updatedUser = currentUser.copy(avatarUrl = null)
                userRepository.updateUser(updatedUser)
                _user.value = updatedUser
            }.onFailure {
                _avatarError.value = "Failed to remove picture. Please try again."
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