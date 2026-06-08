package com.example.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.StreaklyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduledNotificationBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device boot completed, rescheduling notifications...")
            
            val app = StreaklyApp.instance
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = app.settingsRepository.getSettings()
                    if (settings != null && settings.notificationsEnabled) {
                        app.notificationService.scheduleDailyReminder(
                            settings.reminderHour,
                            settings.reminderMinute
                        )
                        Log.d("BootReceiver", "Notification rescheduled at ${settings.reminderHour}:${settings.reminderMinute}")
                    }

                    // Reschedule active task alarms
                    val allTasks = app.taskRepository.getAllTasksList()
                    allTasks.forEach { task ->
                        if (task.reminderEnabled && task.reminderHour != null && task.reminderMinute != null) {
                            NotificationHelper.scheduleTaskNotification(
                                taskId = task.id,
                                taskName = task.title,
                                taskEmoji = task.emoji,
                                hour = task.reminderHour,
                                minute = task.reminderMinute
                            )
                            Log.d("BootReceiver", "Rescheduled alarm for task ${task.title} at ${task.reminderHour}:${task.reminderMinute}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling notification: ${e.message}")
                }
            }
        }
    }
}
