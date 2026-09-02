package com.example.savebite.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.savebite.MainActivity
import com.example.savebite.R
import com.example.savebite.SaveBiteApp

// Fixed ID: reusing it means a second call UPDATES this notification
// instead of stacking a new one every time the worker runs.
private const val EXPIRY_NOTIFICATION_ID = 1001

fun showExpiryNotification(context: Context, expiringCount: Int) {
    // Step 1: Create an "Intent". This tells Android what to do when the notification is clicked.
    // Here, we want to open the MainActivity.
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    
    // A PendingIntent is like a "voucher" you give to the system to run your code later.
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // Step 2: Prepare the text message to show to the user.
    val text = if (expiringCount == 1) {
        "1 item is expiring soon"
    } else {
        "$expiringCount items are expiring soon"
    }

    // Step 3: Build the visual notification object.
    val notification = NotificationCompat.Builder(context, SaveBiteApp.EXPIRY_CHANNEL_ID)
        .setSmallIcon(R.drawable.notifications) // The icon shown in the status bar
        .setContentTitle("Food waste alert")    // The bold title
        .setContentText(text)                   // The detailed message
        .setContentIntent(pendingIntent)        // What happens on click
        .setAutoCancel(true)                    // Remove notification after user clicks it
        .build()

    // Step 4: Actually send the notification to the system tray.
    val notificationManager = NotificationManagerCompat.from(context)
    if (notificationManager.areNotificationsEnabled()) {
        notificationManager.notify(EXPIRY_NOTIFICATION_ID, notification)
    }
}
