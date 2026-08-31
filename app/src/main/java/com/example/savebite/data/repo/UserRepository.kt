package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.UserDao
import com.example.savebite.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    // Insert user
    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    // Get user by Supabase UID
    suspend fun getUserBySupabaseUid(uid: String): User? {
        return userDao.getUserBySupabaseUid(uid)
    }

    // Get user by phone
    suspend fun getUserByPhone(phone: String): User? {
        return userDao.getUserByPhone(phone)
    }

    // Get user by ID as a Flow
    fun getUserByIdFlow(id: Int): Flow<User?> {
        return userDao.getUserByIdFlow(id)
    }

    // Update user
    suspend fun updateUser(user: User): Int {
        return userDao.updateUser(user)
    }
}