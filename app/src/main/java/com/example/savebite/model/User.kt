package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val supabaseUid: String? = null, // Supabase Auth UUID
    val username: String,
    val email: String,
    val phone: String,
    val avatarUrl: String? = null
)