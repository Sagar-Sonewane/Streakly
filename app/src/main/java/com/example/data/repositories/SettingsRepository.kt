package com.example.data.repositories

import com.example.data.database.SettingsDao
import com.example.data.models.SettingsModel
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {
    fun getSettingsFlow(): Flow<SettingsModel?> {
        return settingsDao.getSettingsFlow()
    }

    suspend fun getSettings(): SettingsModel? {
        return settingsDao.getSettings()
    }

    suspend fun saveSettings(settings: SettingsModel) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    suspend fun deleteSettings() {
        settingsDao.deleteSettings()
    }
}
