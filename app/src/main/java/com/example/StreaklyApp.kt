package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repositories.ReflectionRepository
import com.example.data.repositories.SettingsRepository
import com.example.data.repositories.StreakRepository
import com.example.data.repositories.TaskRepository

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

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        taskRepository = TaskRepository(database.taskDao())
        streakRepository = StreakRepository(database.streakDao())
        reflectionRepository = ReflectionRepository(database.reflectionDao())
        settingsRepository = SettingsRepository(database.settingsDao())
    }

    companion object {
        lateinit var instance: StreaklyApp
            private set
    }
}
