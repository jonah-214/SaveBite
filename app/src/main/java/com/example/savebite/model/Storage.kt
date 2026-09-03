package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storage_table")
data class Storage(
    @PrimaryKey
    val name: String
)

/**
 * The storage locations every new user starts with, and the fallback location that
 * items get reassigned to when a custom storage is deleted. Kept in one place so the
 * seed data (AppDatabase), the Inventory screens, and the delete-fallback logic can't
 * drift out of sync with each other.
 */
object DefaultStorages {
    const val FALLBACK = "Refrigerator"
    val ALL = listOf(FALLBACK, "Pantry", "Freezer")
}