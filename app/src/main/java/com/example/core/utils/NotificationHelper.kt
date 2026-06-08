package com.example.core.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.R
import com.example.StreaklyApp
import java.util.Calendar

object NotificationHelper {
    private const val CHANNEL_ID = "streakly_task_reminders"
    private const val CHANNEL_NAME = "Task Reminders"

    private fun getAlarmManager(context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.notification}")
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you of specific habit times"
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }
            getNotificationManager(context).createNotificationChannel(channel)
        }
    }

    fun scheduleTaskNotification(taskId: String, taskName: String, taskEmoji: String, hour: Int, minute: Int) {
        val context = StreaklyApp.instance
        createNotificationChannel(context)

        val alarmManager = getAlarmManager(context)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("task_id", taskId)
            putExtra("task_name", taskName)
            putExtra("task_emoji", taskEmoji)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val requestCode = taskId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("NotificationHelper", "Scheduled alarm for task $taskName ($taskId) at $hour:$minute")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error scheduling alarm: ${e.message}")
        }
    }

    fun cancelTaskNotification(taskId: String) {
        val context = StreaklyApp.instance
        val alarmManager = getAlarmManager(context)
        val intent = Intent(context, TaskNotificationReceiver::class.java)
        val requestCode = taskId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("NotificationHelper", "Cancelled alarm for task ID $taskId")
        }
    }
}
