package com.example.core.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.StreaklyApp

class ScheduledNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ScheduledNotificationReceiver", "Alarm received!")

        val isTest = intent.getBooleanExtra("is_test", false)
        val hour = intent.getIntExtra("hour", 21)
        val minute = intent.getIntExtra("minute", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isTest) "Streakly Test Notification" else "Stay on Track! 🔥"
        val message = if (isTest) "Your notification system is working perfectly." else "Time to log your habits and keep your streaks alive!"

        val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/${com.example.R.raw.notification}")
        val notification = NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(com.example.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .build()

        notificationManager.notify(NotificationService.NOTIFICATION_ID, notification)

        if (!isTest) {
            // Reschedule next exact alarm for tomorrow at the same time
            val notificationService = StreaklyApp.instance.notificationService
            notificationService.scheduleDailyReminder(hour, minute)
            Log.d("ScheduledNotificationReceiver", "Rescheduled next daily alarm for tomorrow at $hour:$minute")
        }
    }
}
