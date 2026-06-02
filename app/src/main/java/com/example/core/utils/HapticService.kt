package com.example.core.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.StreaklyApp
import com.example.providers.SettingsProvider

object HapticService {
    private val vibrator: Vibrator? by lazy {
        try {
            StreaklyApp.instance.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } catch (e: Exception) {
            null
        }
    }

    fun lightImpact() {
        if (!SettingsProvider.hapticEnabled) return
        vibrate(durationMs = 12, amplitude = 40)
    }

    fun mediumImpact() {
        if (!SettingsProvider.hapticEnabled) return
        vibrate(durationMs = 25, amplitude = 100)
    }

    fun heavyImpact() {
        if (!SettingsProvider.hapticEnabled) return
        vibrate(durationMs = 50, amplitude = 200)
    }

    fun selectionClick() {
        if (!SettingsProvider.hapticEnabled) return
        vibrate(durationMs = 15, amplitude = 70)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmplitude = amplitude.coerceIn(1, 255)
                v.vibrate(VibrationEffect.createOneShot(durationMs, clampedAmplitude))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
