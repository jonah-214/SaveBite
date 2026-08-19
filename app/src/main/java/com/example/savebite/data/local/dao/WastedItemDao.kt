package com.example.savebite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.savebite.model.WastedItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WastedItemDao {
    @Insert
    suspend fun insertWastedItem(item: WastedItem)

    @Query("SELECT * FROM wasted_table WHERE wastedDateTimestamp >= :startTimestamp AND wastedDateTimestamp <= :endTimestamp")
    fun getWastedInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<WastedItem>>

    @Query("SELECT * FROM wasted_table WHERE wastedDateTimestamp >= :startTimestamp")
    fun getWastedSince(startTimestamp: Long): Flow<List<WastedItem>>
}
