package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsModel(
    @PrimaryKey val id: Int = 1, // Single entry row
    val accentColorIndex: Int = 0, // 0=orange, 1=blue, 2=purple, 3=green, 4=amber, 5=red
    val language: String = "en", // "en", "hi", "mr"
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 7,
    val reminderMinute: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val themeModeIndex: Int = 2 // 0 = system, 1 = light, 2 = dark
) {
    companion object {
        fun default(): SettingsModel {
            return SettingsModel()
        }
    }
}
