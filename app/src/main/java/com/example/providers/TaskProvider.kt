package com.example.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.StreaklyApp
import com.example.data.models.TaskModel
import com.example.data.repositories.TaskRepository
import com.example.core.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class TaskProvider(
    private val taskRepository: TaskRepository = StreaklyApp.instance.taskRepository,
    private val streakProvider: StreakProvider
) : ViewModel() {

    private val _currentDateKey = MutableStateFlow(DateUtils.getTodayKey())
    val currentDateKey: StateFlow<String> = _currentDateKey.asStateFlow()

    private val _tasksState = MutableStateFlow<List<TaskModel>>(emptyList())
    val tasksState: StateFlow<List<TaskModel>> = _tasksState.asStateFlow()

    init {
        // Collect all tasks and filter/sort them dynamically based on active date key
        viewModelScope.launch {
            _currentDateKey.collectLatest { dateKey ->
                taskRepository.getAllTasks().collectLatest { allTasks ->
                    val sortedFiltered = filterAndSortTasksForDate(allTasks, dateKey)
                    _tasksState.value = sortedFiltered
                    // Run a dynamic streak update if tasks reflect today
                    if (dateKey == DateUtils.getTodayKey()) {
                        triggerStreakRecalculation(sortedFiltered)
                    }
                }
            }
        }
    }

    /**
     * Change the active date shown in Home screen or Heatmap details
     */
    fun setDateKey(dateKey: String) {
        _currentDateKey.value = dateKey
    }

    suspend fun getTasksForDateList(dateKey: String): List<TaskModel> {
        val allTasks = taskRepository.getAllTasksList()
        return filterAndSortTasksForDate(allTasks, dateKey)
    }

    /**
     * Trigger recalculate in StreakProvider with the matching completed and total count
     */
    private fun triggerStreakRecalculation(tasks: List<TaskModel>) {
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        streakProvider.recalculateStreakFromRecords(completed, total)
    }

    /**
     * Calculate and sync streak again manually (e.g. on app startup)
     */
    fun syncTodayStreak() {
        viewModelScope.launch {
            val todayTasks = getTasksForDateList(DateUtils.getTodayKey())
            val total = todayTasks.size
            val completed = todayTasks.count { it.isCompleted }
            streakProvider.recalculateStreakFromRecords(completed, total)
        }
    }

    fun addTask(
        title: String,
        description: String?,
        timeLabel: String?,
        colorIndex: Int,
        frequency: String = "daily",
        weekDaysRaw: String = "",
        importance: String = "regular"
    ) {
        viewModelScope.launch {
            val dateKey = _currentDateKey.value
            val newTask = TaskModel(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                timeLabel = timeLabel,
                colorIndex = colorIndex,
                isCompleted = false,
                dateKey = dateKey,
                createdAt = System.currentTimeMillis(),
                frequency = frequency,
                weekDaysRaw = weekDaysRaw,
                importance = importance
            )
            taskRepository.insertTask(newTask)
        }
    }

    fun toggleTaskCompletion(task: TaskModel) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            taskRepository.insertTask(updatedTask)
        }
    }

    fun updateTask(task: TaskModel) {
        viewModelScope.launch {
            taskRepository.insertTask(task)
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            taskRepository.deleteAllTasks()
            _tasksState.value = emptyList()
            if (_currentDateKey.value == DateUtils.getTodayKey()) {
                streakProvider.recalculateStreakFromRecords(0, 0)
            }
        }
    }

    companion object {
        fun filterAndSortTasksForDate(allTasks: List<TaskModel>, dateKey: String): List<TaskModel> {
            val dayOfWeek = DateUtils.getDayOfWeek(dateKey)
            
            // Calculate end of day timestamp
            val getEndOfDayTimestamp = { dk: String ->
                val date = DateUtils.parseDateKey(dk) ?: java.util.Date()
                val cal = java.util.Calendar.getInstance().apply {
                    time = date
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                    set(java.util.Calendar.SECOND, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }
                cal.timeInMillis
            }
            val endOfDayTime = getEndOfDayTimestamp(dateKey)

            val filtered = allTasks.filter { task ->
                if (task.dateKey == dateKey) {
                    true
                } else {
                    when (task.frequency) {
                        "once" -> false
                        "weekly" -> {
                            val inSelectedDays = task.weekDays.contains(dayOfWeek)
                            inSelectedDays && task.createdAt <= endOfDayTime
                        }
                        "daily" -> {
                            task.createdAt <= endOfDayTime
                        }
                        else -> true
                    }
                }
            }

            return filtered.sortedWith(
                compareBy<TaskModel> {
                    com.example.core.theme.AppColors.importanceOrder[it.importance] ?: 2
                }.thenBy {
                    it.isCompleted
                }.thenBy {
                    it.timeLabel ?: ""
                }
            )
        }
    }
}
