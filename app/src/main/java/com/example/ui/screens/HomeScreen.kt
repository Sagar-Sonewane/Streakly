package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.content.Context
import kotlinx.coroutines.launch
import com.example.core.constants.Quotes
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.core.utils.DateUtils
import com.example.core.utils.Responsive
import com.example.data.models.TaskModel
import com.example.providers.Providers
import com.example.shared.widgets.SectionHeader
import com.example.shared.widgets.StreakFab
import com.example.shared.widgets.TimePickerSheet
import java.util.*

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val settingsProvider = Providers.getSettings()
    val streakProvider = Providers.getStreak()
    val taskProvider = Providers.getTask()

    val settingsState by settingsProvider.settingsState.collectAsState()
    val streakState by streakProvider.streakState.collectAsState()
    val tasksState by taskProvider.tasksState.collectAsState()
    val currentDateKey by taskProvider.currentDateKey.collectAsState()
    val dayRecordsState by streakProvider.dayRecordsState.collectAsState()

    val language = settingsState.language
    val accentColor = AppColors.accentColorOptions[settingsState.accentColorIndex]
    val isPastDate = currentDateKey < DateUtils.getTodayKey()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskModel?>(null) }

    // Multi-language translation labels
    val getLabel = { en: String, hi: String, mr: String ->
        when (language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.bgPrimary,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (!isPastDate) {
                StreakFab(
                    onClick = {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        showAddTaskDialog = true
                    },
                    accentColor = accentColor
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 16.dp, bottom = Responsive.h(12f)),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                StreakFlameCard(
                    currentStreak = streakState.currentStreak,
                    longestStreak = streakState.longestStreak,
                    accentColor = accentColor,
                    getLabel = getLabel
                )
            }
            item {
                WeekCalendarBar(
                    currentDateKey = currentDateKey,
                    onDateSelected = {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        taskProvider.setDateKey(it)
                    },
                    dayRecords = dayRecordsState,
                    language = language,
                    accentColor = accentColor
                )
            }
            if (isPastDate) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = AppColors.textHint,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = getLabel("Past day — view only", "पिछला दिन — केवल देखने के लिए", "मागील दिवस — फक्त पाहण्याकरिता"),
                            style = AppTextStyles.caption.copy(fontWeight = FontWeight.Medium),
                            color = AppColors.textHint
                        )
                    }
                }
            }
            item {
                val completedCount = tasksState.count { it.isCompleted }
                val totalCount = tasksState.size
                ProgressSummarySection(
                    completed = completedCount,
                    total = totalCount,
                    accentColor = accentColor,
                    getLabel = getLabel
                )
            }
            item {
                QuoteHeader(language = language, accentColor = accentColor)
            }
            if (tasksState.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "No Tasks",
                                tint = AppColors.textHint,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = getLabel("No active habits tracked yet", "कोई सक्रिय आदत अभी तक नहीं है", "अजून कोणतीही सवय समाविष्ट नाही"),
                                style = AppTextStyles.titleMedium,
                                color = AppColors.textSecondary
                            )
                            Text(
                                text = getLabel("Tap the + below to write down a discipline.", "अनुशासन जोड़ने के लिए नीचे + दबाएं।", "शिस्त जोडण्यासाठी खालील + बटणावर दाबा."),
                                style = AppTextStyles.bodyMedium,
                                color = AppColors.textHint,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = getLabel("TODAY'S HABITS", "आज की आदतें", "आजच्या सवयी"),
                        style = AppTextStyles.hint(AppColors.textHint).copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.W600
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                items(tasksState, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        isLocked = isPastDate,
                        onToggleCompletion = {
                            taskProvider.toggleTaskCompletion(task)
                            if (!task.isCompleted) {
                                com.example.core.utils.SoundService.playSuccess()
                                com.example.core.utils.HapticService.confirm()
                            } else {
                                com.example.core.utils.SoundService.playDelete()
                                com.example.core.utils.HapticService.doubleClick()
                            }
                        },
                        onEdit = {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            editingTask = task
                        },
                        onDelete = {
                            com.example.core.utils.SoundService.playDelete()
                            com.example.core.utils.HapticService.doubleClick()
                            taskProvider.deleteTask(task.id)
                        }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            accentColor = accentColor,
            getLabel = getLabel,
            onDismiss = { showAddTaskDialog = false },
            onTaskAdded = { title, desc, time, colorIdx, freq, weekdays, importance ->
                taskProvider.addTask(title, desc, time, colorIdx, freq, weekdays, importance)
                showAddTaskDialog = false
            }
        )
    }

    editingTask?.let { taskToEdit ->
        EditTaskDialog(
            task = taskToEdit,
            accentColor = accentColor,
            getLabel = getLabel,
            onDismiss = { editingTask = null },
            onTaskUpdated = { title, desc, time, colorIdx, freq, weekdays, importance ->
                taskProvider.updateTask(
                    taskToEdit.copy(
                        title = title,
                        description = desc,
                        timeLabel = time,
                        colorIndex = colorIdx,
                        frequency = freq,
                        weekDaysRaw = weekdays,
                        importance = importance
                    )
                )
                editingTask = null
            }
        )
    }
}

