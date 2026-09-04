package com.example.savebite.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.savebite.SaveBiteApp
import com.example.savebite.utils.ExpiryGrouping
import com.example.savebite.utils.NotificationPreferenceManager

// Background worker responsible for checking food expiry dates and triggering notifications
// Runs periodically to ensure users are alerted even when the app is not in the foreground
class ExpiryReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    /*
     * Executes the background check for expiring food items
     * 1. Verifies if notifications are enabled in user preferences
     * 2. Triggers a local inventory cleanup (updates days left and marks expired items)
     * 3. Fetches names of items expiring within the "Soon" threshold
     * 4. Displays a system notification if urgent items are found
     */
    override suspend fun doWork(): Result {
        // Retrieve the centralized InventoryRepository from the AppContainer
        val app = applicationContext as SaveBiteApp
        val inventoryRepository = app.container.inventoryRepository

        return try {
            // Check notification preference
            val notificationPreferenceManager = NotificationPreferenceManager(applicationContext)
            if (!notificationPreferenceManager.isNotificationEnabled()) {
                return Result.success()
            }

            // Normalize and update expiry days left for all items
            inventoryRepository.cleanupExpiredItems()

            // Identify items that have entered the "Soon" expiry window
            val expiringSoonNames = inventoryRepository.getExpiringItemNames(
                thresholdDays = ExpiryGrouping.SOON_THRESHOLD_DAYS
            )

            // Show notification if items found
            if (expiringSoonNames.isNotEmpty()) {
                showExpiryNotification(applicationContext, expiringSoonNames)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}