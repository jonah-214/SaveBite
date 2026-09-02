package com.example.savebite.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.utils.ExpiryGrouping
import com.example.savebite.utils.ExpirySection

class ExpiryReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // This method runs in the background, even if the app is closed!
    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val inventoryRepository = InventoryRepository(
            database.inventoryDao(),
            database.storageDao(),
            database.reportDao()
        )

        return try {
            // 1. Refresh daysLeft for every item first — without this, items whose
            //    expiry has crept closer since the Inventory screen was last opened
            //    would be checked against a stale, outdated daysLeft value.
            inventoryRepository.cleanupExpiredItems()

            // 2. Get all food items from the local database (now up to date)
            val allItems = database.inventoryDao().getAllInventorySync()

            // 3. Filter for items that are "expiring soon"
            val grouped = ExpiryGrouping.group(allItems)
            val expiringSoon = grouped[ExpirySection.SOON].orEmpty()

            // 4. If we found any, trigger the notification to show up
            if (expiringSoon.isNotEmpty()) {
                showExpiryNotification(applicationContext, expiringSoon.size)
            }

            Result.success() // Task completed successfully
        } catch (e: Exception) {
            Result.failure() // Something went wrong, try again later
        }
    }
}
