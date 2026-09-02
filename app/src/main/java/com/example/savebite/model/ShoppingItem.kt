package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "shopping_table")
data class ShoppingItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: Int,
    val unit: String,
    val category: String,
    val isPurchased: Boolean = false,
    val isSynced: Boolean = true,
    val isDeleted: Boolean = false
)
