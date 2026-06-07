package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.core.utils.DateUtils
import com.example.core.utils.Responsive
import com.example.data.models.DayRecord
import com.example.data.models.TaskModel
import com.example.providers.Providers
import com.example.shared.widgets.SectionHeader
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HeatmapScreen(
    modifier: Modifier = Modifier
) {
    val settingsProvider = Providers.getSettings()
    val streakProvider = Providers.getStreak()
    val taskProvider = Providers.getTask()

    val settingsState by settingsProvider.settingsState.collectAsState()
    val dayRecords by streakProvider.dayRecordsState.collectAsState()
    val tasksState by taskProvider.tasksState.collectAsState()
    val currentDateKey by taskProvider.currentDateKey.collectAsState()

    val language = settingsState.language
    val accentColor = AppColors.accentColorOptions[settingsState.accentColorIndex]

    var selectedCalendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateKey by remember { mutableStateOf(DateUtils.getTodayKey()) }

    // Read matching tasks for the selected date key to assist offline review the user selected
    var historicalTasks by remember { mutableStateOf<List<TaskModel>>(emptyList()) }
    
    // Fetch historical tasks whenever selected date changes
    LaunchedEffect(selectedDateKey) {
        historicalTasks = taskProvider.getTasksForDateList(selectedDateKey)
    }

    val getLabel = { en: String, hi: String, mr: String ->
        when (language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }

    // Helper to calculate previous/next months
    val adjustMonth = { offset: Int ->
        val updated = Calendar.getInstance().apply {
            time = selectedCalendarMonth.time
            set(Calendar.DAY_OF_MONTH, 1) // Crucial: avoid rollovers on months with varying lengths (e.g., 31st)
            add(Calendar.MONTH, offset)
        }
        selectedCalendarMonth = updated
    }

    // Generate days of month grid
    val daysInMonth = remember(selectedCalendarMonth) {
        val list = mutableListOf<CalendarDateInfo>()
        val tempCal = Calendar.getInstance().apply {
            time = selectedCalendarMonth.time
            set(Calendar.DAY_OF_MONTH, 1)
        }
        
        val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...

        // Add blank dates for grid alignment
        val blankDays = firstDayOfWeek - 1
        for (i in 0 until blankDays) {
            list.add(CalendarDateInfo(null, false))
        }

        // Add actual dates
        for (day in 1..maxDays) {
            val dateCal = Calendar.getInstance().apply {
                time = tempCal.time
                set(Calendar.DAY_OF_MONTH, day)
            }
            list.add(CalendarDateInfo(dateCal.time, true))
        }

        // Pad the remainder of the week so the grid aligns correctly
        while (list.size % 7 != 0) {
            list.add(CalendarDateInfo(null, false))
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary)
    ) {
        // Month navigation banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getLabel("Consistency Grid", "निरंतरता ग्रिड", "सातत्य ग्रिड"),
                style = AppTextStyles.titleLarge
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    com.example.core.utils.SoundService.playTap()
                    com.example.core.utils.HapticService.selectionClick()
                    adjustMonth(-1)
                }) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = AppColors.textPrimary)
                }

                val fmt = SimpleDateFormat("MMMM yyyy", Locale(language))
                Text(
                    text = fmt.format(selectedCalendarMonth.time).uppercase(),
                    style = AppTextStyles.bodyMedium.copy(fontWeight = FontWeight.Bold, color = accentColor),
                    modifier = Modifier.widthIn(min = 100.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(onClick = {
                    com.example.core.utils.SoundService.playTap()
                    com.example.core.utils.HapticService.selectionClick()
                    adjustMonth(1)
                }) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", tint = AppColors.textPrimary)
                }
            }
        }

        // Days of week header labels to align EXACTLY with the columns inside the card layout below (w(5f) outer + 12dp card interior padding)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val headers = when (language) {
                "hi" -> listOf("रवि", "सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि")
                "mr" -> listOf("रवी", "सोम", "मंळ", "बुध", "गुरू", "शुक्र", "शनी")
                else -> listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            }
            headers.forEach { label ->
                Text(
                    text = label,
                    style = AppTextStyles.caption.copy(
                        color = AppColors.textSecondary,
                        fontWeight = FontWeight.Black,
                        fontSize = Responsive.fp(11f)
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Calendar Grid Card
        com.example.shared.widgets.StreaklyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            padding = 12.dp,
            borderRadius = 20.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val chunkSize = 7
                val weeks = daysInMonth.chunked(chunkSize)

                weeks.forEach { weekList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekList.forEach { cell ->
                            if (cell.isValid && cell.date != null) {
                                val key = DateUtils.getDateKey(cell.date)
                                val isSelected = key == selectedDateKey
                                
                                // Lookup corresponding completion rate
                                val record = dayRecords.find { it.dateKey == key }
                                val isToday = key == DateUtils.getTodayKey()
                                
                                // Determine intensity color
                                val blockColor = when {
                                    record == null -> AppColors.bgPrimary // No entry
                                    record.completionPct >= 80.0 -> accentColor
                                    record.completionPct >= 50.0 -> accentColor.copy(alpha = 0.55f)
                                    record.completionPct > 0.0 -> accentColor.copy(alpha = 0.25f)
                                    else -> AppColors.bgTertiary // 0%
                                }

                                val cal = Calendar.getInstance().apply { time = cell.date }
                                val textCol = if (record != null && record.completionPct >= 80.0) Color.Black else AppColors.textPrimary

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(blockColor)
                                        .border(
                                            width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.dp,
                                            color = if (isSelected) Color.White else if (isToday) accentColor else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            com.example.core.utils.SoundService.playTap()
                                            com.example.core.utils.HapticService.selectionClick()
                                            selectedDateKey = key
                                            // Make date active in master tasks view as well
                                            taskProvider.setDateKey(key)
                                        }
                                        .testTag("heatmap_cell_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cal.get(Calendar.DAY_OF_MONTH).toString(),
                                        style = AppTextStyles.bodySmall.copy(
                                            fontFamily = null,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                            fontSize = Responsive.fp(12f),
                                            color = textCol
                                        )
                                    )
                                }
                            } else {
                                // Empty blank space for alignment padding
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Details Panel for picked date
        SectionHeader(
            title = DateUtils.getFormattedDate(selectedDateKey, language),
            subtitle = getLabel("Habits Review", "आदतों की समीक्षा", "सवय आढावा")
        )

        // Pull active list
        val displayTasks = if (selectedDateKey == currentDateKey) tasksState else historicalTasks

        if (displayTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getLabel("No habits tracked for this date.", "इस तिथि के लिए कोई आदत नहीं है।", "या दिनांकासाठी कोणतीही नोंद आढळली नाही."),
                    style = AppTextStyles.bodyMedium,
                    color = AppColors.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = Responsive.h(12f)
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayTasks) { task ->
                    val isDone = task.isCompleted
                    com.example.shared.widgets.StreaklyCard(
                        modifier = Modifier.fillMaxWidth(),
                        padding = 14.dp,
                        borderRadius = 20.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = AppTextStyles.cardTitle(
                                        if (isDone) AppColors.textSecondary else AppColors.textPrimary
                                    ).copy(
                                        textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (!task.description.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = task.description,
                                        style = AppTextStyles.hint(AppColors.textSecondary)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.AccessTime,
                                contentDescription = if (isDone) "Completed" else "Pending",
                                tint = if (isDone) accentColor else AppColors.textHint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class CalendarDateInfo(
    val date: Date?,
    val isValid: Boolean
)
