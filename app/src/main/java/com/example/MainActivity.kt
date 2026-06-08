package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.navigation.AppNavHost

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sharedPrefs = getSharedPreferences("streakly_prefs", MODE_PRIVATE)
        val onboardingComplete = sharedPrefs.getBoolean("onboarding_complete", false)
        val startDestination = if (onboardingComplete) "main" else "onboarding"
        setContent {
            AppNavHost(
                modifier = Modifier.fillMaxSize(),
                startDestination = startDestination
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val app = StreaklyApp.instance
        lifecycleScope.launch {
            try {
                val settings = app.settingsRepository.getSettings()
                if (settings != null && settings.notificationsEnabled) {
                    if (!app.notificationService.isReminderScheduled()) {
                        app.notificationService.scheduleDailyReminder(
                            settings.reminderHour,
                            settings.reminderMinute
                        )
                        android.util.Log.d("MainActivity", "Rescheduled alarm in onResume")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error checking alarm status on resume: ${e.message}")
            }
        }
    }
}

