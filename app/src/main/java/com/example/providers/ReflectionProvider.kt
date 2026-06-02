package com.example.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.StreaklyApp
import com.example.data.models.ReflectionModel
import com.example.data.repositories.ReflectionRepository
import com.example.core.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class ReflectionProvider(
    private val reflectionRepository: ReflectionRepository = StreaklyApp.instance.reflectionRepository
) : ViewModel() {

    private val _reflectionsState = MutableStateFlow<List<ReflectionModel>>(emptyList())
    val reflectionsState: StateFlow<List<ReflectionModel>> = _reflectionsState.asStateFlow()

    private val _todayReflectionState = MutableStateFlow<ReflectionModel?>(null)
    val todayReflectionState: StateFlow<ReflectionModel?> = _todayReflectionState.asStateFlow()

    init {
        // Collect all historically entered reflections
        viewModelScope.launch {
            reflectionRepository.getAllReflectionsFlow().collectLatest { list ->
                _reflectionsState.value = list
            }
        }

        // Keep track of today's reflection
        viewModelScope.launch {
            val todayKey = DateUtils.getTodayKey()
            reflectionRepository.getReflectionForDateFlow(todayKey).collectLatest { reflection ->
                _todayReflectionState.value = reflection
            }
        }
    }

    fun saveReflection(moodIndex: Int, moodEmoji: String, text: String) {
        viewModelScope.launch {
            val todayKey = DateUtils.getTodayKey()
            val existing = reflectionRepository.getReflectionForDate(todayKey)
            
            val reflection = ReflectionModel(
                id = existing?.id ?: UUID.randomUUID().toString(),
                dateKey = todayKey,
                moodEmoji = moodEmoji,
                moodIndex = moodIndex,
                text = text,
                createdAt = System.currentTimeMillis()
            )
            reflectionRepository.saveReflection(reflection)
        }
    }

    fun deleteReflection(id: String) {
        viewModelScope.launch {
            reflectionRepository.deleteReflectionById(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            reflectionRepository.deleteAllReflections()
            _reflectionsState.value = emptyList()
            _todayReflectionState.value = null
        }
    }
}
