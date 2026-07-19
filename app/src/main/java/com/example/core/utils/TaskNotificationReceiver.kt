package com.example.core.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class TaskNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val taskName = intent.getStringExtra("task_name") ?: "Habit"
        val taskEmoji = intent.getStringExtra("task_emoji") ?: "🎯"
        val hour = intent.getIntExtra("hour", 8)
        val minute = intent.getIntExtra("minute", 0)

        Log.d("TaskNotificationReceiver", "Task alarm received for $taskName")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.notification}")
        val notification = NotificationCompat.Builder(context, "streakly_task_reminders")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time for $taskName!")
            .setContentText("Keep the streak alive — check it off now")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .build()

        notificationManager.notify(taskId.hashCode(), notification)

        // Reschedule next exact alarm for tomorrow at the same time
        NotificationHelper.scheduleTaskNotification(taskId, taskName, taskEmoji, hour, minute)
    }
}
