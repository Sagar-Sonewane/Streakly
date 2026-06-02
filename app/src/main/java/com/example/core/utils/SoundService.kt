package com.example.core.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.providers.SettingsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundService {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playTap() {
        if (!SettingsProvider.soundEnabled) return
        scope.launch {
            synthesizeTone(
                startFreq = 600.0,
                endFreq = 250.0,
                durationMs = 50,
                isSine = true
            )
        }
    }

    fun playTaskDone() {
        if (!SettingsProvider.soundEnabled) return
        scope.launch {
            // Dual chime at C5 (523) + G5 (784)
            synthesizeChime(523.25, 783.99, 250)
        }
    }

    fun playMilestone() {
        if (!SettingsProvider.soundEnabled) return
        scope.launch {
            // Ascending major scale fanfare (C5 -> E5 -> G5 -> C6)
            val notes = listOf(523.25, 659.25, 783.99, 1046.50)
            for (freq in notes) {
                synthesizeTone(freq, freq, 120, isSine = true)
                kotlinx.coroutines.delay(100)
            }
        }
    }

    fun playDismiss() {
        if (!SettingsProvider.soundEnabled) return
        scope.launch {
            synthesizeTone(
                startFreq = 800.0,
                endFreq = 100.0,
                durationMs = 250,
                isSine = true
            )
        }
    }

    private fun synthesizeTone(startFreq: Double, endFreq: Double, durationMs: Int, isSine: Boolean = true) {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            if (numSamples <= 0) return
            val samples = FloatArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val time = i.toDouble() / sampleRate
                val angle = 2.0 * Math.PI * currentFreq * time
                val wave = sin(angle)

                // Volume envelope (exponential decay)
                val envelope = if (progress < 0.1) progress / 0.1 else 1.0 - progress
                samples[i] = (wave * envelope * 0.4).toFloat()
            }

            playFloatSamples(samples, sampleRate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun synthesizeChime(f1: Double, f2: Double, durationMs: Int) {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            if (numSamples <= 0) return
            val samples = FloatArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val time = i.toDouble() / sampleRate
                
                // Combine harmonics
                val wave1 = sin(2.0 * Math.PI * f1 * time)
                val wave2 = sin(2.0 * Math.PI * f2 * time)
                val combined = (wave1 * 0.6) + (wave2 * 0.4)

                // Quick Attack, slow exponential decay
                val envelope = Math.exp(-4.0 * progress)
                samples[i] = (combined * envelope * 0.4).toFloat()
            }

            playFloatSamples(samples, sampleRate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playFloatSamples(samples: FloatArray, sampleRate: Int) {
        try {
            val numSamples = samples.size
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                buffer[i] = (samples[i] * Short.MAX_VALUE).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            
            scope.launch {
                kotlinx.coroutines.delay(samples.size * 1000L / sampleRate + 120)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
