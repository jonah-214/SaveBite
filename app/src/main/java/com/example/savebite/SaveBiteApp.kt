package com.example.savebite

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.savebite.data.AppContainer
import com.example.savebite.data.DefaultAppContainer

class SaveBiteApp : Application() {

    // AppContainer instance used by the rest of the app to get dependencies
    lateinit var container: AppContainer

    companion object {
        const val EXPIRY_CHANNEL_ID = "expiry_reminders"
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize the Dependency Injection container
        container = DefaultAppContainer(this)
        createExpiryNotificationChannel()
    }

    private fun createExpiryNotificationChannel() {
        // Step 1: Notifications need a "Channel" to live in (Android 8.0+).
        // This lets the user control this specific category of alerts in their phone settings.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EXPIRY_CHANNEL_ID,
                "Expiry Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts you when inventory items are close to their expiry date"
            }

            // Step 2: Register the channel with the Android System.
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}