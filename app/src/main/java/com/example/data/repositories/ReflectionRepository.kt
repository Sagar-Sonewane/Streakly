package com.example.data.repositories

import com.example.data.database.ReflectionDao
import com.example.data.models.ReflectionModel
import kotlinx.coroutines.flow.Flow

class ReflectionRepository(private val reflectionDao: ReflectionDao) {
    fun getAllReflectionsFlow(): Flow<List<ReflectionModel>> {
        return reflectionDao.getAllReflectionsFlow()
    }

    suspend fun getReflectionForDate(dateKey: String): ReflectionModel? {
        return reflectionDao.getReflectionForDate(dateKey)
    }

    fun getReflectionForDateFlow(dateKey: String): Flow<ReflectionModel?> {
        return reflectionDao.getReflectionForDateFlow(dateKey)
    }

    suspend fun saveReflection(reflection: ReflectionModel) {
        reflectionDao.insertOrUpdateReflection(reflection)
    }

    suspend fun deleteReflectionById(id: String) {
        reflectionDao.deleteReflectionById(id)
    }

    suspend fun deleteAllReflections() {
        reflectionDao.deleteAllReflections()
    }
}
