package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report_table") // 建议把表名也同步更新
data class ReportItem(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val quantity: Int,
    val unit: String,
    val price: Double = 0.0,
    val recordTimestamp: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.WASTED, // 新增状态标识
    val reason: String
)

enum class ReportStatus {
    WASTED,    // 浪费/过期丢弃
    CONSUMED   // 正常消耗/吃掉
}