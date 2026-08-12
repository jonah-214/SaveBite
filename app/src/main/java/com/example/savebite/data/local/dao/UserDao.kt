package com.example.savebite.data.local.dao

import androidx.room.*
import com.example.savebite.model.User

// Room database operations - User
@Dao
interface UserDao {

    // Insert a user into the database
    @Insert
    suspend fun insertUser(user: User): Long

    // Get a user by email or phone number
    @Query("SELECT * FROM users WHERE email = :identifier OR phone = :identifier LIMIT 1")
    suspend fun getUserByEmailOrPhone(identifier: String): User?

    // Get a user by their email
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // Get a user by their phone number
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    // Get a user by their username
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    // Get a user by their ID
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    // Get a user by their username, excluding a specific ID
    @Query("SELECT * FROM users WHERE username = :username AND id != :excludeId LIMIT 1")
    suspend fun getUserByUsernameExcludingId(username: String, excludeId: Int): User?

    // Get a user by their email, excluding a specific ID
    @Query("SELECT * FROM users WHERE email = :email AND id != :excludeId LIMIT 1")
    suspend fun getUserByEmailExcludingId(email: String, excludeId: Int): User?

    // Get a user by their phone number, excluding a specific ID
    @Query("SELECT * FROM users WHERE phone = :phone AND id != :excludeId LIMIT 1")
    suspend fun getUserByPhoneExcludingId(phone: String, excludeId: Int): User?

    // Update a user's information
    @Update
    suspend fun updateUser(user: User): Int
}