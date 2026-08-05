package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storage_table")
data class Storage(
    @PrimaryKey
    val name: String
)