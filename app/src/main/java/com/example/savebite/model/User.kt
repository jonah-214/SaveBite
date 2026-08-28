package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room database table - User
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val supabaseUid: String? = null, // Supabase Auth UUID
    val username: String,
    val email: String,
    val phone: String,
    val passwordHash: String // Reserved for a possible future offline-login fallback
)