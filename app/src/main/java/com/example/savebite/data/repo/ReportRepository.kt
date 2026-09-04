package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.ReportDao
import com.example.savebite.data.remote.toRoom
import com.example.savebite.data.remote.toSupabase
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    // Returns a stream of report items within a specific timestamp range
    fun getReportItemsInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<ReportItem>>
    
    // Returns a stream of report items filtered by status and range
    fun getReportItemsByStatusInRange(status: ReportStatus, startTimestamp: Long, endTimestamp: Long): Flow<List<ReportItem>>
    
    // Returns a stream of report items logged since a specific timestamp
    fun getReportItemsSince(startTimestamp: Long): Flow<List<ReportItem>>
    
    // Persists a new report item and syncs it to the cloud
    suspend fun insertReportItem(item: ReportItem)
    
    // Fetches latest logs from Supabase and merges them into the local cache
    suspend fun syncFromCloud(): Result<Unit>
}

// Implementation of [ReportRepository] using Room for local storage and Supabase for remote sync
class ReportRepositoryImpl(
    private val reportDao: ReportDao,
    private val supabaseDataRepository: SupabaseDataRepository
) : ReportRepository {

    override fun getReportItemsInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<ReportItem>> {
        return reportDao.getReportItemsInRange(startTimestamp, endTimestamp)
    }

    override fun getReportItemsByStatusInRange(
        status: ReportStatus,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<List<ReportItem>> {
        return reportDao.getReportItemsByStatusInRange(status, startTimestamp, endTimestamp)
    }

    override fun getReportItemsSince(startTimestamp: Long): Flow<List<ReportItem>> {
        return reportDao.getReportItemsSince(startTimestamp)
    }

    // Persists a report item locally to Room first for immediate offline availability, then pushes it to Supabase.
    override suspend fun insertReportItem(item: ReportItem) {
        reportDao.insertReportItem(item)
        supabaseDataRepository.insertReportItem(item.toSupabase())
    }

    // Fetches historical report records from Supabase and performs an upsert into local Room storage.
    override suspend fun syncFromCloud(): Result<Unit> {
        val remoteResult = supabaseDataRepository.fetchReportItems()
        return if (remoteResult.isSuccess) {
            val remoteItems = remoteResult.getOrDefault(emptyList())
            remoteItems.forEach { supabaseItem ->
                reportDao.insertReportItem(supabaseItem.toRoom())
            }
            Result.success(Unit)
        } else {
            Result.failure(remoteResult.exceptionOrNull() ?: Exception("Sync failed"))
        }
    }
}