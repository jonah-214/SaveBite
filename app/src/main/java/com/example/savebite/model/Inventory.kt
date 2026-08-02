package com.example.savebite.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

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