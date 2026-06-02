package com.example.data.repositories

import com.example.data.database.TaskDao
import com.example.data.models.TaskModel
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    fun getTasksForDate(dateKey: String): Flow<List<TaskModel>> {
        return taskDao.getTasksForDateFlow(dateKey)
    }

    suspend fun getTasksForDateList(dateKey: String): List<TaskModel> {
        return taskDao.getTasksForDateList(dateKey)
    }

    fun getAllTasks(): Flow<List<TaskModel>> {
        return taskDao.getAllTasksFlow()
    }

    suspend fun getAllTasksList(): List<TaskModel> {
        return taskDao.getAllTasksList()
    }

    suspend fun insertTask(task: TaskModel) {
        taskDao.insertTask(task)
    }

    suspend fun deleteTask(id: String) {
        taskDao.deleteTaskById(id)
    }

    suspend fun deleteAllTasks() {
        taskDao.deleteAllTasks()
    }
}
