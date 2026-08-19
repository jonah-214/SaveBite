package com.example.savebite.utils

object Validators {
    // Check for email format (something@domain.com)
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    
    // Check for letters, numbers, and underscores only
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9_]+$")

    // Check for Malaysian phone format starting with 60 (e.g., 60123456789)
    private val MY_PHONE_REGEX = Regex("^60[0-9]{8,9}$")

    // Validation for username
    fun validateUsername(username: String): String? {
        if (username.isBlank()) return "Username is required"
        if (username != username.trim()) return "Username cannot start or end with spaces"
        if (username.length < 3 || username.length > 20) return "Username must be 3-20 characters"
        if (!USERNAME_REGEX.matches(username)) return "Username can only contain letters, numbers, and underscores"
        if (username.contains("__")) return "Username cannot contain spaces"
        return null
    }

    // Validation for email
    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email is required"
        if (!EMAIL_REGEX.matches(email)) return "Enter a valid email address"
        return null
    }

    // Validation for phone number
    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) return "Phone number is required"
        val cleaned = phone.replace(" ", "").replace("-", "")
        if (!MY_PHONE_REGEX.matches(cleaned)) {
            return "Enter a valid Malaysian phone number (e.g. 60123456789)"
        }
        return null
    }

    // Validation for password
    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "Password is required"
        if (password.length < 6) return "Password must be at least 6 characters"
        if (!password.any { it.isUpperCase() }) return "Include at least one uppercase letter"
        if (!password.any { it.isLowerCase() }) return "Include at least one lowercase letter"
        if (!password.any { it.isDigit() }) return "Include at least one number"
        return null
    }
}