package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repositories.ReflectionRepository
import com.example.data.repositories.SettingsRepository
import com.example.data.repositories.StreakRepository
import com.example.data.repositories.TaskRepository

import com.example.data.repositories.DailyCompletionRepository
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreaklyApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var taskRepository: TaskRepository
        private set

    lateinit var streakRepository: StreakRepository
        private set

    lateinit var reflectionRepository: ReflectionRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var dailyCompletionRepository: DailyCompletionRepository
        private set

    lateinit var notificationService: com.example.core.utils.NotificationService
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        taskRepository = TaskRepository(database.taskDao())
        streakRepository = StreakRepository(database.streakDao())
        reflectionRepository = ReflectionRepository(database.reflectionDao())
        settingsRepository = SettingsRepository(database.settingsDao())
        dailyCompletionRepository = DailyCompletionRepository(database.dailyCompletionDao())
        notificationService = com.example.core.utils.NotificationService(this)

        // Run data migration if needed
        val sharedPrefs = getSharedPreferences("streakly_prefs", Context.MODE_PRIVATE)
        val migrationDone = sharedPrefs.getBoolean("task_migration_v1_done", false)
        if (!migrationDone) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val allTasks = taskRepository.getAllTasksList()
                    val todayKey = com.example.core.utils.DateUtils.getTodayKey()
                    allTasks.forEach { task ->
                        if (task.frequency != "once" && task.isCompleted) {
                            dailyCompletionRepository.setTaskCompleted(task.id, todayKey, true)
                            taskRepository.insertTask(task.copy(isCompleted = false))
                        }
                    }
                    sharedPrefs.edit().putBoolean("task_migration_v1_done", true).apply()
                    Log.d("StreaklyApp", "Data migration to daily completions completed successfully")
                } catch (e: Exception) {
                    Log.e("StreaklyApp", "Error running task migration: ${e.message}")
                }
            }
        }
    }

    companion object {
        lateinit var instance: StreaklyApp
            private set
    }
}
