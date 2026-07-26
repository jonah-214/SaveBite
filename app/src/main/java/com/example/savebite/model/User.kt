package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room database table - User
@Entity(tableName = "users")
data class User(
    // Primary key - assign unique id to each user
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String
)