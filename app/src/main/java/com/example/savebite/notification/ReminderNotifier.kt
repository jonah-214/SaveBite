package com.example.savebite.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.savebite.MainActivity
import com.example.savebite.R
import com.example.savebite.SaveBiteApp

// Notification ID to update existing notification
private const val EXPIRY_NOTIFICATION_ID = 1001

fun showExpiryNotification(context: Context, expiringItems: List<String>) {
    if (expiringItems.isEmpty()) return

    // Intent to open app when clicked
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // Prepare notification message
    val expiringCount = expiringItems.size
    val firstItem = expiringItems.first()
    
    val text = when (expiringCount) {
        1 -> context.getString(R.string.notification_expiry_single, firstItem)
        2 -> context.getString(R.string.notification_expiry_double, firstItem)
        else -> context.getString(R.string.notification_expiry_multiple, firstItem, expiringCount - 1)
    }

    // Build notification
    val notification = NotificationCompat.Builder(context, SaveBiteApp.EXPIRY_CHANNEL_ID)
        .setSmallIcon(R.drawable.notifications)
        .setContentTitle(context.getString(R.string.notification_expiry_title))
        .setContentText(text)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH) // Set high priority
        .build()

    // Send notification
    val notificationManager = NotificationManagerCompat.from(context)
    if (notificationManager.areNotificationsEnabled()) {
        notificationManager.notify(EXPIRY_NOTIFICATION_ID, notification)
    }
}
