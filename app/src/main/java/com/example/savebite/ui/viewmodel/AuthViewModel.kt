package com.example.savebite.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.SupabaseAuthRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.model.User
import com.example.savebite.utils.PasswordHasher
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

    // Loading state - useful to disable buttons / show a spinner during network calls
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Clear all errors when switching Login or Signup screens
    fun clearErrors() {
        _loginIdentifierError.value = null
        _loginPasswordError.value = null
        _loginError.value = null
        _signupUsernameError.value = null
        _signupEmailError.value = null
        _signupPhoneError.value = null
        _signupPasswordError.value = null
    }

    // Login user
    fun login(identifier: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loginIdentifierError.value = null
            _loginPasswordError.value = null
            _loginError.value = null

            // Email or Phone and Password blank check
            val identifierErr = if (identifier.isBlank()) "Email or phone number is required" else null
            val passwordErr = if (password.isBlank()) "Password is required" else null

            _loginIdentifierError.value = identifierErr
            _loginPasswordError.value = passwordErr

            if (identifierErr != null || passwordErr != null) {
                return@launch
            }

            // Supabase Auth only accepts email, so resolve phone -> email locally first
            val email = if (identifier.contains("@")) {
                identifier
            } else {
                val localUser = userRepository.getUserByPhone(identifier)
                if (localUser == null) {
                    _loginError.value = "Invalid email/phone number or password"
                    return@launch
                }
                localUser.email
            }

            _isLoading.value = true
            val result = supabaseAuthRepository.login(email, password)
            _isLoading.value = false

            result.onSuccess { user ->
                sessionManager.saveUserSession(user.id)
                onSuccess()
            }.onFailure {
                _loginError.value = "Invalid email/phone number or password"
            }
        }
    }

    // Signup user
    fun signup(username: String, email: String, phone: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _signupUsernameError.value = null
            _signupEmailError.value = null
            _signupPhoneError.value = null
            _signupPasswordError.value = null

            // Validation checks all fields (format only - uniqueness is enforced by Supabase)
            val usernameFormatError = Validators.validateUsername(username)
            val emailFormatError = Validators.validateEmail(email)
            val phoneFormatError = Validators.validatePhone(phone)
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
            val result = supabaseAuthRepository.signUp(username, email, phone, password)
            _isLoading.value = false

            result.onSuccess { user ->
                sessionManager.saveUserSession(user.id)
                onSuccess()
            }.onFailure { error ->
                // Map Supabase's unique-constraint / auth errors back to the right field
                val message = error.message.orEmpty()
                when {
                    message.contains("username", ignoreCase = true) ->
                        _signupUsernameError.value = "Username is already taken"
                    message.contains("email", ignoreCase = true) ||
                            message.contains("already registered", ignoreCase = true) ->
                        _signupEmailError.value = "Email is already registered"
                    message.contains("phone", ignoreCase = true) ->
                        _signupPhoneError.value = "Phone number is already registered"
                    else ->
                        _signupEmailError.value = "Signup failed: $message"
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