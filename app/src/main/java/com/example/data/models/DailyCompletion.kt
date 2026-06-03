package com.example.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_completions",
    indices = [Index(value = ["taskId", "dateKey"], unique = true)]
)
data class DailyCompletion(
    @PrimaryKey val id: String,
    val taskId: String,
    val dateKey: String, // yyyy-MM-dd
    val isCompleted: Boolean
)
