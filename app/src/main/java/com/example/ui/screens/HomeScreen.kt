package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import com.example.core.utils.CategoryUtils
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
    var activeDetailTask by remember { mutableStateOf<TaskModel?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        language = language,
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
                            scope.launch {
                                snackbarHostState.showSnackbar("${task.title} deleted")
                            }
                        },
                        onDuplicate = {
                            com.example.core.utils.SoundService.playSuccess()
                            com.example.core.utils.HapticService.confirm()
                            taskProvider.duplicateTask(task)
                            scope.launch {
                                snackbarHostState.showSnackbar("${task.title} duplicated")
                            }
                        },
                        onArchive = {
                            com.example.core.utils.SoundService.playDelete()
                            com.example.core.utils.HapticService.doubleClick()
                            taskProvider.deleteTask(task.id)
                            scope.launch {
                                snackbarHostState.showSnackbar("Habit archived")
                            }
                        },
                        onClick = {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            activeDetailTask = task
                        }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog) {
        TaskEditBottomSheet(
            task = null,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, time, colorIdx, freq, weekdays, importance, emoji, reminderHour, reminderMinute, reminderEnabled, difficulty ->
                taskProvider.addTask(
                    title = title,
                    description = desc,
                    timeLabel = time,
                    colorIndex = colorIdx,
                    frequency = freq,
                    weekDaysRaw = weekdays,
                    importance = importance,
                    emoji = emoji,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    reminderEnabled = reminderEnabled,
                    difficulty = difficulty
                )
                showAddTaskDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("$title added")
                }
            }
        )
    }

    editingTask?.let { taskToEdit ->
        TaskEditBottomSheet(
            task = taskToEdit,
            onDismiss = { editingTask = null },
            onConfirm = { title, desc, time, colorIdx, freq, weekdays, importance, emoji, reminderHour, reminderMinute, reminderEnabled, difficulty ->
                taskProvider.updateTask(
                    taskToEdit.copy(
                        title = title,
                        description = desc,
                        timeLabel = time,
                        colorIndex = colorIdx,
                        frequency = freq,
                        weekDaysRaw = weekdays,
                        importance = importance,
                        emoji = emoji,
                        reminderHour = reminderHour,
                        reminderMinute = reminderMinute,
                        reminderEnabled = reminderEnabled,
                        difficulty = difficulty
                    )
                )
                editingTask = null
                scope.launch {
                    snackbarHostState.showSnackbar("$title updated")
                }
            }
        )
    }

    activeDetailTask?.let { task ->
        TaskDetailsBottomSheet(
            task = task,
            onDismiss = { activeDetailTask = null },
            onToggleCompletion = {
                taskProvider.toggleTaskCompletion(task)
                if (!task.isCompleted) {
                    com.example.core.utils.SoundService.playSuccess()
                    com.example.core.utils.HapticService.confirm()
                } else {
                    com.example.core.utils.SoundService.playDelete()
                    com.example.core.utils.HapticService.doubleClick()
                }
                activeDetailTask = task.copy(isCompleted = !task.isCompleted)
            },
            onEdit = {
                com.example.core.utils.SoundService.playTap()
                com.example.core.utils.HapticService.selectionClick()
                editingTask = task
                activeDetailTask = null
            },
            onDelete = {
                com.example.core.utils.SoundService.playDelete()
                com.example.core.utils.HapticService.doubleClick()
                taskProvider.deleteTask(task.id)
                activeDetailTask = null
                scope.launch {
                    snackbarHostState.showSnackbar("${task.title} deleted")
                }
            },
            onDuplicate = {
                com.example.core.utils.SoundService.playSuccess()
                com.example.core.utils.HapticService.confirm()
                taskProvider.duplicateTask(task)
                activeDetailTask = null
                scope.launch {
                    snackbarHostState.showSnackbar("${task.title} duplicated")
                }
            },
            onArchive = {
                com.example.core.utils.SoundService.playDelete()
                com.example.core.utils.HapticService.doubleClick()
                taskProvider.deleteTask(task.id)
                activeDetailTask = null
                scope.launch {
                    snackbarHostState.showSnackbar("Habit archived")
                }
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

                Spacer(modifier = Modifier.height(8.dp))

                var scaleTarget by remember { mutableStateOf(1.0f) }
                val scale by animateFloatAsState(
                    targetValue = scaleTarget,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "TrophyPulse"
                )

                LaunchedEffect(currentStreak, longestStreak) {
                    if (currentStreak > 0 && currentStreak >= longestStreak) {
                        scaleTarget = 1.08f
                        kotlinx.coroutines.delay(150)
                        scaleTarget = 1.0f
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.Black.copy(alpha = 0.20f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = "Trophy icon",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bestLabel,
                        style = AppTextStyles.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItemRow(
    task: TaskModel,
    isLocked: Boolean = false,
    language: String,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onArchive: () -> Unit,
    onClick: () -> Unit
) {
    val isDark = AppColors.isDark
    val themeAccent = AppColors.accentColor
    val taskColor = when (task.importance) {
        "regular" -> Color(0xFF4CAF50)
        "moderate" -> Color(0xFFFFC107)
        else -> themeAccent
    }

    val taskProvider = Providers.getTask()
    var streakCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(task.id, task.isCompleted) {
        val list = taskProvider.getCompletionsForTaskList(task.id)
        streakCount = calculateTaskStreak(list, task).first
    }

    val getLabel = { en: String, hi: String, mr: String ->
        when (language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }

    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                taskColor.copy(alpha = 0.04f)
            } else {
                taskColor.copy(alpha = 0.08f)
            }
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("task_item_card_${task.id}")
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (!isLocked) {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        showMenu = true
                    }
                }
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card left border: 6.dp in priority color
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(taskColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .graphicsLayer {
                        // Soft fade when completed
                        alpha = if (task.isCompleted) 0.65f else 1.0f
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: completion circle checkbox
                val animScale by animateFloatAsState(
                    targetValue = if (task.isCompleted) 1.1f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            scaleX = animScale
                            scaleY = animScale
                            alpha = if (isLocked) 0.5f else 1f
                        }
                        .clip(CircleShape)
                        .background(if (task.isCompleted) themeAccent.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (task.isCompleted) themeAccent else taskColor,
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
                            tint = themeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Center-left: task category rounded icon mapping instead of emoji text
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(taskColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryUtils.getIconForEmoji(task.emoji),
                        contentDescription = "Habit Icon",
                        tint = taskColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Center: task name and details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = task.title,
                        style = AppTextStyles.titleMedium.copy(
                            textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                            color = if (task.isCompleted) AppColors.textSecondary else AppColors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val freqLabel = when (task.frequency) {
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
                    val catLabel = CategoryUtils.getCategoryLabel(task.emoji, language)

                    Text(
                        text = "$freqLabel • $catLabel",
                        style = AppTextStyles.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = AppColors.textSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!task.timeLabel.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = "Time",
                                    tint = AppColors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = task.timeLabel,
                                    style = AppTextStyles.caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = AppColors.textSecondary
                                )
                            }
                        }

                        val badgeText = when (task.importance) {
                            "regular" -> getLabel("Easy", "आसान", "सोपे")
                            "moderate" -> getLabel("Mid", "मध्यम", "मध्यम")
                            else -> getLabel("High", "उच्च", "उच्च")
                        }
                        Text(
                            text = "Priority: $badgeText",
                            style = AppTextStyles.caption.copy(fontWeight = FontWeight.SemiBold),
                            color = taskColor
                        )
                    }

                    if (streakCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$streakCount " + getLabel("Day Streak", "दिन की निरंतरता", "दिवस सातत्य"),
                                style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFF5722)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right side: Options dropdown menu
                Box {
                    if (!isLocked) {
                        IconButton(
                            onClick = {
                                com.example.core.utils.SoundService.playTap()
                                com.example.core.utils.HapticService.selectionClick()
                                showMenu = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = AppColors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(AppColors.bgSecondary)
                        ) {
                            DropdownMenuItem(
                                text = { Text(getLabel("Edit Task", "संपादित करें", "संपादन करा"), color = AppColors.textPrimary) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AppColors.textSecondary) }
                            )
                            DropdownMenuItem(
                                text = { Text(getLabel("Duplicate", "डुप्लिकेट करें", "नक्कल करा"), color = AppColors.textPrimary) },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = AppColors.textSecondary) }
                            )
                            DropdownMenuItem(
                                text = { Text(getLabel("Archive", "आर्काइव करें", "संग्रह करा"), color = AppColors.textPrimary) },
                                onClick = {
                                    showMenu = false
                                    onArchive()
                                },
                                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = "Archive", tint = AppColors.textSecondary) }
                            )
                            DropdownMenuItem(
                                text = { Text(getLabel("Delete", "हटाएं", "हटवा"), color = AppColors.danger) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppColors.danger) }
                            )
                        }
                    }
                }
            }
        }
    }
}


// Material3TimePickerDialog moved to TimePickerSheet.kt
