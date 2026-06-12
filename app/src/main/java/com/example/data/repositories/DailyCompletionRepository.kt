package com.example.data.repositories

import com.example.data.database.DailyCompletionDao
import com.example.data.models.DailyCompletion
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DailyCompletionRepository(private val dailyCompletionDao: DailyCompletionDao) {

    fun getCompletionsForDateFlow(dateKey: String): Flow<List<DailyCompletion>> {
        return dailyCompletionDao.getCompletionsForDateFlow(dateKey)
    }

    suspend fun getCompletionsForDateList(dateKey: String): List<DailyCompletion> {
        return dailyCompletionDao.getCompletionsForDateList(dateKey)
    }

    suspend fun isTaskCompleted(taskId: String, dateKey: String): Boolean {
        val completion = dailyCompletionDao.getCompletionForTaskAndDate(taskId, dateKey)
        return completion?.isCompleted ?: false
    }

    suspend fun setTaskCompleted(taskId: String, dateKey: String, isCompleted: Boolean) {
        val existing = dailyCompletionDao.getCompletionForTaskAndDate(taskId, dateKey)
        if (existing != null) {
            dailyCompletionDao.insertOrUpdateCompletion(existing.copy(isCompleted = isCompleted))
        } else {
            val newRecord = DailyCompletion(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                dateKey = dateKey,
                isCompleted = isCompleted
            )
            dailyCompletionDao.insertOrUpdateCompletion(newRecord)
        }
    }

    suspend fun deleteCompletionsForTask(taskId: String) {
        dailyCompletionDao.deleteCompletionsForTask(taskId)
    }

    suspend fun getCompletionsForTaskList(taskId: String): List<DailyCompletion> {
        return dailyCompletionDao.getCompletionsForTaskList(taskId)
    }

    suspend fun pruneOldRecords(beforeDateKey: String) {
        dailyCompletionDao.pruneCompletionsOlderThanDate(beforeDateKey)
    }

    suspend fun deleteAll() {
        dailyCompletionDao.deleteAllCompletions()
    }
}
