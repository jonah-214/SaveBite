package com.example.savebite.repo

import com.example.savebite.data.dao.UserDao
import com.example.savebite.model.User
import com.example.savebite.utils.PasswordHasher

// Repository for user operations
class UserRepository(private val userDao: UserDao) {

    // Login user
    suspend fun login(identifier: String, password: String): User? {
        // Check if user exists and password is correct
        val user = userDao.getUserByEmailOrPhone(identifier) ?: return null
        // Hash password and compare with stored hash
        val hashedInput = PasswordHasher.hash(password)
        // Return user if password is correct, otherwise return null
        return if (user.passwordHash == hashedInput) user else null
    }

    // Signup user
    suspend fun signup(username: String, email: String, phone: String, password: String): Result<Unit> {
        // Check if email or phone number is already registered
        if (userDao.getUserByEmail(email) != null) {
            return Result.failure(Exception("Email already registered"))
        }
        if (userDao.getUserByPhone(phone) != null) {
            return Result.failure(Exception("Phone number already registered"))
        }
        // Hash password and create new user
        val newUser = User(
            username = username,
            email = email,
            phone = phone,
            passwordHash = PasswordHasher.hash(password)
        )
        // Insert new user into database
        userDao.insertUser(newUser)
        return Result.success(Unit)
    }
}