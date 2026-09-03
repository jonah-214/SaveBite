package com.example.savebite.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.SupabaseDataRepository
import com.example.savebite.utils.ExpiryGrouping
import com.example.savebite.utils.NotificationPreferenceManager

class ExpiryReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // Background task execution
    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val inventoryRepository = InventoryRepository(
            database.inventoryDao(),
            database.storageDao(),
            database.reportDao(),
            SupabaseDataRepository()
        )

        return try {
            // Check notification preference
            val notificationPreferenceManager = NotificationPreferenceManager(applicationContext)
            if (!notificationPreferenceManager.isNotificationEnabled()) {
                return Result.success()
            }

            // Update expiry days
            inventoryRepository.cleanupExpiredItems()

            // Fetch items expiring soon via repository
            val expiringSoonNames = inventoryRepository.getExpiringItemNames(
                thresholdDays = ExpiryGrouping.SOON_THRESHOLD_DAYS
            )

            // Show notification if items found
            if (expiringSoonNames.isNotEmpty()) {
                showExpiryNotification(applicationContext, expiringSoonNames)
            }

            Result.success() // Task success
        } catch (e: Exception) {
            Result.failure() // Task failure
        }
    }
}
