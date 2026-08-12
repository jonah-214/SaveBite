package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.UserDao
import com.example.savebite.model.User

class UserRepository(private val userDao: UserDao) {
    // Insert user
    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    // Get user by email or phone
    suspend fun getUserByEmailOrPhone(identifier: String): User? {
        return userDao.getUserByEmailOrPhone(identifier)
    }

    // Get user by email
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    // Get user by phone
    suspend fun getUserByPhone(phone: String): User? {
        return userDao.getUserByPhone(phone)
    }

    // Get user by username
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    // Get user by ID
    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    // Get user by username, excluding a specific ID
    suspend fun getUserByUsernameExcludingId(username: String, excludeId: Int): User? {
        return userDao.getUserByUsernameExcludingId(username, excludeId)
    }

    // Get user by email, excluding a specific ID
    suspend fun getUserByEmailExcludingId(email: String, excludeId: Int): User? {
        return userDao.getUserByEmailExcludingId(email, excludeId)
    }

    // Get user by phone, excluding a specific ID
    suspend fun getUserByPhoneExcludingId(phone: String, excludeId: Int): User? {
        return userDao.getUserByPhoneExcludingId(phone, excludeId)
    }

    // Update user
    suspend fun updateUser(user: User): Int {
        return userDao.updateUser(user)
    }
}