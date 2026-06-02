package com.example.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.StreaklyApp
import com.example.data.models.DayRecord
import com.example.data.models.StreakModel
import com.example.data.repositories.StreakRepository
import com.example.core.constants.AppConstants
import com.example.core.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class StreakProvider(
    private val streakRepository: StreakRepository = StreaklyApp.instance.streakRepository
) : ViewModel() {

    private val _streakState = MutableStateFlow(StreakModel.default())
    val streakState: StateFlow<StreakModel> = _streakState.asStateFlow()

    private val _dayRecordsState = MutableStateFlow<List<DayRecord>>(emptyList())
    val dayRecordsState: StateFlow<List<DayRecord>> = _dayRecordsState.asStateFlow()

    // Holds a milestone that was newly crossed but not yet claimed. Triggering the popup in UI.
    private val _newlyCrossedMilestone = MutableStateFlow<Int?>(null)
    val newlyCrossedMilestone: StateFlow<Int?> = _newlyCrossedMilestone.asStateFlow()

    init {
        // Load initially
        viewModelScope.launch {
            streakRepository.getStreakFlow().collectLatest { streak ->
                if (streak != null) {
                    _streakState.value = streak
                } else {
                    val defaultStreak = StreakModel.default()
                    streakRepository.saveStreak(defaultStreak)
                    _streakState.value = defaultStreak
                }
            }
        }

        viewModelScope.launch {
            streakRepository.getAllDayRecordsFlow().collectLatest { records ->
                _dayRecordsState.value = records
            }
        }
    }

    /**
     * Recalculates the user's streaks based on Day Records from the past up to yesterday,
     * and today's dynamic record.
     */
    fun recalculateStreakFromRecords(todayCompleted: Int, todayTotal: Int) {
        viewModelScope.launch {
            val records = streakRepository.getAllDayRecordsList()
            val todayKey = DateUtils.getTodayKey()
            val yesterdayKey = DateUtils.getYesterdayKey()

            // Update today's record in Database
            val todayPct = if (todayTotal > 0) {
                (todayCompleted.toDouble() / todayTotal * 100)
            } else {
                100.0 // Default 100% if no tasks
            }
            val todayRecord = DayRecord(
                dateKey = todayKey,
                tasksCompleted = todayCompleted,
                tasksTotal = todayTotal,
                completionPct = todayPct
            )
            streakRepository.saveDayRecord(todayRecord)

            // Re-fetch all sorted day records chronologically (ascending dateKey)
            val updatedRecords = streakRepository.getAllDayRecordsList().sortedBy { it.dateKey }
            if (updatedRecords.isEmpty()) return@launch

            val firstRecordKey = updatedRecords.first().dateKey
            val firstDate = DateUtils.parseDateKey(firstRecordKey) ?: Date()
            val calendar = Calendar.getInstance().apply { time = firstDate }
            val todayDate = Date()

            var computedCurrentStreak = 0
            var computedLongestStreak = 0
            var computedTotalStreakDays = 0

            // Chronological traversal from first recording day to today
            while (!calendar.time.after(todayDate)) {
                val currentKey = DateUtils.getDateKey(calendar.time)
                val record = updatedRecords.find { it.dateKey == currentKey }

                if (currentKey == todayKey) {
                    // It's today! Today is dynamic. Let's see if we hit threshold.
                    val pct = todayRecord.completionPct
                    if (pct >= 80.0) {
                        computedCurrentStreak++
                        computedTotalStreakDays++
                    } else if (pct < 50.0) {
                        // Today is NOT yet a permanent reset, as user still has until midnight.
                        // So we do not force reset currentStreak to 0 if we already had a streak up to yesterday!
                        // This allows user to save their streak during the active day.
                    } else {
                        // Grace zone (50-79%): Keeps streak exactly as yesterday's
                    }
                } else {
                    // It's a past day!
                    if (record != null) {
                        val pct = record.completionPct
                        if (pct >= 80.0) {
                            computedCurrentStreak++
                            computedTotalStreakDays++
                        } else if (pct < 50.0) {
                            computedCurrentStreak = 0
                        } else {
                            // Grace zone (50-79%): Keeps current streak unchanged, but doesn't increment
                        }
                    } else {
                        // Missed past day altogether (no tasks created or tracked) is a failure
                        computedCurrentStreak = 0
                    }
                }

                if (computedCurrentStreak > computedLongestStreak) {
                    computedLongestStreak = computedCurrentStreak
                }

                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            // Retrieve old streak values to see if a NEW milestone is unlocked
            val currentStreakModel = streakRepository.getStreak() ?: StreakModel.default()
            val claimedMilestones = currentStreakModel.getMilestonesClaimedList().toMutableSet()

            // Find reached milestones
            val newlyUnlocked = AppConstants.MILESTONES.firstOrNull { milestone ->
                computedCurrentStreak >= milestone && !claimedMilestones.contains(milestone.toString()) && _newlyCrossedMilestone.value != milestone
            }

            // Save new streak model
            val updatedStreak = currentStreakModel.copy(
                currentStreak = computedCurrentStreak,
                longestStreak = maxOf(currentStreakModel.longestStreak, computedLongestStreak),
                totalStreakDays = computedTotalStreakDays,
                lastActiveDate = if (todayRecord.completionPct >= 80.0) todayKey else currentStreakModel.lastActiveDate
            )
            streakRepository.saveStreak(updatedStreak)
            _streakState.value = updatedStreak

            // Trigger show milestone popup if found
            if (newlyUnlocked != null) {
                _newlyCrossedMilestone.value = newlyUnlocked
            }
        }
    }

    /**
     * Set newly crossed milestone back to null after displaying / claiming.
     */
    fun claimMilestone(milestone: Int) {
        viewModelScope.launch {
            val current = streakRepository.getStreak() ?: StreakModel.default()
            val claimed = current.getMilestonesClaimedList().toMutableSet()
            claimed.add(milestone.toString())
            
            val updated = current.copy(
                milestonesClaimedStr = claimed.joinToString(",")
            )
            streakRepository.saveStreak(updated)
            _streakState.value = updated
            _newlyCrossedMilestone.value = null
        }
    }

    fun dismissMilestone() {
        _newlyCrossedMilestone.value = null
    }

    fun resetAllStreakData() {
        viewModelScope.launch {
            streakRepository.resetAllStreakData()
            _streakState.value = StreakModel.default()
            _dayRecordsState.value = emptyList()
            _newlyCrossedMilestone.value = null
        }
    }
}
