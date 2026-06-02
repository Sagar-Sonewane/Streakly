package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reflections")
data class ReflectionModel(
    @PrimaryKey val id: String,
    val dateKey: String, // yyyy-MM-dd
    val moodEmoji: String,
    val moodIndex: Int, // 0 = Low, 1 = Okay, 2 = Good, 3 = Epic
    val text: String,
    val createdAt: Long // timestamp
)
