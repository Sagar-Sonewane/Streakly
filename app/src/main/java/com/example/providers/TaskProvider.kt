package com.example.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.StreaklyApp
import com.example.data.models.TaskModel
import com.example.data.repositories.TaskRepository
import com.example.data.repositories.DailyCompletionRepository
import com.example.core.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class TaskProvider(
    private val taskRepository: TaskRepository = StreaklyApp.instance.taskRepository,
    private val dailyCompletionRepository: DailyCompletionRepository = StreaklyApp.instance.dailyCompletionRepository,
    private val streakProvider: StreakProvider
) : ViewModel() {

    private val _currentDateKey = MutableStateFlow(DateUtils.getTodayKey())
    val currentDateKey: StateFlow<String> = _currentDateKey.asStateFlow()

    private val _tasksState = MutableStateFlow<List<TaskModel>>(emptyList())
    val tasksState: StateFlow<List<TaskModel>> = _tasksState.asStateFlow()

    init {
        viewModelScope.launch {
            _currentDateKey.collectLatest { dateKey ->
                val tasksFlow = taskRepository.getAllTasks()
                val completionsFlow = dailyCompletionRepository.getCompletionsForDateFlow(dateKey)
                
                kotlinx.coroutines.flow.combine(tasksFlow, completionsFlow) { allTasks, completions ->
                    val completedTaskIds = completions.filter { it.isCompleted }.map { it.taskId }.toSet()
                    val mappedTasks = allTasks.map { task ->
                        if (task.frequency == "once") {
                            task
                        } else {
                            task.copy(isCompleted = completedTaskIds.contains(task.id))
                        }
                    }
                    filterAndSortTasksForDate(mappedTasks, dateKey)
                }.collectLatest { sortedFiltered ->
                    _tasksState.value = sortedFiltered
                    
                    // Update DayRecord for the active dateKey
                    val total = sortedFiltered.size
                    val completed = sortedFiltered.count { it.isCompleted }
                    val pct = if (total > 0) {
                        (completed.toDouble() / total * 100)
                    } else {
                        0.0
                    }
                    val record = com.example.data.models.DayRecord(
                        dateKey = dateKey,
                        tasksCompleted = completed,
                        tasksTotal = total,
                        completionPct = pct
                    )
                    StreaklyApp.instance.streakRepository.saveDayRecord(record)
                    
                    // Trigger dynamic streak update only when selected date is today
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
        val completions = dailyCompletionRepository.getCompletionsForDateList(dateKey)
        val completedTaskIds = completions.filter { it.isCompleted }.map { it.taskId }.toSet()
        val mappedTasks = allTasks.map { task ->
            if (task.frequency == "once") {
                task
            } else {
                task.copy(isCompleted = completedTaskIds.contains(task.id))
            }
        }
        return filterAndSortTasksForDate(mappedTasks, dateKey)
    }

    suspend fun isTaskCompletedToday(taskId: String): Boolean {
        return dailyCompletionRepository.isTaskCompleted(taskId, DateUtils.getTodayKey())
    }

    suspend fun getCompletionPercentageForDate(dateKey: String): Double {
        val tasks = getTasksForDateList(dateKey)
        if (tasks.isEmpty()) return 0.0
        val completed = tasks.count { it.isCompleted }
        return (completed.toDouble() / tasks.size) * 100
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
            if (task.frequency == "once") {
                val updatedTask = task.copy(isCompleted = !task.isCompleted)
                taskRepository.insertTask(updatedTask)
            } else {
                dailyCompletionRepository.setTaskCompleted(task.id, _currentDateKey.value, !task.isCompleted)
            }
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
            dailyCompletionRepository.deleteCompletionsForTask(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            taskRepository.deleteAllTasks()
            dailyCompletionRepository.deleteAll()
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
