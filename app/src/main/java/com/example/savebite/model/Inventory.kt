package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_table")
data class Inventory(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(), // Unique UUID string
    val name: String,
    val description: String = "",
    val category: String = "General",
    val storage: String,
    val quantity: Int,
    val daysLeft: Int,
    val purchaseDate: String = "",
    val expiry: String,
    val notes: String = ""
)