package com.example.savebite.utils

object Validators {
    // Check for email format
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    
    // Check for letters, numbers, and underscores only
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9_]+$")

    // Check for Malaysian phone format starting with 60
    private val MY_PHONE_REGEX = Regex("^60[0-9]{8,10}$")

    // Validation for username
    fun validateUsername(username: String): String? {
        if (username.isBlank()) return "Username is required"
        if (username.length !in 3..20) return "Username must be 3-20 characters"
        if (!USERNAME_REGEX.matches(username)) return "Username can only contain letters, numbers, and underscores"
        if (username.contains("__")) return "Username cannot contain consecutive underscores"
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
        if (!MY_PHONE_REGEX.matches(phone)) {
            return "Enter a valid Malaysian phone number (e.g. 60123456789)"
        }
        return null
    }

    // Malaysian phone number normalization
    fun normalizeMalaysianPhone(rawInput: String): String {
        var digits = rawInput.trim().replace(" ", "").replace("-", "")
        if (digits.startsWith("+")) digits = digits.substring(1)

        return when {
            digits.startsWith("60") -> digits
            digits.startsWith("0")  -> "60" + digits.substring(1) // 0123456789 -> 60123456789
            else -> "60$digits" // 123456789 -> 60123456789
        }
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

    // Validation for profile picture
    fun validateImage(
        bytes: ByteArray,
        mimeType: String?
    ): String? {
        val maxSize = 2 * 1024 * 1024 // 2MB
        if (bytes.size > maxSize) return "Image size must be less than 2MB"

        val allowedTypes = listOf("image/jpeg", "image/png", "image/webp", "image/jpg")
        if (mimeType != null && !allowedTypes.contains(mimeType.lowercase())) {
            return "Only JPEG, JPG, PNG, and WEBP formats are allowed"
        }
        return null
    }
}