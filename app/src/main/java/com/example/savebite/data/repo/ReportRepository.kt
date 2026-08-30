package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.ReportDao
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getReportItemsInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<ReportItem>>
    fun getReportItemsByStatusInRange(status: ReportStatus, startTimestamp: Long, endTimestamp: Long): Flow<List<ReportItem>>
    suspend fun insertReportItem(item: ReportItem)
}

class ReportRepositoryImpl(
    private val reportDao: ReportDao
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

    override suspend fun insertReportItem(item: ReportItem) {
        reportDao.insertReportItem(item)
    }
}