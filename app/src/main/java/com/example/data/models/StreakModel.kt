package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class StreakModel(
    @PrimaryKey val id: Int = 1, // Single entry row
    val currentStreak: Int,
    val longestStreak: Int,
    val totalStreakDays: Int,
    val lastActiveDate: String?, // yyyy-MM-dd
    val milestonesClaimedStr: String // Comma separated string of milestones claimed, e.g. "3,7,10"
) {
    fun getMilestonesClaimedList(): List<String> {
        if (milestonesClaimedStr.isBlank()) return emptyList()
        return milestonesClaimedStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    companion object {
        fun default(): StreakModel {
            return StreakModel(
                id = 1,
                currentStreak = 0,
                longestStreak = 0,
                totalStreakDays = 0,
                lastActiveDate = null,
                milestonesClaimedStr = ""
            )
        }
    }
}
