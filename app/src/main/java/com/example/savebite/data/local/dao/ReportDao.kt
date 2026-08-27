package com.example.savebite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    // 1. 支持单条插入与批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportItem(item: ReportItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportItems(items: List<ReportItem>)

    // 2. 根据时间范围查询所有记录（包含 Wasted 和 Consumed）
    @Query("SELECT * FROM report_table WHERE recordTimestamp BETWEEN :startTimestamp AND :endTimestamp")
    fun getReportItemsInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<ReportItem>>

    // 3. 根据时间范围和状态（WASTED 或 CONSUMED）筛选查询
    @Query("SELECT * FROM report_table WHERE status = :status AND recordTimestamp BETWEEN :startTimestamp AND :endTimestamp")
    fun getReportItemsByStatusInRange(
        status: ReportStatus,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<List<ReportItem>>

    // 4. 获取自某个时间点之后的所有记录
    @Query("SELECT * FROM report_table WHERE recordTimestamp >= :startTimestamp")
    fun getReportItemsSince(startTimestamp: Long): Flow<List<ReportItem>>
}