@Composable
fun QuoteHeader(language: String, accentColor: Color) {
    val context = LocalContext.current
    val todayKey = remember { DateUtils.getTodayKey() }
    val prefKey = remember { "quote_dismissed_$todayKey" }
    
    val sharedPrefs = remember {
        context.getSharedPreferences("streakly_prefs", Context.MODE_PRIVATE)
    }
    
    var isDismissedInPrefs by remember {
        mutableStateOf(sharedPrefs.getBoolean(prefKey, false))
    }
    
    if (isDismissedInPrefs) return

    val cal = Calendar.getInstance()
    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    val quote = remember(dayOfYear) { Quotes.getQuoteForDay(dayOfYear) }

    var isPaused by remember { mutableStateOf(false) }
    var timeLeftMs by remember { mutableStateOf(30000) } // 30 seconds
    var isDismissingAnimation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isPaused, isDismissingAnimation) {
        if (!isPaused && !isDismissingAnimation) {
            val tickRate = 50L
            while (timeLeftMs > 0) {
                kotlinx.coroutines.delay(tickRate)
                timeLeftMs = (timeLeftMs - tickRate.toInt()).coerceAtLeast(0)
            }
            isDismissingAnimation = true
            kotlinx.coroutines.delay(300)
            sharedPrefs.edit().putBoolean(prefKey, true).apply()
            isDismissedInPrefs = true
        }
    }

    AnimatedVisibility(
        visible = !isDismissingAnimation,
        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(300)) + androidx.compose.animation.fadeOut(animationSpec = tween(300))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.bgSecondary)
                .border(1.dp, AppColors.border, RoundedCornerShape(20.dp))
                .clickable {
                    isPaused = !isPaused
                    com.example.core.utils.SoundService.playToggle()
                    com.example.core.utils.HapticService.selectionClick()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Responsive.sp(14f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "STREAKLY • DAILY MOTIVATION",
                            style = AppTextStyles.caption.copy(
                                fontWeight = FontWeight.Black,
                                color = accentColor,
                                fontSize = Responsive.fp(10f),
                                letterSpacing = 1.2.sp
                            )
                        )
                        if (isPaused) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• PAUSED",
                                style = AppTextStyles.caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.textHint,
                                    fontSize = Responsive.fp(9f)
                                )
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            isDismissingAnimation = true
                            scope.launch {
                                kotlinx.coroutines.delay(300)
                                sharedPrefs.edit().putBoolean(prefKey, true).apply()
                                isDismissedInPrefs = true
                            }
                        },
                        modifier = Modifier.size(Responsive.sp(20f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Quote",
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(Responsive.sp(16f))
                        )
                    }
                }
                
                Text(
                    text = "\"${quote.text}\"",
                    style = AppTextStyles.bodyMedium.copy(
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textPrimary,
                        fontSize = Responsive.adaptive(12f, 13f, 14f).sp
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) {
                    if (isPaused) {
                        Text(
                            text = "Tap block to resume",
                            style = AppTextStyles.bodySmall.copy(
                                color = AppColors.textHint,
                                fontSize = Responsive.fp(10f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    Text(
                        text = "- ${quote.character} (${quote.anime})",
                        style = AppTextStyles.bodySmall.copy(
                            color = AppColors.textSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = Responsive.fp(11f)
                        ),
                        textAlign = TextAlign.End
                    )
                }
            }

            val progress = timeLeftMs / 30000f
            val barColor = if (timeLeftMs <= 5000) AppColors.danger else accentColor

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
fun StreakFlameCard(
    currentStreak: Int,
    longestStreak: Int,
    accentColor: Color,
    getLabel: (String, String, String) -> String
) {
    val isDark = AppColors.isDark
    val bestLabel = getLabel("Best: $longestStreak days", "सर्वोच्च: $longestStreak दिन", "सर्वोत्तम: $longestStreak दिवस")

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = if (isDark) 0.dp else 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = accentColor.copy(alpha = 0.25f),
                spotColor = accentColor.copy(alpha = 0.25f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor,
                        accentColor.copy(alpha = 0.85f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Left: Large bold graphic container with Flame Icon (Avatar style)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Flame Icon",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            // Middle: Information column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Top Tag Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = getLabel("ACTIVE STREAK", "सक्रिय निरंतरता", "सक्रिय सातत्य"),
                        style = AppTextStyles.caption.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Big main number and label
                Text(
                    text = "$currentStreak " + getLabel("Days", "दिन", "दिवस"),
                    style = AppTextStyles.headingLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Trophy subtext
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy icon",
                        tint = AppColors.warning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bestLabel,
                        style = AppTextStyles.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun WeekCalendarBar(
    currentDateKey: String,
    onDateSelected: (String) -> Unit,
    dayRecords: List<com.example.data.models.DayRecord>,
    language: String,
    accentColor: Color
) {
    val isDark = AppColors.isDark
    val textHintColor = AppColors.textHint
    val textSecColor = AppColors.textSecondary
    val textPriColor = AppColors.textPrimary
    
    val dateKeys = remember {
        val cal = Calendar.getInstance()
        val list = mutableListOf<String>()
        for (i in 0..6) {
            list.add(DateUtils.getDateKey(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        list.reverse()
        list
    }

    com.example.shared.widgets.StreaklyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        padding = 16.dp,
        borderRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when(language) {
                        "hi" -> "साप्ताहिक अनुशासन"
                        "mr" -> "साप्ताहिक वेळापत्रक"
                        else -> "Weekly Tracker"
                    },
                    style = AppTextStyles.sectionHeader(textPriColor).copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = DateUtils.getFormattedDate(currentDateKey, language),
                    style = AppTextStyles.label(accentColor).copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                dateKeys.forEach { key ->
                    val isTodaySelected = key == currentDateKey
                    val dayLabel = DateUtils.getDayOfWeekLabel(key, language)
                    val dayNum = DateUtils.getDayOfMonthFromDateKey(key).toString()
                    
                    val record = dayRecords.find { it.dateKey == key }
                    val isDone = record?.let { it.tasksCompleted > 0 && it.tasksCompleted >= it.tasksTotal } ?: false

                    val activeBg = if (isDark) accentColor else Color(0xFF0F1320)
                    val inactiveBg = if (isDark) AppColors.bgTertiary else Color(0xFFF8F9FB)
                    val itemBg = if (isTodaySelected) activeBg else inactiveBg
                    val itemTextColor = if (isTodaySelected) Color.White else textPriColor

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(itemBg)
                            .then(
                                if (!isTodaySelected) {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = if (isDone) accentColor.copy(alpha = 0.4f) else AppColors.border,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                } else Modifier
                            )
                            .clickable { onDateSelected(key) }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Dot at top of pill
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isTodaySelected) Color.White
                                    else if (isDone) accentColor
                                    else Color.Transparent
                                )
                        )

                        Text(
                            text = dayLabel,
                            style = AppTextStyles.hint(
                                if (isTodaySelected) Color.White.copy(alpha = 0.8f) else textSecColor
                            ).copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )

                        Text(
                            text = dayNum,
                            style = AppTextStyles.label(itemTextColor).copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressSummarySection(
    completed: Int,
    total: Int,
    accentColor: Color,
    getLabel: (String, String, String) -> String
) {
    val isDark = AppColors.isDark
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = getLabel("DAILY PROGRESS SUMMARY", "दैनिक प्रगति विवरण", "दैनिक प्रगती अहवाल"),
            style = AppTextStyles.hint(AppColors.textHint).copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.W800
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip 1: Total Habits
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(AppColors.bgSecondary)
                    .border(1.dp, AppColors.border, RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Total",
                        tint = AppColors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = getLabel("Total: $total", "कुल: $total", "एकूण: $total"),
                        style = AppTextStyles.caption.copy(
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Chip 2: Completed
            val successColor = AppColors.success
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(successColor.copy(alpha = 0.12f))
                    .border(1.dp, successColor.copy(alpha = 0.35f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = successColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = getLabel("Done: $completed", "पूर्ण: $completed", "पूर्ण: $completed"),
                        style = AppTextStyles.caption.copy(
                            color = if (isDark) successColor else AppColors.getLegibleColor(successColor),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Chip 3: Pending
            val pendingCount = (total - completed).coerceAtLeast(0)
            val pendingColor = AppColors.warning
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(pendingColor.copy(alpha = 0.12f))
                    .border(1.dp, pendingColor.copy(alpha = 0.35f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Remaining",
                        tint = pendingColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = getLabel("Left: $pendingCount", "शेष: $pendingCount", "उर्वरित: $pendingCount"),
                        style = AppTextStyles.caption.copy(
                            color = if (isDark) pendingColor else AppColors.getLegibleColor(pendingColor),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItemRow(
    task: TaskModel,
    isLocked: Boolean = false,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = AppColors.isDark
    // Category color mapping
    val taskColor = AppColors.taskCategoryColors.getOrNull(task.colorIndex) ?: AppColors.accentOrange
    val importanceColor = AppColors.getImportanceColor(task.importance)

    // Soft colored container background in light mode, dark card with colored left border in dark mode
    val cardBg = if (isDark) {
        AppColors.bgSecondary.copy(alpha = 0.9f)
    } else {
        taskColor.copy(alpha = 0.12f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .then(
                if (isDark) {
                    Modifier.border(
                        width = 1.dp,
                        color = if (task.isCompleted) AppColors.success.copy(alpha = 0.5f) else AppColors.border,
                        shape = RoundedCornerShape(20.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = if (task.isCompleted) AppColors.success.copy(alpha = 0.4f) else taskColor.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            )
            .testTag("task_item_card_${task.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Draw a thick left border color block if in dark mode or if it's high priority
            if (isDark || task.importance == "priority") {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(76.dp)
                        .background(
                            if (task.importance == "priority") importanceColor else taskColor,
                            RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                        )
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .graphicsLayer {
                        // Soft fade when completed
                        alpha = if (task.isCompleted) 0.65f else 1.0f
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Checkbox border / background -> success or importance color
                    Box(
                        modifier = Modifier
                            .size(Responsive.sp(26f))
                            .graphicsLayer { alpha = if (isLocked) 0.5f else 1f }
                            .clip(CircleShape)
                            .background(if (task.isCompleted) AppColors.success else Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (task.isCompleted) AppColors.success else (if (isDark) taskColor else AppColors.getLegibleColor(taskColor)),
                                shape = CircleShape
                            )
                            .clickable(enabled = !isLocked) { onToggleCompletion() }
                            .testTag("task_toggle_check_${task.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(Responsive.sp(14f))
                            )
                        }
                    }

                    // Title + Subtitles
                    Column {
                        // Title row
                        Text(
                            text = task.title,
                            style = AppTextStyles.titleMedium.copy(
                                textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                color = if (task.isCompleted) AppColors.textSecondary else AppColors.textPrimary,
                                fontSize = Responsive.fp(16f),
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Description line
                        if (!task.description.isNullOrEmpty()) {
                            Text(
                                text = task.description,
                                style = AppTextStyles.bodySmall.copy(fontSize = Responsive.fp(12f)),
                                color = AppColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Frequency and time metadata row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            // Frequency label
                            if (task.frequency != "once") {
                                val freqIcon = if (task.frequency == "daily") Icons.Default.Refresh else Icons.Default.DateRange
                                val freqLabel = if (task.frequency == "daily") "Daily" else {
                                    val names = mapOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
                                    val days = task.weekDays
                                    if (days.size >= 5 && days.containsAll(listOf(1, 2, 3, 4, 5))) "Weekdays"
                                    else if (days.size == 2 && days.containsAll(listOf(6, 7))) "Weekends"
                                    else if (days.size == 7) "Every day"
                                    else days.sorted().mapNotNull { names[it]?.first()?.toString() }.joinToString(" ")
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = freqIcon,
                                        contentDescription = "Frequency",
                                        tint = if (isDark) AppColors.textHint else AppColors.getLegibleColor(taskColor).copy(alpha = 0.8f),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = freqLabel,
                                        style = AppTextStyles.caption.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) AppColors.textSecondary else AppColors.getLegibleColor(taskColor).copy(alpha = 0.8f)
                                        )
                                    )
                                }
                            }

                            // Time label
                            if (!task.timeLabel.isNullOrEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Time",
                                        tint = AppColors.textHint,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = task.timeLabel,
                                        style = AppTextStyles.caption.copy(fontSize = 11.sp),
                                        color = AppColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Action controls row (importance label + buttons)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (task.importance != "regular") {
                        val badgeText = if (task.importance == "priority") "High" else "Mid"
                        val badgeColor = AppColors.getImportanceColor(task.importance)
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .border(0.5.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = AppTextStyles.caption.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            )
                        }
                    }

                    if (!isLocked) {
                        // Edit
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(Responsive.sp(34f))
                                .testTag("task_edit_button_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Task",
                                tint = if (isDark) AppColors.textSecondary else AppColors.getLegibleColor(taskColor).copy(alpha = 0.8f),
                                modifier = Modifier.size(Responsive.sp(16f))
                            )
                        }

                        // Delete
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(Responsive.sp(34f))
                                .testTag("task_delete_button_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Task",
                                tint = AppColors.danger.copy(alpha = 0.6f),
                                modifier = Modifier.size(Responsive.sp(16f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    accentColor: Color,
    getLabel: (String, String, String) -> String,
    onDismiss: () -> Unit,
    onTaskAdded: (String, String?, String?, Int, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedColorIdx by remember { mutableIntStateOf(0) }

    // Frequency state variables
    var selectedFrequency by remember { mutableStateOf("daily") } // "once" | "daily" | "weekly"
    val selectedWeekDays = remember { mutableStateListOf<Int>() }
    var selectedImportance by remember { mutableStateOf("regular") } // "regular" | "moderate" | "priority"

    // Time picker state variables
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var isAM by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }

    val formattedTime = remember(showTimePicker, selectedHour, selectedMinute, isAM) {
        if (!showTimePicker) null
        else {
            val h = selectedHour.toString().padStart(2, '0')
            val m = selectedMinute.toString().padStart(2, '0')
            val period = if (isAM) "AM" else "PM"
            "$h:$m $period"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bgSecondary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = getLabel("Create Habit", "नयी आदत", "नवीन सवय जोडा"),
                    style = AppTextStyles.headingMedium,
                    color = accentColor
                )

                // Title Input (Required)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(getLabel("Habit Title *", "आदतों का नाम *", "सवय शीर्षक *")) },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = AppColors.border,
                        focusedLabelColor = accentColor
                    )
                )

                // Description Input (Optional)
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(getLabel("Description (Optional)", "विवरण (वैकल्पिक)", "स्पष्टीकरण (पर्यायी)")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = AppColors.border,
                        focusedLabelColor = accentColor
                    )
                )

                // ── TIME PICKER SYSTEM ─────────────────
                SectionLabel(getLabel("Time", "समय", "वेळ"))

                // Tappable row that expands to show time picker dial option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.bgTertiary)
                        .border(
                            width = if (showTimePicker) 1.5.dp else 0.8.dp,
                            color = if (showTimePicker) accentColor else AppColors.border,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            showTimePicker = !showTimePicker
                        }
                        .padding(horizontal = 16.dp, vertical = 13.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Schedule picker trigger",
                            tint = if (showTimePicker) accentColor else AppColors.textHint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formattedTime ?: getLabel("Set time (optional)", "समय निर्धारित करें (वैकल्पिक)", "वेळ निश्चित करा (पर्यायी)"),
                            style = AppTextStyles.bodyMedium.copy(fontSize = 14.sp),
                            color = if (formattedTime != null) AppColors.textPrimary else AppColors.textHint,
                            modifier = Modifier.weight(1f)
                        )
                        if (formattedTime != null) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        com.example.core.utils.SoundService.playTap()
                                        com.example.core.utils.HapticService.selectionClick()
                                        showTimePicker = false
                                        selectedHour = 8
                                        selectedMinute = 0
                                        isAM = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear selected time",
                                    tint = AppColors.textHint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (showTimePicker) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Time dial expandable",
                                tint = AppColors.textHint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Expandable picker section
                AnimatedVisibility(visible = showTimePicker) {
                    TimePickerSheet(
                        selectedHour = selectedHour,
                        selectedMinute = selectedMinute,
                        isAM = isAM,
                        onTimeChanged = { hour, minute, am ->
                            selectedHour = hour
                            selectedMinute = minute
                            isAM = am
                        },
                        accentColor = accentColor,
                        getLabel = getLabel
                    )
                }

                // ── REPEAT SYSTEM ─────────────────
                SectionLabel(getLabel("Repeat", "दौहराएं", "पुनरावृत्ती"))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val freqOptions = listOf(
                        Triple("once", getLabel("Once", "एक बार", "एकदा"), Icons.Default.LooksOne),
                        Triple("daily", getLabel("Daily", "दैनिक", "रोज"), Icons.Default.Repeat),
                        Triple("weekly", getLabel("Weekly", "साप्ताहिक", "साप्ताहिक"), Icons.Default.DateRange)
                    )

                    freqOptions.forEach { (value, label, icon) ->
                        val isSelected = selectedFrequency == value
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) accentColor.copy(alpha = 0.12f) else AppColors.bgTertiary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    com.example.core.utils.SoundService.playTap()
                                    com.example.core.utils.HapticService.selectionClick()
                                    selectedFrequency = value
                                    if (value != "weekly") {
                                        selectedWeekDays.clear()
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                color = if (isSelected) accentColor else AppColors.border
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = value,
                                    tint = if (isSelected) accentColor else AppColors.textHint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    style = AppTextStyles.caption.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) accentColor else AppColors.textHint
                                )
                            }
                        }
                    }
                }

                // Expandable weekday picker (For Weekly task configuration)
                if (selectedFrequency == "weekly") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (selectedWeekDays.isEmpty()) getLabel("Select days", "दिनों का चयन करें", "दिवस निवडा")
                                   else "${selectedWeekDays.size} " + getLabel("day(s) selected", "दिन चयनित", "दिवस निवडले"),
                            style = AppTextStyles.caption.copy(fontSize = 11.sp),
                            color = if (selectedWeekDays.isEmpty()) AppColors.danger else AppColors.textHint
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            days.forEachIndexed { i, dayName ->
                                val dayNum = i + 1
                                val isSelected = selectedWeekDays.contains(dayNum)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) accentColor else AppColors.bgTertiary)
                                        .border(
                                            width = if (isSelected) 0.dp else 0.8.dp,
                                            color = if (isSelected) Color.Transparent else AppColors.border,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            com.example.core.utils.SoundService.playTap()
                                            com.example.core.utils.HapticService.selectionClick()
                                            if (isSelected) selectedWeekDays.remove(dayNum)
                                            else selectedWeekDays.add(dayNum)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayName,
                                        style = AppTextStyles.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                        color = if (isSelected) Color.Black else AppColors.textHint
                                    )
                                }
                            }
                        }

                        // Quick Select Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val quickPicks = listOf(
                                Triple("Weekdays", listOf(1, 2, 3, 4, 5), getLabel("Weekdays", "कार्यदिवस", "कामाचे दिवस")),
                                Triple("Weekends", listOf(6, 7), getLabel("Weekends", "सप्ताहांत", "सुट्टीचे दिवस")),
                                Triple("All days", listOf(1, 2, 3, 4, 5, 6, 7), getLabel("All days", "सभी दिन", "सर्व दिवस"))
                            )
                            quickPicks.forEach { (label, dayValues, displayLabel) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(AppColors.bgTertiary)
                                        .border(0.8.dp, AppColors.border, RoundedCornerShape(20.dp))
                                        .clickable {
                                            com.example.core.utils.SoundService.playTap()
                                            com.example.core.utils.HapticService.selectionClick()
                                            selectedWeekDays.clear()
                                            selectedWeekDays.addAll(dayValues)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = displayLabel,
                                        style = AppTextStyles.caption.copy(fontSize = 11.sp),
                                        color = AppColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // ── IMPORTANCE SYSTEM ─────────────────
                SectionLabel(getLabel("Importance", "महत्व", "प्राधान्य"))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val levels = listOf("regular", "moderate", "priority")
                    val icons = mapOf(
                        "regular" to Icons.Default.RadioButtonUnchecked,
                        "moderate" to Icons.Default.FlashOn,
                        "priority" to Icons.Default.Whatshot
                    )
                    val labelsEnHiMr = mapOf(
                        "regular" to Triple("Regular", "सामान्य", "सामान्य"),
                        "moderate" to Triple("Moderate", "मध्यम", "मध्यम"),
                        "priority" to Triple("Priority", "प्राथमिकता", "प्राधान्य")
                    )

                    levels.forEach { level ->
                        val color = AppColors.getImportanceColor(level)
                        val isSelected = selectedImportance == level
                        val labels = labelsEnHiMr[level]!!
                        val termLabel = getLabel(labels.first, labels.second, labels.third)

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.12f) else AppColors.bgTertiary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                            com.example.core.utils.SoundService.playTap()
                                            com.example.core.utils.HapticService.selectionClick()
                                            selectedImportance = level
                                        },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                color = if (isSelected) color else AppColors.border
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icons[level]!!,
                                    contentDescription = level,
                                    tint = if (isSelected) color else AppColors.textHint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = termLabel,
                                    style = AppTextStyles.caption.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) color else AppColors.textHint
                                )
                            }
                        }
                    }
                }

                // Custom Color Tag Choice
                SectionLabel(getLabel("Color Tag", "रंग लेबल", "रंग टॅग"))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppColors.accentColorOptions.forEachIndexed { index, color ->
                        val isSelected = index == selectedColorIdx
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    com.example.core.utils.SoundService.playTap()
                                    com.example.core.utils.HapticService.selectionClick()
                                    selectedColorIdx = index
                                }
                        )
                    }
                }

                // Bottom actions buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        onDismiss()
                    }) {
                        Text(
                            text = getLabel("Cancel", "रद्द करें", "रद्द करा"),
                            color = AppColors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                // Validation for weekly tasks
                                if (selectedFrequency == "weekly" && selectedWeekDays.isEmpty()) {
                                    com.example.core.utils.SoundService.playError()
                                    com.example.core.utils.HapticService.error()
                                    android.widget.Toast.makeText(
                                        com.example.StreaklyApp.instance,
                                        "Please select at least one day for weekly tasks",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                com.example.core.utils.SoundService.playAdd()
                                com.example.core.utils.HapticService.confirm()
                                onTaskAdded(
                                    title.trim(),
                                    desc.trim().ifBlank { null },
                                    formattedTime,
                                    selectedColorIdx,
                                    selectedFrequency,
                                    selectedWeekDays.joinToString(","),
                                    selectedImportance
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("add_task_dialog_confirm")
                    ) {
                        Text(
                            text = getLabel("Add", "जोड़ें", "जोडा"),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Time picker dialog handled internally by TimePickerSheet
}

@Composable
fun EditTaskDialog(
    task: TaskModel,
    accentColor: Color,
    getLabel: (String, String, String) -> String,
    onDismiss: () -> Unit,
    onTaskUpdated: (String, String?, String?, Int, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var desc by remember { mutableStateOf(task.description ?: "") }
    var selectedColorIdx by remember { mutableIntStateOf(task.colorIndex) }

    // Initialize frequency options
    var selectedFrequency by remember { mutableStateOf(task.frequency) }
    val selectedWeekDays = remember {
        mutableStateListOf<Int>().apply { addAll(task.weekDays) }
    }
    var selectedImportance by remember { mutableStateOf(task.importance) }

    // Parse time picker initial state
    val initVals = remember(task.timeLabel) {
        var hour = 8
        var minute = 0
        var isAMVal = true
        var showState = false
        if (!task.timeLabel.isNullOrBlank()) {
            try {
                val parts = task.timeLabel.split(" ")
                val timeParts = parts[0].split(":")
                hour = timeParts[0].toInt()
                minute = timeParts[1].toInt()
                isAMVal = parts[1].equals("AM", ignoreCase = true)
                showState = true
            } catch (e: Exception) {
                // ignore
            }
        }
        Triple(hour, minute, Pair(isAMVal, showState))
    }

    var selectedHour by remember { mutableIntStateOf(initVals.first) }
    var selectedMinute by remember { mutableIntStateOf(initVals.second) }
    var isAM by remember { mutableStateOf(initVals.third.first) }
    var showTimePicker by remember { mutableStateOf(initVals.third.second) }
    var showNativeTimePicker by remember { mutableStateOf(false) }

    val formattedTime = remember(showTimePicker, selectedHour, selectedMinute, isAM) {
        if (!showTimePicker) null
        else {
            val h = selectedHour.toString().padStart(2, '0')
            val m = selectedMinute.toString().padStart(2, '0')
            val period = if (isAM) "AM" else "PM"
            "$h:$m $period"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bgSecondary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = getLabel("Edit Habit", "आदत संपादित करें", "सवय संपादन करा"),
                    style = AppTextStyles.headingMedium,
                    color = accentColor
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(getLabel("Habit Title *", "आदतों का नाम *", "सवय शीर्षक *")) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_task_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = AppColors.border,
                        focusedLabelColor = accentColor
                    )
                )

                // Description Input
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(getLabel("Description (Optional)", "विवरण (वैकल्पिक)", "स्पष्टीकरण (पर्यायी)")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = AppColors.border,
                        focusedLabelColor = accentColor
                    )
                )

                // ── TIME PICKER SYSTEM ─────────────────
                SectionLabel(getLabel("Time", "समय", "वेळ"))

                // Tappable row that expands to show custom picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.bgTertiary)
                        .border(
                            width = if (showTimePicker) 1.5.dp else 0.8.dp,
                            color = if (showTimePicker) accentColor else AppColors.border,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            showTimePicker = !showTimePicker
                        }
                        .padding(horizontal = 16.dp, vertical = 13.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Schedule picker trigger",
                            tint = if (showTimePicker) accentColor else AppColors.textHint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formattedTime ?: getLabel("Set time (optional)", "समय निर्धारित करें (वैकल्पिक)", "वेळ निश्चित करा (पर्यायी)"),
                            style = AppTextStyles.bodyMedium.copy(fontSize = 14.sp),
                            color = if (formattedTime != null) AppColors.textPrimary else AppColors.textHint,
                            modifier = Modifier.weight(1f)
                        )
                        if (formattedTime != null) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        com.example.core.utils.SoundService.playTap()
                                        com.example.core.utils.HapticService.selectionClick()
                                        showTimePicker = false
                                        selectedHour = 8
                                        selectedMinute = 0
                                        isAM = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear selected time",
                                    tint = AppColors.textHint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (showTimePicker) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Time dial expandable",
                                tint = AppColors.textHint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Expandable time picker section
                AnimatedVisibility(visible = showTimePicker) {
                    TimePickerSheet(
                        selectedHour = selectedHour,
                        selectedMinute = selectedMinute,
                        isAM = isAM,
                        onTimeChanged = { hour, minute, am ->
                            selectedHour = hour
                            selectedMinute = minute
                            isAM = am
                        },
                        accentColor = accentColor,
                        getLabel = getLabel
                    )
                }

                // ── REPEAT SYSTEM ─────────────────
                SectionLabel(getLabel("Repeat", "दौहराएं", "पुनरावृत्ती"))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val freqOptions = listOf(
                        Triple("once", getLabel("Once", "एक बार", "एकदा"), Icons.Default.LooksOne),
                        Triple("daily", getLabel("Daily", "दैनिक", "रोज"), Icons.Default.Repeat),
                        Triple("weekly", getLabel("Weekly", "साप्ताहिक", "साप्ताहिक"), Icons.Default.DateRange)
                    )

                    freqOptions.forEach { (value, label, icon) ->
                        val isSelected = selectedFrequency == value
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) accentColor.copy(alpha = 0.12f) else AppColors.bgTertiary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    com.example.core.utils.SoundService.playTap()
                                    com.example.core.utils.HapticService.selectionClick()
                                    selectedFrequency = value
                                    if (value != "weekly") {
                                        selectedWeekDays.clear()
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                color = if (isSelected) accentColor else AppColors.border
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = value,
                                    tint = if (isSelected) accentColor else AppColors.textHint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    style = AppTextStyles.caption.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) accentColor else AppColors.textHint
                                )
                            }
                        }
                    }
                }

                // Expandable Weekly grid
                if (selectedFrequency == "weekly") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (selectedWeekDays.isEmpty()) getLabel("Select days", "दिनों का चयन करें", "दिवस निवडा")
                                   else "${selectedWeekDays.size} " + getLabel("day(s) selected", "दिन चयनित", "दिवस निवडले"),
                            style = AppTextStyles.caption.copy(fontSize = 11.sp),
                            color = if (selectedWeekDays.isEmpty()) AppColors.danger else AppColors.textHint
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            days.forEachIndexed { i, dayName ->
                                val dayNum = i + 1
                                val isSelected = selectedWeekDays.contains(dayNum)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) accentColor else AppColors.bgTertiary)
                                        .border(
                                            width = if (isSelected) 0.dp else 0.8.dp,
                                            color = if (isSelected) Color.Transparent else AppColors.border,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            com.example.core.utils.SoundService.playTap()
                                            com.example.core.utils.HapticService.selectionClick()
                                            if (isSelected) selectedWeekDays.remove(dayNum)
                                            else selectedWeekDays.add(dayNum)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayName,
                                        style = AppTextStyles.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                        color = if (isSelected) Color.Black else AppColors.textHint
                                    )
                                }
                            }
                        }

                        // Quick Select Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val quickPicks = listOf(
                                Triple("Weekdays", listOf(1, 2, 3, 4, 5), getLabel("Weekdays", "कार्यदिवस", "कामाचे दिवस")),
                                Triple("Weekends", listOf(6, 7), getLabel("Weekends", "सप्ताहांत", "सुट्टीचे दिवस")),
                                Triple("All days", listOf(1, 2, 3, 4, 5, 6, 7), getLabel("All days", "सभी दिन", "सर्व दिवस"))
                            )
                            quickPicks.forEach { (label, dayValues, displayLabel) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(AppColors.bgTertiary)
                                        .border(0.8.dp, AppColors.border, RoundedCornerShape(20.dp))
                                        .clickable {
                                            com.example.core.utils.SoundService.playTap()
                                            com.example.core.utils.HapticService.selectionClick()
                                            selectedWeekDays.clear()
                                            selectedWeekDays.addAll(dayValues)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = displayLabel,
                                        style = AppTextStyles.caption.copy(fontSize = 11.sp),
                                        color = AppColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // ── IMPORTANCE SYSTEM ─────────────────
                SectionLabel(getLabel("Importance", "महत्व", "प्राधान्य"))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val levels = listOf("regular", "moderate", "priority")
                    val icons = mapOf(
                        "regular" to Icons.Default.RadioButtonUnchecked,
                        "moderate" to Icons.Default.FlashOn,
                        "priority" to Icons.Default.Whatshot
                    )
                    val labelsEnHiMr = mapOf(
                        "regular" to Triple("Regular", "सामान्य", "सामान्य"),
                        "moderate" to Triple("Moderate", "मध्यम", "मध्यम"),
                        "priority" to Triple("Priority", "प्राथमिकता", "प्राधान्य")
                    )

                    levels.forEach { level ->
                        val color = AppColors.getImportanceColor(level)
                        val isSelected = selectedImportance == level
                        val labels = labelsEnHiMr[level]!!
                        val termLabel = getLabel(labels.first, labels.second, labels.third)

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.12f) else AppColors.bgTertiary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                            com.example.core.utils.SoundService.playTap()
                                            com.example.core.utils.HapticService.selectionClick()
                                            selectedImportance = level
                                        },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                color = if (isSelected) color else AppColors.border
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icons[level]!!,
                                    contentDescription = level,
                                    tint = if (isSelected) color else AppColors.textHint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = termLabel,
                                    style = AppTextStyles.caption.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) color else AppColors.textHint
                                )
                            }
                        }
                    }
                }

                // Custom Color Tag Choice
                SectionLabel(getLabel("Color Tag", "रंग लेबल", "रंग टॅग"))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppColors.accentColorOptions.forEachIndexed { index, color ->
                        val isSelected = index == selectedColorIdx
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    com.example.core.utils.SoundService.playTap()
                                    com.example.core.utils.HapticService.selectionClick()
                                    selectedColorIdx = index
                                }
                        )
                    }
                }

                // Bottom buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        onDismiss()
                    }) {
                        Text(
                            text = getLabel("Cancel", "रद्द करें", "रद्द करा"),
                            color = AppColors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                if (selectedFrequency == "weekly" && selectedWeekDays.isEmpty()) {
                                    com.example.core.utils.SoundService.playError()
                                    com.example.core.utils.HapticService.error()
                                    android.widget.Toast.makeText(
                                        com.example.StreaklyApp.instance,
                                        "Please select at least one day for weekly tasks",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                com.example.core.utils.SoundService.playTap()
                                com.example.core.utils.HapticService.confirm()
                                onTaskUpdated(
                                    title.trim(),
                                    desc.trim().ifBlank { null },
                                    formattedTime,
                                    selectedColorIdx,
                                    selectedFrequency,
                                    selectedWeekDays.joinToString(","),
                                    selectedImportance
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("edit_task_dialog_confirm")
                    ) {
                        Text(
                            text = getLabel("Save", "सहेजें", "जतन करा"),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Time picker dialog handled internally by TimePickerSheet
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(Locale.getDefault()),
        style = AppTextStyles.bodyMedium.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        ),
        color = AppColors.textHint,
        modifier = Modifier.padding(top = 10.dp)
    )
}

// Material3TimePickerDialog moved to TimePickerSheet.kt
