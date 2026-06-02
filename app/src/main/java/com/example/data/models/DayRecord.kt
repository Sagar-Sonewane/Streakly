package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_records")
data class DayRecord(
    @PrimaryKey val dateKey: String, // yyyy-MM-dd
    val tasksCompleted: Int,
    val tasksTotal: Int,
    val completionPct: Double
)
