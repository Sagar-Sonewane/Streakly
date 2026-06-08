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
            val context = StreaklyApp.instance
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun shouldVibrate(): Boolean {
        if (!SettingsProvider.hapticEnabled) return false
        val context = StreaklyApp.instance
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        val ringerMode = audioManager?.ringerMode ?: android.media.AudioManager.RINGER_MODE_NORMAL
        return ringerMode != android.media.AudioManager.RINGER_MODE_SILENT
    }

    private fun vibratePredefined(effectId: Int, fallbackDurationMs: Long, fallbackAmplitude: Int) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (!shouldVibrate()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effectToUse = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val status = v.areEffectsSupported(effectId)
                    if (status.isNotEmpty() && status[0] == Vibrator.VIBRATION_EFFECT_SUPPORT_YES) {
                        effectId
                    } else {
                        VibrationEffect.EFFECT_TICK
                    }
                } else {
                    effectId
                }
                v.vibrate(VibrationEffect.createPredefined(effectToUse))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedDuration = fallbackDurationMs.coerceIn(40L, 60L)
                val clampedAmplitude = fallbackAmplitude.coerceIn(80, 100)
                v.vibrate(VibrationEffect.createOneShot(clampedDuration, clampedAmplitude))
            } else {
                @Suppress("DEPRECATION")
                val clampedDuration = fallbackDurationMs.coerceIn(40L, 60L)
                v.vibrate(clampedDuration)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectionClick() {
        vibratePredefined(VibrationEffect.EFFECT_TICK, 40L, 80)
    }

    fun confirm() {
        vibratePredefined(VibrationEffect.EFFECT_CLICK, 50L, 90)
    }

    fun strongClick() {
        vibratePredefined(VibrationEffect.EFFECT_HEAVY_CLICK, 60L, 100)
    }

    fun doubleClick() {
        vibratePredefined(VibrationEffect.EFFECT_DOUBLE_CLICK, 60L, 100)
    }

    fun error() {
        if (!shouldVibrate()) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 80), -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 80, 60, 80), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun celebrate() {
        if (!shouldVibrate()) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 40, 80), -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 80, 40, 80), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun celebrateMilestone() {
        if (!shouldVibrate()) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100, 50, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 100, 50, 100, 50, 200), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun tickHaptic() {
        if (!shouldVibrate()) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(10)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Deprecated legacy aliases to prevent compile breaks during transition
    fun lightImpact() = selectionClick()
    fun mediumImpact() = confirm()
    fun heavyImpact() = strongClick()
}
