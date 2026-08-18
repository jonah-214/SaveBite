package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room database table - User
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val supabaseUid: String? = null, // Maps to Supabase Auth UUID
    val username: String,
    val email: String,
    val phone: String,
    val passwordHash: String // Keep for offline fallback
)