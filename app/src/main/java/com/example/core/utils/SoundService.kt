package com.example.core.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import com.example.R
import com.example.providers.SettingsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundService {
    private val scope = CoroutineScope(Dispatchers.Default)
    
    private var soundPool: SoundPool? = null
    private val loadedSounds = mutableSetOf<Int>()
    
    private var successSoundId = 0
    private var deleteSoundId = 0
    private var addSoundId = 0
    private var notificationSoundId = 0

    init {
        initSoundPool()
    }

    @Synchronized
    private fun initSoundPool() {
        if (soundPool != null) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()

            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    synchronized(loadedSounds) {
                        loadedSounds.add(sampleId)
                    }
                    Log.d("SoundService", "Sound loaded successfully: $sampleId")
                } else {
                    Log.e("SoundService", "Failed to load sound: $sampleId, status: $status")
                }
            }

            val context = try {
                com.example.StreaklyApp.instance
            } catch (e: Exception) {
                Log.w("SoundService", "StreaklyApp instance not initialized: ${e.message}")
                return
            }
            successSoundId = pool.load(context, R.raw.task_complete, 1)
            deleteSoundId = pool.load(context, R.raw.task_remove, 1)
            addSoundId = pool.load(context, R.raw.task_add, 1)
            notificationSoundId = pool.load(context, R.raw.notification, 1)

            soundPool = pool
            Log.d("SoundService", "SoundPool initialized and sounds loading started")
        } catch (e: Exception) {
            Log.e("SoundService", "Error initializing SoundPool: ${e.message}", e)
        }
    }

    private fun getAudioManager(): AudioManager? {
        return try {
            val appInstance = try {
                com.example.StreaklyApp.instance
            } catch (e: Exception) {
                return null
            }
            appInstance.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        } catch (e: Exception) {
            null
        }
    }

    private fun shouldPlaySound(): Boolean {
        if (!SettingsProvider.soundEnabled) return false
        val audioManager = getAudioManager() ?: return false
        val ringerMode = audioManager.ringerMode
        return ringerMode != AudioManager.RINGER_MODE_SILENT && ringerMode != AudioManager.RINGER_MODE_VIBRATE
    }

    private fun playSound(soundId: Int) {
        if (!shouldPlaySound()) return
        if (soundPool == null) {
            initSoundPool()
        }
        val pool = soundPool ?: return
        val isLoaded = synchronized(loadedSounds) {
            loadedSounds.contains(soundId)
        }
        if (soundId != 0 && isLoaded) {
            try {
                pool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            } catch (e: Exception) {
                Log.e("SoundService", "Error playing sound $soundId: ${e.message}", e)
            }
        } else {
            Log.w("SoundService", "Sound $soundId not loaded yet or invalid")
        }
    }

    fun playTap() {
        if (!shouldPlaySound()) return
        try {
            getAudioManager()?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        } catch (e: Exception) {
            Log.e("SoundService", "Error playing playTap: ${e.message}", e)
        }
    }

    fun playSuccess() {
        playSound(successSoundId)
    }

    fun playDelete() {
        playSound(deleteSoundId)
    }

    fun playAdd() {
        playSound(addSoundId)
    }

    fun playToggle() {
        if (!shouldPlaySound()) return
        try {
            getAudioManager()?.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
        } catch (e: Exception) {
            Log.e("SoundService", "Error playing playToggle: ${e.message}", e)
        }
    }

    fun playError() {
        if (!shouldPlaySound()) return
        scope.launch {
            try {
                delay(100) // slight delay
                getAudioManager()?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.4f)
            } catch (e: Exception) {
                Log.e("SoundService", "Error playing playError: ${e.message}", e)
            }
        }
    }

    @Synchronized
    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            synchronized(loadedSounds) {
                loadedSounds.clear()
            }
            successSoundId = 0
            deleteSoundId = 0
            addSoundId = 0
            notificationSoundId = 0
            Log.d("SoundService", "SoundPool released successfully")
        } catch (e: Exception) {
            Log.e("SoundService", "Error releasing SoundPool: ${e.message}", e)
        }
    }

    // Deprecated legacy aliases to prevent compile breaks during multi-file transition
    fun playTaskDone() = playSuccess()
    fun playMilestone() = playSuccess()
    fun playDismiss() = playDelete()
}
