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

    // ---- Login field errors ----
    private val _loginIdentifierError = mutableStateOf<String?>(null)
    val loginIdentifierError: State<String?> = _loginIdentifierError

    private val _loginPasswordError = mutableStateOf<String?>(null)
    val loginPasswordError: State<String?> = _loginPasswordError

    // General/credential-level error - kept separate and combined on purpose.
    /* Login failures never reveal WHICH part (email or password) was wrong,
    since that would leak whether an account exists to an attacker. */
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // ---- Signup field errors ----
    private val _usernameError = mutableStateOf<String?>(null)
    val usernameError: State<String?> = _usernameError

    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    private val _phoneError = mutableStateOf<String?>(null)
    val phoneError: State<String?> = _phoneError

    private val _passwordError = mutableStateOf<String?>(null)
    val passwordError: State<String?> = _passwordError

    // Login user
    fun login(identifier: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loginIdentifierError.value = null
            _loginPasswordError.value = null
            _errorMessage.value = null

            var hasFieldError = false
            if (identifier.isBlank()) {
                _loginIdentifierError.value = "Email or phone number is required"
                hasFieldError = true
            }
            if (password.isBlank()) {
                _loginPasswordError.value = "Password is required"
                hasFieldError = true
            }
            if (hasFieldError) return@launch

            val user = userDao.getUserByEmailOrPhone(identifier)
            val hashedInput = PasswordHasher.hash(password)

            if (user == null || user.passwordHash != hashedInput) {
                _errorMessage.value = "Invalid email/phone number or password"
            } else {
                sessionManager.saveUserSession(user.id)
                onSuccess()
            }
        }
    }

    // Signup user
    fun signup(username: String, email: String, phone: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _usernameError.value = null
            _emailError.value = null
            _phoneError.value = null
            _passwordError.value = null

            val usernameFormatError = Validators.validateUsername(username)
            val emailFormatError = Validators.validateEmail(email)
            val phoneFormatError = Validators.validatePhone(phone)
            val passwordFormatError = Validators.validatePassword(password)

            _usernameError.value = usernameFormatError
            _emailError.value = emailFormatError
            _phoneError.value = phoneFormatError
            _passwordError.value = passwordFormatError

            if (usernameFormatError != null || emailFormatError != null ||
                phoneFormatError != null || passwordFormatError != null
            ) {
                return@launch
            }

            if (userDao.getUserByUsername(username) != null) {
                _usernameError.value = "Username is already taken"
                return@launch
            }
            if (userDao.getUserByEmail(email) != null) {
                _emailError.value = "Email is already registered"
                return@launch
            }
            if (userDao.getUserByPhone(phone) != null) {
                _phoneError.value = "Phone number is already registered"
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

    // Call when the Login screen is (re)entered, so stale errors from previous visit or from Signup don't linger on screen.
    fun clearLoginErrors() {
        _loginIdentifierError.value = null
        _loginPasswordError.value = null
        _errorMessage.value = null
    }

    // Call when the Signup screen is (re)entered, for the same reason.
    fun clearSignupErrors() {
        _usernameError.value = null
        _emailError.value = null
        _phoneError.value = null
        _passwordError.value = null
    }
}