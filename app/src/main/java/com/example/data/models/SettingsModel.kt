package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsModel(
    @PrimaryKey val id: Int = 1, // Single entry row
    val accentColorIndex: Int = 4, // 0=neon cyan, 1=electric blue, 2=purple pulse, 3=emerald green, 4=sunset orange, 5=rose pink
    val language: String = "en", // "en", "hi", "mr"
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 7,
    val reminderMinute: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val themeModeIndex: Int = 0 // 0 = system, 1 = light, 2 = dark
) {
    companion object {
        fun default(): SettingsModel {
            return SettingsModel()
        }
    }
}
