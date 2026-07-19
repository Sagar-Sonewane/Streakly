package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.core.theme.LocalAccentColor
import com.example.core.utils.DateUtils
import com.example.core.utils.CategoryUtils
import com.example.data.models.DailyCompletion
import com.example.data.models.TaskModel
import com.example.providers.Providers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsBottomSheet(
    task: TaskModel,
    onDismiss: () -> Unit,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onArchive: () -> Unit
) {
    val accentColor = LocalAccentColor.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val settingsProvider = Providers.getSettings()
    val settingsState by settingsProvider.settingsState.collectAsState()
    val language = settingsState.language
    val taskProvider = Providers.getTask()

    var completions by remember { mutableStateOf<List<DailyCompletion>>(emptyList()) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var longestStreak by remember { mutableIntStateOf(0) }

    val getLabel = { en: String, hi: String, mr: String ->
        when (language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }

    // Load completion history and calculate streaks
    LaunchedEffect(task.id, task.isCompleted) {
        val list = taskProvider.getCompletionsForTaskList(task.id)
        completions = list
        val streakPair = calculateTaskStreak(list, task)
        currentStreak = streakPair.first
        longestStreak = streakPair.second
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = AppColors.bgSecondary,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header (Emoji & Title & Category)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryUtils.getIconForEmoji(task.emoji),
                            contentDescription = "Habit Icon",
                            tint = accentColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = AppTextStyles.headingMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            ),
                            color = AppColors.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = CategoryUtils.getCategoryLabel(task.emoji, language),
                            style = AppTextStyles.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        )
                    }
                }

                // Description/Notes Card
                if (!task.description.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColors.bgTertiary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = getLabel("Notes", "टिप्पणियां", "नोंद"),
                                style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                                color = AppColors.textHint
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.description,
                                style = AppTextStyles.bodyMedium,
                                color = AppColors.textPrimary
                            )
                        }
                    }
                }

                // Stats Section (Streak cards)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Current Streak
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColors.bgTertiary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Current Streak",
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$currentStreak " + getLabel("Days", "दिन", "दिवस"),
                                style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 16.sp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = getLabel("Current Streak", "सक्रिय निरंतरता", "सक्रिय सातत्य"),
                                style = AppTextStyles.caption.copy(fontSize = 11.sp),
                                color = AppColors.textHint,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Longest Streak
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColors.bgTertiary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = "Longest Streak",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$longestStreak " + getLabel("Days", "दिन", "दिवस"),
                                style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 16.sp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = getLabel("Longest Streak", "सर्वोच्च निरंतरता", "सर्वोच्च सातत्य"),
                                style = AppTextStyles.caption.copy(fontSize = 11.sp),
                                color = AppColors.textHint,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Total Completions
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColors.bgTertiary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Total Completions",
                                tint = AppColors.success,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${completions.count { it.isCompleted }}",
                                style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 16.sp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = getLabel("Total Done", "कुल पूर्ण", "एकूण पूर्ण"),
                                style = AppTextStyles.caption.copy(fontSize = 11.sp),
                                color = AppColors.textHint,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Progress Insights Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.bgTertiary),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val weekKeys = remember { getCurrentWeekKeys() }
                        val isScheduled: (String) -> Boolean = { key ->
                            when (task.frequency) {
                                "daily" -> true
                                "weekly" -> {
                                    val dayOfWeek = DateUtils.getDayOfWeek(key)
                                    task.weekDays.contains(dayOfWeek)
                                }
                                else -> false
                            }
                        }
                        
                        val scheduledDaysThisWeek = weekKeys.filter { isScheduled(it) }
                        val completedDaysThisWeek = scheduledDaysThisWeek.filter { key ->
                            completions.find { it.dateKey == key }?.isCompleted == true
                        }
                        
                        val progressPercentage = if (scheduledDaysThisWeek.isNotEmpty()) {
                            (completedDaysThisWeek.size.toDouble() / scheduledDaysThisWeek.size * 100).toInt()
                        } else {
                            0
                        }

                        // Title & Percentage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getLabel("WEEKLY PROGRESS", "साप्ताहिक प्रगति", "साप्ताहिक प्रगती"),
                                style = AppTextStyles.caption.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = AppColors.textHint
                            )
                            Text(
                                text = "$progressPercentage%",
                                style = AppTextStyles.label(accentColor).copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            )
                        }

                        // Linear progress indicator
                        LinearProgressIndicator(
                            progress = progressPercentage / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = accentColor,
                            trackColor = AppColors.bgSecondary
                        )

                        Text(
                            text = getLabel(
                                "Completed ${completedDaysThisWeek.size} of ${scheduledDaysThisWeek.size} scheduled days",
                                "निर्धारित ${scheduledDaysThisWeek.size} में से ${completedDaysThisWeek.size} दिन पूरे हुए",
                                "नियोजित ${scheduledDaysThisWeek.size} पैकी ${completedDaysThisWeek.size} दिवस पूर्ण झाले"
                            ),
                            style = AppTextStyles.bodySmall,
                            color = AppColors.textSecondary
                        )

                        Divider(color = AppColors.border.copy(alpha = 0.5f), thickness = 1.dp)

                        // 7-day visual trend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekKeys.forEach { key ->
                                val dayLabel = DateUtils.getDayOfWeekLabel(key, language)
                                val scheduled = isScheduled(key)
                                val done = completions.find { it.dateKey == key }?.isCompleted == true

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = dayLabel,
                                        style = AppTextStyles.caption.copy(fontSize = 11.sp),
                                        color = AppColors.textHint
                                    )

                                    if (scheduled) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (done) accentColor else Color.Transparent)
                                                .border(
                                                    width = 2.dp,
                                                    color = if (done) accentColor else AppColors.textHint.copy(alpha = 0.5f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (done) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Done",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp, 2.dp)
                                                    .background(AppColors.textHint.copy(alpha = 0.3f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Info Panel Details (Grid or List items)
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.bgTertiary),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = getLabel("HABIT PARAMETERS", "आदत के विवरण", "सवयीचे तपशील"),
                            style = AppTextStyles.caption.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = AppColors.textHint
                        )

                        // Priority
                        InfoRow(
                            icon = Icons.Default.PriorityHigh,
                            label = getLabel("Priority", "प्राथमिकता", "प्राधान्य"),
                            value = when (task.importance) {
                                "regular" -> getLabel("Easy", "आसान", "सोपे")
                                "moderate" -> getLabel("Mid", "मध्यम", "मध्यम")
                                else -> getLabel("High", "उच्च", "उच्च")
                            },
                            valueColor = when (task.importance) {
                                "regular" -> Color(0xFF4CAF50)
                                "moderate" -> Color(0xFFFFC107)
                                else -> Color(0xFFFF5722)
                            }
                        )

                        // Difficulty
                        InfoRow(
                            icon = Icons.Default.Star,
                            label = getLabel("Difficulty", "कठिनाई", "काठिण्य"),
                            value = task.difficulty
                        )

                        // Reminder
                        val reminderText = if (task.reminderEnabled && task.reminderHour != null && task.reminderMinute != null) {
                            val h = task.reminderHour.toString().padStart(2, '0')
                            val m = task.reminderMinute.toString().padStart(2, '0')
                            "$h:$m"
                        } else {
                            getLabel("Disabled", "अक्षम", "बंद")
                        }
                        InfoRow(
                            icon = Icons.Rounded.Schedule,
                            label = getLabel("Reminder Time", "रिमाइंडर समय", "स्मरण वेळ"),
                            value = reminderText
                        )

                        // Schedule
                        val scheduleLabel = when (task.frequency) {
                            "once" -> getLabel("Once", "एक बार", "एकदा")
                            "daily" -> getLabel("Daily", "दैनिक", "रोज")
                            else -> {
                                val days = task.weekDays
                                if (days.size >= 5 && days.containsAll(listOf(1, 2, 3, 4, 5))) getLabel("Weekdays", "कार्यदिवस", "कामाचे दिवस")
                                else if (days.size == 2 && days.containsAll(listOf(6, 7))) getLabel("Weekends", "सप्ताहांत", "शनि-रवि")
                                else if (days.size == 7) getLabel("Daily", "दैनिक", "रोज")
                                else getLabel("Custom Weekly", "कस्टम साप्ताहिक", "साप्ताहिक")
                            }
                        }
                        InfoRow(
                            icon = Icons.Rounded.CalendarToday,
                            label = getLabel("Repeat Schedule", "दोहराव अनुसूची", "वारंवारता"),
                            value = scheduleLabel
                        )

                        // Created Date
                        InfoRow(
                            icon = Icons.Default.CalendarMonth,
                            label = getLabel("Created", "निर्मित", "तयार केले"),
                            value = SimpleDateFormat("dd MMM, yyyy", Locale.US).format(Date(task.createdAt))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: Mark Complete / Incomplete
                val completeBtnColor = if (task.isCompleted) AppColors.danger.copy(alpha = 0.15f) else accentColor
                val completeBtnTextColor = if (task.isCompleted) AppColors.danger else Color.Black
                Button(
                    onClick = {
                        onToggleCompletion()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = completeBtnColor,
                        contentColor = completeBtnTextColor
                    )
                ) {
                    Text(
                        text = if (task.isCompleted) {
                            getLabel("Mark Incomplete", "अपूर्ण चिह्नित करें", "अपूर्ण म्हणून चिन्हांकित करा")
                        } else {
                            getLabel("Mark Complete", "पूर्ण चिह्नित करें", "पूर्ण चिन्हांकित करा")
                        },
                        style = AppTextStyles.actionButton.copy(
                            fontWeight = FontWeight.Bold,
                            color = completeBtnTextColor
                        )
                    )
                }

                // Button 2: Edit Task
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textPrimary),
                    border = BorderStroke(1.dp, AppColors.border)
                ) {
                    Text(
                        text = getLabel("Edit Task", "संपादित करें", "संपादन करा"),
                        style = AppTextStyles.actionButton.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = AppColors.textPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AppColors.textHint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = AppTextStyles.bodyMedium,
            color = AppColors.textSecondary
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = AppTextStyles.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

/**
 * Calculates current and longest streaks based on historical daily completion dates.
 */
fun calculateTaskStreak(
    completions: List<DailyCompletion>,
    task: TaskModel
): Pair<Int, Int> {
    val completedDates = completions.filter { it.isCompleted }.map { it.dateKey }.toSet()
    if (completedDates.isEmpty()) return Pair(0, 0)

    val sortedCompletedKeys = completedDates.sorted()

    if (task.frequency == "once") {
        val hasCompletion = completedDates.contains(task.dateKey)
        val streak = if (hasCompletion) 1 else 0
        return Pair(streak, streak)
    }

    val todayKey = DateUtils.getTodayKey()
    val taskCreatedDate = Date(task.createdAt)
    val taskCreatedKey = DateUtils.getDateKey(taskCreatedDate)

    val isScheduled: (String) -> Boolean = { key ->
        when (task.frequency) {
            "daily" -> true
            "weekly" -> {
                val dayOfWeek = DateUtils.getDayOfWeek(key)
                task.weekDays.contains(dayOfWeek)
            }
            else -> false
        }
    }

    // walk backward to calculate current streak
    var currentStreak = 0
    val tempCal = Calendar.getInstance()
    
    for (i in 0..365) {
        val walkKey = DateUtils.getDateKey(tempCal.time)
        if (walkKey < taskCreatedKey && walkKey < sortedCompletedKeys.first()) {
            break
        }

        if (isScheduled(walkKey)) {
            val wasCompleted = completedDates.contains(walkKey)
            if (wasCompleted) {
                currentStreak++
            } else {
                if (walkKey == todayKey) {
                    // today is not completed yet, skip it so yesterday's streak is still alive
                } else {
                    break
                }
            }
        }
        tempCal.add(Calendar.DAY_OF_YEAR, -1)
    }

    // walk forward to calculate longest streak
    var longestStreak = 0
    var runningStreak = 0
    val chronologicalCal = Calendar.getInstance()
    val firstCompletedDate = DateUtils.parseDateKey(sortedCompletedKeys.first()) ?: Date()
    chronologicalCal.time = firstCompletedDate

    val todayDate = Date()
    while (!chronologicalCal.time.after(todayDate)) {
        val walkKey = DateUtils.getDateKey(chronologicalCal.time)
        if (isScheduled(walkKey)) {
            val wasCompleted = completedDates.contains(walkKey)
            if (wasCompleted) {
                runningStreak++
            } else {
                if (walkKey == todayKey) {
                    // runningStreak remains alive until end of day
                } else {
                    runningStreak = 0
                }
            }
        }
        if (runningStreak > longestStreak) {
            longestStreak = runningStreak
        }
        chronologicalCal.add(Calendar.DAY_OF_YEAR, 1)
    }

    return Pair(currentStreak, maxOf(currentStreak, longestStreak))
}

fun getCurrentWeekKeys(): List<String> {
    val cal = Calendar.getInstance()
    val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysToSubtract = when (currentDayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }
    cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)

    val weekKeys = mutableListOf<String>()
    for (i in 0..6) {
        weekKeys.add(DateUtils.getDateKey(cal.time))
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return weekKeys
}
