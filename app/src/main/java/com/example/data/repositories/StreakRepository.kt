package com.example.data.repositories

import com.example.data.database.StreakDao
import com.example.data.models.DayRecord
import com.example.data.models.StreakModel
import kotlinx.coroutines.flow.Flow

class StreakRepository(private val streakDao: StreakDao) {
    fun getStreakFlow(): Flow<StreakModel?> {
        return streakDao.getStreakFlow()
    }

    suspend fun getStreak(): StreakModel? {
        return streakDao.getStreak()
    }

    suspend fun saveStreak(streak: StreakModel) {
        streakDao.insertOrUpdateStreak(streak)
    }

    fun getAllDayRecordsFlow(): Flow<List<DayRecord>> {
        return streakDao.getAllDayRecordsFlow()
    }

    suspend fun getAllDayRecordsList(): List<DayRecord> {
        return streakDao.getAllDayRecordsList()
    }

    suspend fun getDayRecordForDate(dateKey: String): DayRecord? {
        return streakDao.getDayRecord(dateKey)
    }

    suspend fun saveDayRecord(record: DayRecord) {
        streakDao.insertOrUpdateDayRecord(record)
    }

    suspend fun resetAllStreakData() {
        streakDao.deleteStreak()
        streakDao.deleteAllDayRecords()
    }
}
