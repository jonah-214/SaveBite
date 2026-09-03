package com.example.savebite.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.ProfileRepository
import com.example.savebite.data.repo.SupabaseAuthRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.utils.SessionManager
import com.example.savebite.utils.Validators
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
    private val supabaseAuthRepository: SupabaseAuthRepository,
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // --- Login State ---
    private val _loginIdentifierError = mutableStateOf<String?>(null)
    val loginIdentifierError: State<String?> = _loginIdentifierError

    private val _loginPasswordError = mutableStateOf<String?>(null)
    val loginPasswordError: State<String?> = _loginPasswordError

    private val _loginError = mutableStateOf<String?>(null)
    val loginError: State<String?> = _loginError

    // --- Forgot Password State ---
    private val _forgotPasswordEmailError = mutableStateOf<String?>(null)
    val forgotPasswordEmailError: State<String?> = _forgotPasswordEmailError

    private val _resetEmailSent = mutableStateOf(false)
    val resetEmailSent: State<Boolean> = _resetEmailSent

    // --- Sign Up State ---
    private val _signupUsernameError = mutableStateOf<String?>(null)
    val signupUsernameError: State<String?> = _signupUsernameError

    private val _signupEmailError = mutableStateOf<String?>(null)
    val signupEmailError: State<String?> = _signupEmailError

    private val _signupPhoneError = mutableStateOf<String?>(null)
    val signupPhoneError: State<String?> = _signupPhoneError

    private val _signupPasswordError = mutableStateOf<String?>(null)
    val signupPasswordError: State<String?> = _signupPasswordError

    // --- Global UI State ---
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    // Resets all error states and success messages
    // Should be called when navigating between auth screens
    fun clearErrors() {
        _loginIdentifierError.value = null
        _loginPasswordError.value = null
        _loginError.value = null
        _signupUsernameError.value = null
        _signupEmailError.value = null
        _signupPhoneError.value = null
        _signupPasswordError.value = null
        _successMessage.value = null
        _forgotPasswordEmailError.value = null
        _resetEmailSent.value = false
    }

    // --- Clear Individual Error Methods ---
    fun clearLoginIdentifierError() { _loginIdentifierError.value = null }
    fun clearLoginPasswordError() { _loginPasswordError.value = null }
    fun clearSignupUsernameError() { _signupUsernameError.value = null }
    fun clearSignupEmailError() { _signupEmailError.value = null }
    fun clearSignupPhoneError() { _signupPhoneError.value = null }
    fun clearSignupPasswordError() { _signupPasswordError.value = null }
    fun clearForgotPasswordEmailError() { _forgotPasswordEmailError.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }

    // Attempts to log in the user using either email or phone number.
    // Handles account reactivation if the user was previously deactivated.
    fun login(identifier: String, password: String, onSuccess: () -> Unit) {
        if (_isLoading.value) return

        val trimmedIdentifier = identifier.trim()

        viewModelScope.launch {
            _loginIdentifierError.value = null
            _loginPasswordError.value = null
            _loginError.value = null

            val normalizedIdentifier = if (trimmedIdentifier.contains("@")) {
                trimmedIdentifier
            } else {
                Validators.normalizeMalaysianPhone(trimmedIdentifier)
            }

            // Perform initial validation before network request
            val identifierErr = when {
                trimmedIdentifier.isBlank() -> "Email or phone number is required"
                normalizedIdentifier.contains("@") -> Validators.validateEmail(normalizedIdentifier)
                else -> Validators.validatePhone(normalizedIdentifier)
            }
            val passwordErr = if (password.isBlank()) "Password is required" else null

            _loginIdentifierError.value = identifierErr
            _loginPasswordError.value = passwordErr

            if (identifierErr != null || passwordErr != null) {
                return@launch
            }

            _isLoading.value = true

            // Resolve identifier to email for Supabase Auth
            val email = if (normalizedIdentifier.contains("@")) {
                normalizedIdentifier
            } else {
                val localUser = userRepository.getUserByPhone(normalizedIdentifier)
                if (localUser != null) {
                    localUser.email
                } else {
                    val remoteEmail = supabaseAuthRepository.getEmailByPhone(normalizedIdentifier)
                    if (remoteEmail == null) {
                        _isLoading.value = false
                        _loginError.value = "Invalid email/phone number or password"
                        return@launch
                    }
                    remoteEmail
                }
            }

            val result = supabaseAuthRepository.login(email, password)

            result.onSuccess { loginResult ->
                if (loginResult.wasReactivated) {
                    val uid = loginResult.user.supabaseUid
                    if (uid != null) {
                        profileRepository.setAccountActive(uid, true)
                    }
                }
                _isLoading.value = false
                sessionManager.saveUserSession(loginResult.user.id)
                _successMessage.value = if (loginResult.wasReactivated) {
                    "Welcome back! Your account has been reactivated."
                } else {
                    "Login successful"
                }
                onSuccess()
            }.onFailure {
                _isLoading.value = false
                _loginError.value = "Invalid email/phone number or password"
            }
        }
    }

    // Sends a password reset link to the specified email address
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _forgotPasswordEmailError.value = null
            _resetEmailSent.value = false

            val emailFormatError = Validators.validateEmail(email)
            if (emailFormatError != null) {
                _forgotPasswordEmailError.value = emailFormatError
                return@launch
            }

            _isLoading.value = true
            val result = supabaseAuthRepository.sendPasswordReset(email)
            _isLoading.value = false

            result.onSuccess {
                _resetEmailSent.value = true
            }.onFailure {
                _forgotPasswordEmailError.value = "Failed to send password reset email. Please try again."
            }
        }
    }

    // Creates a new user account with Supabase and mirrors it to the local database.
    fun signup(
        username: String,
        email: String,
        phone: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        if (_isLoading.value) return

        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = Validators.normalizeMalaysianPhone(phone)

        viewModelScope.launch {
            _signupUsernameError.value = null
            _signupEmailError.value = null
            _signupPhoneError.value = null
            _signupPasswordError.value = null

            val usernameFormatError = Validators.validateUsername(trimmedUsername)
            val emailFormatError = Validators.validateEmail(trimmedEmail)
            val phoneFormatError = Validators.validatePhone(trimmedPhone)
            val passwordFormatError = Validators.validatePassword(password)

            _signupUsernameError.value = usernameFormatError
            _signupEmailError.value = emailFormatError
            _signupPhoneError.value = phoneFormatError
            _signupPasswordError.value = passwordFormatError

            if (usernameFormatError != null || emailFormatError != null ||
                phoneFormatError != null || passwordFormatError != null
            ) {
                return@launch
            }

            _isLoading.value = true
            val result = supabaseAuthRepository.signUp(trimmedUsername, trimmedEmail, trimmedPhone, password)
            _isLoading.value = false

            result.onSuccess { user ->
                sessionManager.saveUserSession(user.id)
                _successMessage.value = "Account created successfully"
                onSuccess()
            }.onFailure { error ->
                val message = error.message.orEmpty()
                when {
                    message == "CONFLICT_USERNAME" ->
                        _signupUsernameError.value = "Username is already taken"
                    message == "CONFLICT_EMAIL" ||
                            message.equals("User already registered", ignoreCase = true) ->
                        _signupEmailError.value = "Email is already registered"
                    message == "CONFLICT_PHONE" ->
                        _signupPhoneError.value = "Phone number is already registered"
                    else ->
                        _signupEmailError.value = "Signup failed. Please try again."
                }
            }
        }
    }

    // Logs out the current user and clears the local session
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            supabaseAuthRepository.logout()
            sessionManager.clearUserSession()
            onLoggedOut()
        }
    }
}