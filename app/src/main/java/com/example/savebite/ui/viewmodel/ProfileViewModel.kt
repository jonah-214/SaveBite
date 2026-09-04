package com.example.savebite.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.R
import com.example.savebite.data.repo.ProfileRepository
import com.example.savebite.data.repo.SupabaseAuthRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.model.User
import com.example.savebite.utils.NotificationPreferenceManager
import com.example.savebite.utils.SessionManager
import com.example.savebite.utils.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class ProfileViewModel(
    // Application context only, passed in by ProfileViewModelFactory — used solely to resolve
    // string resources for error messages, never held onto in a way that could leak an Activity.
    private val context: Context,
    private val userRepository: UserRepository,
    private val supabaseAuthRepository: SupabaseAuthRepository,
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
    private val notificationPreferenceManager: NotificationPreferenceManager
) : ViewModel() {

    // The signed-in user, streamed in from Room by loadUser() below.
    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    // --- Edit Profile: one error slot per field, so each OutlinedTextField can show its own
    // message instead of a single shared error for the whole form.
    private val _avatarError = mutableStateOf<String?>(null)
    val avatarError: State<String?> = _avatarError

    private val _usernameError = mutableStateOf<String?>(null)
    val usernameError: State<String?> = _usernameError

    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    private val _phoneError = mutableStateOf<String?>(null)
    val phoneError: State<String?> = _phoneError

    // Picked-but-not-yet-uploaded avatar, kept as both raw bytes (for the actual upload) and
    // its content URI (so AsyncImage can preview it immediately without waiting on the network).
    private val _pendingAvatarBytes = mutableStateOf<ByteArray?>(null)
    val pendingAvatarBytes: State<ByteArray?> = _pendingAvatarBytes

    private val _pendingAvatarUri = mutableStateOf<android.net.Uri?>(null)
    val pendingAvatarUri: State<android.net.Uri?> = _pendingAvatarUri

    private val _updateSuccess = mutableStateOf(value = false)
    val updateSuccess: State<Boolean> = _updateSuccess

    // Set alongside updateSuccess when the just-saved change included a new email —
    // Supabase Auth requires clicking a confirmation link before it takes effect as the
    // login credential, so the UI should say so instead of implying it's already active.
    private val _emailConfirmationPending = mutableStateOf(value = false)
    val emailConfirmationPending: State<Boolean> = _emailConfirmationPending

    // --- Change Password: one error slot per field, same reasoning as the Edit Profile ones above.
    private val _currentPasswordError = mutableStateOf<String?>(null)
    val currentPasswordError: State<String?> = _currentPasswordError

    private val _newPasswordError = mutableStateOf<String?>(null)
    val newPasswordError: State<String?> = _newPasswordError

    private val _confirmNewPasswordError = mutableStateOf<String?>(null)
    val confirmNewPasswordError: State<String?> = _confirmNewPasswordError

    private val _passwordChangeSuccess = mutableStateOf(value = false)
    val changePasswordSuccess: State<Boolean> = _passwordChangeSuccess

    // --- Deactivate Account: the re-entered password field's error.
    private val _deactivatePasswordError = mutableStateOf<String?>(null)
    val deactivatePasswordError: State<String?> = _deactivatePasswordError

    // Shared across all the actions above — only one runs at a time per screen, so one flag is enough.
    private val _isLoading = mutableStateOf(value = false)
    val isLoading: State<Boolean> = _isLoading

    // Notification Preference - loaded from SharedPreferences so it survives leaving the screen
    private val _notificationEnabled = mutableStateOf(notificationPreferenceManager.isNotificationEnabled())
    val notificationEnabled: State<Boolean> = _notificationEnabled

    init {
        loadUser()
    }

    // Toggle whether expiry reminder notifications are shown
    fun setNotificationEnabled(enabled: Boolean) {
        _notificationEnabled.value = enabled
        notificationPreferenceManager.setNotificationEnabled(enabled)
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
        _emailConfirmationPending.value = false
        _passwordChangeSuccess.value = false
        _deactivatePasswordError.value = null
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

    // Clear Deactivate Account error as the user retypes
    fun clearDeactivatePasswordError() { _deactivatePasswordError.value = null }

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
                _emailError.value = context.getString(R.string.profile_error_user_not_loaded)
                _isLoading.value = false
                return@launch
            }

            // 1. Handle Avatar Upload if there's a pending one
            var finalAvatarUrl = currentUser.avatarUrl
            _pendingAvatarBytes.value?.let { bytes ->
                val uploadResult = profileRepository.uploadAvatar(uid, bytes)
                uploadResult.onSuccess { newUrl ->
                    finalAvatarUrl = newUrl
                }.onFailure {
                    _avatarError.value = context.getString(R.string.profile_error_avatar_upload_failed)
                    // Clear the broken pending avatar so a retry starts clean instead of
                    // re-attempting the same failed upload alongside unrelated field edits.
                    _pendingAvatarBytes.value = null
                    _pendingAvatarUri.value = null
                    _isLoading.value = false
                    return@launch
                }
            }

            // 2. Update Profile Metadata
            val emailChanged = trimmedEmail != currentUser.email
            val result = profileRepository.updateProfile(
                uid,
                trimmedUsername,
                trimmedEmail,
                trimmedPhone,
                currentEmail = currentUser.email
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
                // If the email changed, Supabase Auth needs the user to confirm it via a link
                // sent to their inbox before it becomes the login credential — surface that
                // instead of implying the change is already fully in effect.
                _emailConfirmationPending.value = emailChanged
                _updateSuccess.value = true
            }.onFailure { error ->
                val message = error.message.orEmpty()
                when (message) {
                    "CONFLICT_USERNAME" -> _usernameError.value =
                        context.getString(R.string.profile_error_username_taken)
                    "CONFLICT_EMAIL" -> _emailError.value =
                        context.getString(R.string.profile_error_email_taken)
                    "CONFLICT_PHONE" -> _phoneError.value =
                        context.getString(R.string.profile_error_phone_taken)
                    "AUTH_EMAIL_UPDATE_FAILED" -> _emailError.value =
                        context.getString(R.string.profile_error_auth_email_update_failed)
                    else -> _emailError.value = context.getString(R.string.profile_error_update_failed)
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
                _avatarError.value = context.getString(R.string.profile_error_user_not_loaded)
                return@launch
            }

            _isLoading.value = true
            _avatarError.value = null

            val result = profileRepository.uploadAvatar(uid, imageBytes)
            result.onSuccess { url ->
                val updatedUser = currentUser.copy(avatarUrl = url)
                userRepository.updateUser(updatedUser)
                _user.value = updatedUser
            }.onFailure {
                _avatarError.value = context.getString(R.string.profile_error_avatar_upload_failed)
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
            
            val result = profileRepository.removeAvatar(uid)
            result.onSuccess {
                val updatedUser = currentUser.copy(avatarUrl = null)
                userRepository.updateUser(updatedUser)
                _user.value = updatedUser
            }.onFailure {
                _avatarError.value = context.getString(R.string.profile_error_avatar_remove_failed)
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
                _currentPasswordError.value = context.getString(R.string.profile_error_user_not_loaded)
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
                _newPasswordError.value = context.getString(R.string.change_password_error_same_as_current)
                _isLoading.value = false
                return@launch
            }

            // Confirm password must match new password
            if (newPassword != confirmPassword) {
                _confirmNewPasswordError.value = context.getString(R.string.change_password_error_mismatch)
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
                _currentPasswordError.value = context.getString(R.string.change_password_error_incorrect_current)
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

    // Deactivate the account: verify the password first (so an accidental tap can't do this),
    // mark it inactive in Supabase, then sign out. Logging back in with the same credentials
    // later auto-reactivates it — see AuthViewModel.login().
    fun deactivateAccount(
        password: String,
        onDeactivated: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _deactivatePasswordError.value = null

            val currentUser = _user.value
            val uid = currentUser?.supabaseUid
            if (currentUser == null || uid == null) {
                _deactivatePasswordError.value = context.getString(R.string.profile_error_user_not_loaded)
                _isLoading.value = false
                return@launch
            }

            val reauth = supabaseAuthRepository.reauthenticate(currentUser.email, password)
            if (reauth.isFailure) {
                _deactivatePasswordError.value =
                    context.getString(R.string.deactivate_account_error_incorrect_password)
                _isLoading.value = false
                return@launch
            }

            val result = profileRepository.setAccountActive(uid, false)
            result.onSuccess {
                supabaseAuthRepository.logout()
                sessionManager.clearUserSession()
                _isLoading.value = false
                onDeactivated()
            }.onFailure {
                _deactivatePasswordError.value = context.getString(R.string.deactivate_account_error_failed)
                _isLoading.value = false
            }
        }
    }
}