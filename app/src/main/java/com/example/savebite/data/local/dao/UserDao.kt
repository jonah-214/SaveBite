package com.example.savebite.data.local.dao

import androidx.room.*
import com.example.savebite.data.local.entity.User
import kotlinx.coroutines.flow.Flow

// Room database operations - User
@Dao
interface UserDao {

    // Insert a user into the database
    @Insert
    suspend fun insertUser(user: User): Long

    // Get all users from the database
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
}