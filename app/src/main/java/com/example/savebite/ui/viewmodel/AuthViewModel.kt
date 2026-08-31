package com.example.savebite.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.SupabaseAuthRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.utils.SessionManager
import com.example.savebite.utils.Validators
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
    private val supabaseAuthRepository: SupabaseAuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Login Errors - Phone or Email
    private val _loginIdentifierError = mutableStateOf<String?>(null)
    val loginIdentifierError: State<String?> = _loginIdentifierError

    // Login Errors - Password
    private val _loginPasswordError = mutableStateOf<String?>(null)
    val loginPasswordError: State<String?> = _loginPasswordError

    // Login Errors - When credentials are invalid
    private val _loginError = mutableStateOf<String?>(null)
    val loginError: State<String?> = _loginError

    // Forgot Password Errors - Email field error
    private val _forgotPasswordEmailError = mutableStateOf<String?>(null)
    val forgotPasswordEmailError: State<String?> = _forgotPasswordEmailError

    // Forgot Password - Reset email sent success message
    private val _resetEmailSent = mutableStateOf(false)
    val resetEmailSent: State<Boolean> = _resetEmailSent

    // Sign Up Errors - Username
    private val _signupUsernameError = mutableStateOf<String?>(null)
    val signupUsernameError: State<String?> = _signupUsernameError

    // Sign Up Errors - Email
    private val _signupEmailError = mutableStateOf<String?>(null)
    val signupEmailError: State<String?> = _signupEmailError

    // Sign Up Errors - Phone
    private val _signupPhoneError = mutableStateOf<String?>(null)
    val signupPhoneError: State<String?> = _signupPhoneError

    // Sign Up Errors - Password
    private val _signupPasswordError = mutableStateOf<String?>(null)
    val signupPasswordError: State<String?> = _signupPasswordError

    // Loading state - used to show loading spinner
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Login/Sign Up Success Message
    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: State<String?> = _successMessage

    // Clear all errors when switching Login or Signup screens
    fun clearErrors() {
        _loginIdentifierError.value = null
        _loginPasswordError.value = null
        _loginError.value = null
        _signupUsernameError.value = null
        _signupEmailError.value = null
        _signupPhoneError.value = null
        _signupPasswordError.value = null
        _successMessage.value = null
        _forgotPasswordEmailError .value = null
        _resetEmailSent.value = false
    }

    // Clear individual field errors as the user edits (Login screen)
    fun clearLoginIdentifierError() { _loginIdentifierError.value = null }
    fun clearLoginPasswordError() { _loginPasswordError.value = null }

    // Clear individual field errors as the user edits (Sign Up screen)
    fun clearSignupUsernameError() { _signupUsernameError.value = null }
    fun clearSignupEmailError() { _signupEmailError.value = null }
    fun clearSignupPhoneError() { _signupPhoneError.value = null }
    fun clearSignupPasswordError() { _signupPasswordError.value = null }

    // Clear field error (Forgot Password screen)
    fun clearForgotPasswordEmailError() { _forgotPasswordEmailError.value = null }

    // Clear only the success message after it's been shown as a toast
    fun clearSuccessMessage() { _successMessage.value = null }

    // Login user
    fun login(identifier: String, password: String, onSuccess: () -> Unit) {
        if (_isLoading.value) return

        val trimmedIdentifier = identifier.trim()

        viewModelScope.launch {
            _loginIdentifierError.value = null
            _loginPasswordError.value = null
            _loginError.value = null

            val identifierErr = when {
                trimmedIdentifier.isBlank() -> "Email or phone number is required"
                trimmedIdentifier.contains("@") -> Validators.validateEmail(trimmedIdentifier)
                else -> Validators.validatePhone(trimmedIdentifier)
            }
            val passwordErr = if (password.isBlank()) "Password is required" else null

            _loginIdentifierError.value = identifierErr
            _loginPasswordError.value = passwordErr

            if (identifierErr != null || passwordErr != null) {
                return@launch
            }

            _isLoading.value = true

            // Supabase Auth only accepts email, so resolve phone -> email locally first
            val email = if (trimmedIdentifier.contains("@")) {
                trimmedIdentifier
            } else {
                val localUser = userRepository.getUserByPhone(trimmedIdentifier)
                if (localUser != null) {
                    localUser.email
                } else {
                    val remoteEmail = supabaseAuthRepository.getEmailByPhone(trimmedIdentifier)
                    if (remoteEmail == null) {
                        _isLoading.value = false
                        _loginError.value = "Invalid email/phone number or password"
                        return@launch
                    }
                    remoteEmail
                }
            }

            val result = supabaseAuthRepository.login(email, password)
            _isLoading.value = false

            result.onSuccess { user ->
                sessionManager.saveUserSession(user.id)
                _successMessage.value = "Login successful"
                onSuccess()
            }.onFailure {
                _loginError.value = "Invalid email/phone number or password"
            }
        }
    }

    // Reset Password
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

    // Signup user
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
        val trimmedPhone = phone.trim().replace(" ", "").replace("-", "")

        viewModelScope.launch {
            _signupUsernameError.value = null
            _signupEmailError.value = null
            _signupPhoneError.value = null
            _signupPasswordError.value = null

            // Validation checks all fields (format only - uniqueness is enforced by Supabase)
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

    // Logout user
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            supabaseAuthRepository.logout()
            sessionManager.clearUserSession()
            onLoggedOut()
        }
    }
}