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
    //Single insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportItem(item: ReportItem)

    //Batch inserts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportItems(items: List<ReportItem>)

    // Observes all logged report entries within a specific timestamp window.
    // Emits dynamic updates whenever report data within the specified timeframe changes.
    @Query("SELECT * FROM report_table WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp")
    fun getReportItemsInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<ReportItem>>

    // Observes logged items filtered by both status (e.g., CONSUMED vs. WASTED) and a target date range.
    @Query("SELECT * FROM report_table WHERE status = :status AND timestamp BETWEEN :startTimestamp AND :endTimestamp")
    fun getReportItemsByStatusInRange(
        status: ReportStatus,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<List<ReportItem>>

    // Streams all report entries logged after a given starting timestamp.
    @Query("SELECT * FROM report_table WHERE timestamp >= :startTimestamp")
    fun getReportItemsSince(startTimestamp: Long): Flow<List<ReportItem>>
}
