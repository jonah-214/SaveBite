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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportItem(item: ReportItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportItems(items: List<ReportItem>)

    @Query("SELECT * FROM report_table WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp")
    fun getReportItemsInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<ReportItem>>

    @Query("SELECT * FROM report_table WHERE status = :status AND timestamp BETWEEN :startTimestamp AND :endTimestamp")
    fun getReportItemsByStatusInRange(
        status: ReportStatus,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<List<ReportItem>>

    @Query("SELECT * FROM report_table WHERE timestamp >= :startTimestamp")
    fun getReportItemsSince(startTimestamp: Long): Flow<List<ReportItem>>
}
