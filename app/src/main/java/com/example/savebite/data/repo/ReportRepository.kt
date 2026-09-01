package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.ReportDao
import com.example.savebite.data.remote.toRoom
import com.example.savebite.data.remote.toSupabase
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getReportItemsInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<ReportItem>>
    fun getReportItemsByStatusInRange(status: ReportStatus, startTimestamp: Long, endTimestamp: Long): Flow<List<ReportItem>>
    fun getReportItemsSince(startTimestamp: Long): Flow<List<ReportItem>>
    suspend fun insertReportItem(item: ReportItem)
    suspend fun syncFromCloud(): Result<Unit>
}

class ReportRepositoryImpl(
    private val reportDao: ReportDao,
    private val supabaseDataRepository: SupabaseDataRepository = SupabaseDataRepository()
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

    override suspend fun insertReportItem(item: ReportItem) {
        reportDao.insertReportItem(item)
        supabaseDataRepository.insertReportItem(item.toSupabase())
    }

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