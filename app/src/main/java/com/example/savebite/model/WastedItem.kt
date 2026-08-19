package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wasted_table")
data class WastedItem(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val quantity: Int,
    val unit: String,
    val wastedDateTimestamp: Long = System.currentTimeMillis()
)
