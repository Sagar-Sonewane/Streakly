package com.example.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.StreaklyApp
import com.example.data.models.SettingsModel
import com.example.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsProvider(
    private val settingsRepository: SettingsRepository = StreaklyApp.instance.settingsRepository
) : ViewModel() {

    companion object {
        @Volatile
        var soundEnabled: Boolean = true
            private set

        @Volatile
        var hapticEnabled: Boolean = true
            private set

        fun updateCachedSettings(sound: Boolean, haptic: Boolean) {
            soundEnabled = sound
            hapticEnabled = haptic
        }
    }

    private val _settingsState = MutableStateFlow(SettingsModel.default())
    val settingsState: StateFlow<SettingsModel> = _settingsState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettingsFlow().collectLatest { settings ->
                if (settings != null) {
                    _settingsState.value = settings
                    updateCachedSettings(settings.soundEnabled, settings.hapticEnabled)
                } else {
                    // Create default settings if not exists, migrating from SharedPreferences if preferences exist
                    val sharedPrefs = StreaklyApp.instance.getSharedPreferences("streakly_prefs", android.content.Context.MODE_PRIVATE)
                    
                    val existingTheme = sharedPrefs.getString("theme_mode", null)
                    val themeIndex = when (existingTheme) {
                        "light" -> 1
                        "dark" -> 2
                        else -> 0 // 0 = System Default
                    }
                    
                    val existingAccentHex = sharedPrefs.getString("accent_color", null)
                    val accentIndex = if (existingAccentHex != null) {
                        val foundIndex = com.example.core.theme.AppColors.accentOptions.indexOfFirst { 
                            it.hex.equals(existingAccentHex, ignoreCase = true) 
                        }
                        if (foundIndex != -1) foundIndex else 4 // Orange as fallback
                    } else {
                        4 // Orange as default (Sunset Orange is index 4 in AppColors.accentOptions)
                    }

                    // Ensure SharedPreferences stays in sync on first creation
                    if (existingAccentHex == null) {
                        val defaultHex = com.example.core.theme.AppColors.accentOptions[accentIndex].hex
                        sharedPrefs.edit().putString("accent_color", defaultHex).apply()
                    }

                    val defaultSettings = SettingsModel(
                        accentColorIndex = accentIndex,
                        themeModeIndex = themeIndex
                    )
                    settingsRepository.saveSettings(defaultSettings)
                    updateCachedSettings(defaultSettings.soundEnabled, defaultSettings.hapticEnabled)
                }
            }
        }
    }

    fun updateAccentColor(index: Int) {
        viewModelScope.launch {
            val current = _settingsState.value
            settingsRepository.saveSettings(current.copy(accentColorIndex = index))
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val current = _settingsState.value
            settingsRepository.saveSettings(current.copy(language = lang))
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = _settingsState.value
            settingsRepository.saveSettings(current.copy(notificationsEnabled = enabled))
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val current = _settingsState.value
            settingsRepository.saveSettings(current.copy(reminderHour = hour, reminderMinute = minute))
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = _settingsState.value
            settingsRepository.saveSettings(current.copy(soundEnabled = enabled))
        }
    }

    fun updateHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = _settingsState.value
            settingsRepository.saveSettings(current.copy(hapticEnabled = enabled))
        }
    }

    fun updateThemeModeIndex(index: Int) {
        viewModelScope.launch {
            val current = _settingsState.value
            settingsRepository.saveSettings(current.copy(themeModeIndex = index))
        }
    }
}
