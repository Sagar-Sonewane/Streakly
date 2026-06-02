package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskModel(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val timeLabel: String?,
    val colorIndex: Int,
    val isCompleted: Boolean,
    val dateKey: String, // 'yyyy-MM-dd' which day it belongs to
    val createdAt: Long,  // timestamp
    val frequency: String = "daily",     // 'daily' | 'once' | 'weekly'
    val weekDaysRaw: String = "",        // Used only when frequency == 'weekly', comma-separated: "1,2,3"
    val importance: String = "regular"   // 'regular' | 'moderate' | 'priority'
) {
    val weekDays: List<Int>
        get() = if (weekDaysRaw.isBlank()) emptyList() else weekDaysRaw.split(",").mapNotNull { it.toIntOrNull() }
}
