package com.example.savebite.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.local.dao.UserDao
import com.example.savebite.model.User
import com.example.savebite.utils.PasswordHasher
import com.example.savebite.utils.SessionManager
import com.example.savebite.utils.Validators
import kotlinx.coroutines.launch

// ViewModel for authentication
class AuthViewModel(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Login Errors
    private val _loginIdentifierError = mutableStateOf<String?>(null)
    val loginIdentifierError: State<String?> = _loginIdentifierError

    private val _loginPasswordError = mutableStateOf<String?>(null)
    val loginPasswordError: State<String?> = _loginPasswordError

    // General login error - shown when credentials don't match
    private val _loginError = mutableStateOf<String?>(null)
    val loginError: State<String?> = _loginError

    // Sign Up Errors
    private val _signupUsernameError = mutableStateOf<String?>(null)
    val signupUsernameError: State<String?> = _signupUsernameError

    private val _signupEmailError = mutableStateOf<String?>(null)
    val signupEmailError: State<String?> = _signupEmailError

    private val _signupPhoneError = mutableStateOf<String?>(null)
    val signupPhoneError: State<String?> = _signupPhoneError

    private val _signupPasswordError = mutableStateOf<String?>(null)
    val signupPasswordError: State<String?> = _signupPasswordError

    /* Call this whenever the Login or Signup screen is (re)entered, so stale
    error messages from a previous visit don't linger on screen. */
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

            // Per-field blank checks
            val identifierErr = if (identifier.isBlank()) "Email or phone number is required" else null
            val passwordErr = if (password.isBlank()) "Password is required" else null

            _loginIdentifierError.value = identifierErr
            _loginPasswordError.value = passwordErr

            if (identifierErr != null || passwordErr != null) {
                return@launch
            }

            val user = userDao.getUserByEmailOrPhone(identifier)
            val hashedInput = PasswordHasher.hash(password)

            if (user == null || user.passwordHash != hashedInput) {
                // Deliberately vague (doesn't reveal whether the account exists)
                _loginError.value = "Invalid email/phone number or password"
            } else {
                sessionManager.saveUserSession(user.id)
                onSuccess()
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

            // Format/rule validation - runs first, no database access needed
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

            // Uniqueness ("record") checks
            if (userDao.getUserByUsername(username) != null) {
                _signupUsernameError.value = "Username is already taken"
                return@launch
            }
            if (userDao.getUserByEmail(email) != null) {
                _signupEmailError.value = "Email is already registered"
                return@launch
            }
            if (userDao.getUserByPhone(phone) != null) {
                _signupPhoneError.value = "Phone number is already registered"
                return@launch
            }

            val newUser = User(
                username = username,
                email = email,
                phone = phone,
                passwordHash = PasswordHasher.hash(password)
            )
            val newUserId = userDao.insertUser(newUser)
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