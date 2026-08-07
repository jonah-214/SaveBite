package com.example.savebite.utils

import java.security.MessageDigest

// Hashing password
object PasswordHasher {
    // Hash a password using SHA-256 algorithm
    fun hash(password: String): String {
        // Convert password to bytes
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        // Convert bytes to hexadecimal string
        return bytes.joinToString("") { "%02x".format(it) }
    }
}