package com.example.data.database

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE dateKey = :dateKey ORDER BY createdAt ASC")
    fun getTasksForDateFlow(dateKey: String): Flow<List<TaskModel>>

    @Query("SELECT * FROM tasks WHERE dateKey = :dateKey ORDER BY createdAt ASC")
    suspend fun getTasksForDateList(dateKey: String): List<TaskModel>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasksFlow(): Flow<List<TaskModel>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun getAllTasksList(): List<TaskModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskModel)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks WHERE id = 1 LIMIT 1")
    fun getStreakFlow(): Flow<StreakModel?>

    @Query("SELECT * FROM streaks WHERE id = 1 LIMIT 1")
    suspend fun getStreak(): StreakModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streak: StreakModel)

    @Query("SELECT * FROM day_records ORDER BY dateKey DESC")
    fun getAllDayRecordsFlow(): Flow<List<DayRecord>>

    @Query("SELECT * FROM day_records ORDER BY dateKey DESC")
    suspend fun getAllDayRecordsList(): List<DayRecord>

    @Query("SELECT * FROM day_records WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDayRecord(dateKey: String): DayRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDayRecord(record: DayRecord)

    @Query("DELETE FROM streaks")
    suspend fun deleteStreak()

    @Query("DELETE FROM day_records")
    suspend fun deleteAllDayRecords()
}

@Dao
interface ReflectionDao {
    @Query("SELECT * FROM reflections ORDER BY createdAt DESC")
    fun getAllReflectionsFlow(): Flow<List<ReflectionModel>>

    @Query("SELECT * FROM reflections WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getReflectionForDate(dateKey: String): ReflectionModel?

    @Query("SELECT * FROM reflections WHERE dateKey = :dateKey LIMIT 1")
    fun getReflectionForDateFlow(dateKey: String): Flow<ReflectionModel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReflection(reflection: ReflectionModel)

    @Query("DELETE FROM reflections WHERE id = :reflectionId")
    suspend fun deleteReflectionById(reflectionId: String)

    @Query("DELETE FROM reflections")
    suspend fun deleteAllReflections()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsModel?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): SettingsModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SettingsModel)

    @Query("DELETE FROM settings")
    suspend fun deleteSettings()
}

@Dao
interface DailyCompletionDao {
    @Query("SELECT * FROM daily_completions WHERE dateKey = :dateKey")
    fun getCompletionsForDateFlow(dateKey: String): Flow<List<DailyCompletion>>

    @Query("SELECT * FROM daily_completions WHERE dateKey = :dateKey")
    suspend fun getCompletionsForDateList(dateKey: String): List<DailyCompletion>

    @Query("SELECT * FROM daily_completions WHERE taskId = :taskId AND dateKey = :dateKey LIMIT 1")
    suspend fun getCompletionForTaskAndDate(taskId: String, dateKey: String): DailyCompletion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCompletion(completion: DailyCompletion)

    @Query("DELETE FROM daily_completions WHERE taskId = :taskId")
    suspend fun deleteCompletionsForTask(taskId: String)

    @Query("DELETE FROM daily_completions WHERE dateKey < :dateKey")
    suspend fun pruneCompletionsOlderThanDate(dateKey: String)

    @Query("DELETE FROM daily_completions")
    suspend fun deleteAllCompletions()
}
