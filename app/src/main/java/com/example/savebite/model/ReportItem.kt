package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report_table")
data class ReportItem(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val quantity: Int,
    val unit: String,
    val price: Double = 0.0,
    val recordTimestamp: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.WASTED,
    val reason: String
)

enum class ReportStatus {
    WASTED,
    CONSUMED
}