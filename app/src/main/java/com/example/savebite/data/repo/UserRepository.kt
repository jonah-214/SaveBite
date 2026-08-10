package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.UserDao
import com.example.savebite.model.User

class UserRepository(private val userDao: UserDao) {

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

    // Insert user
    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    // Get user by ID
    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }
}