package com.example.providers

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.StreaklyApp

object Providers {
    
    @Suppress("UNCHECKED_CAST")
    class Factory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return creator() as T
        }
    }

    @Composable
    fun getSettings(): SettingsProvider {
        return viewModel(
            factory = Factory {
                SettingsProvider(StreaklyApp.instance.settingsRepository)
            }
        )
    }

    @Composable
    fun getStreak(): StreakProvider {
        return viewModel(
            factory = Factory {
                StreakProvider(StreaklyApp.instance.streakRepository)
            }
        )
    }

    @Composable
    fun getTask(): TaskProvider {
        val streakProvider = getStreak()
        return viewModel(
            factory = Factory {
                TaskProvider(
                    StreaklyApp.instance.taskRepository,
                    StreaklyApp.instance.dailyCompletionRepository,
                    streakProvider
                )
            }
        )
    }

    @Composable
    fun getReflection(): ReflectionProvider {
        return viewModel(
            factory = Factory {
                ReflectionProvider(StreaklyApp.instance.reflectionRepository)
            }
        )
    }
}
