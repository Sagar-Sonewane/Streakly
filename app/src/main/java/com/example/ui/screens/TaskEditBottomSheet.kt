package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.core.theme.LocalAccentColor
import com.example.data.models.TaskModel
import com.example.core.utils.SoundService
import com.example.core.utils.HapticService
import com.example.core.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun TaskEditBottomSheet(
    task: TaskModel? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String?,
        timeLabel: String?,
        colorIdx: Int,
        freq: String,
        weekdays: String,
        importance: String,
        emoji: String,
        reminderHour: Int?,
        reminderMinute: Int?,
        reminderEnabled: Boolean,
        difficulty: String
    ) -> Unit
) {
    val accentColor = LocalAccentColor.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Step 1 states
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var emoji by remember { mutableStateOf(task?.emoji ?: "🎯") }
    var showEmojiBottomSheet by remember { mutableStateOf(false) }
    val emojiScale = remember { Animatable(1f) }

    // Step 2 states
    var hasTimeSet by remember { mutableStateOf(task?.reminderHour != null) }
    var selectedHour by remember { mutableIntStateOf(task?.reminderHour ?: 8) }
    var selectedMinute by remember { mutableIntStateOf(task?.reminderMinute ?: 0) }
    var isAM by remember { mutableStateOf((task?.reminderHour ?: 8) < 12) }
    var reminderEnabled by remember { mutableStateOf(task?.reminderEnabled ?: (task?.reminderHour != null)) }
    var showClockInline by remember { mutableStateOf(false) }

    // Repeat options:
    var repeatOption by remember {
        mutableStateOf(
            when {
                task == null -> "daily"
                task.frequency == "daily" -> "daily"
                task.frequency == "once" -> "once"
                task.weekDaysRaw == "1,2,3,4,5" -> "weekdays"
                task.weekDaysRaw == "6,7" -> "weekends"
                else -> "custom"
            }
        )
    }
    val selectedCustomDays = remember {
        mutableStateListOf<Int>().apply {
            if (task?.frequency == "weekly") {
                addAll(task.weekDays)
            }
        }
    }

    // Step 3 states
    var selectedImportance by remember { mutableStateOf(task?.importance ?: "regular") } // regular (Easy), moderate (Medium), priority (High)
    var selectedColorIdx by remember { mutableIntStateOf(task?.colorIndex ?: 0) }
    var selectedDifficulty by remember { mutableStateOf(task?.difficulty ?: "Medium") } // Low, Medium, High

    // Bottom sheet controls
    var currentStep by remember { mutableIntStateOf(1) }
    var cameFromStep4 by remember { mutableStateOf(false) }

    // Emoji keywords map & search states
    var showEmojiSearchDialog by remember { mutableStateOf(false) }

    val presetEmojis = listOf("🏋️", "📚", "🧘", "💧", "🏃", "✍️", "🎯", "😴", "🎨", "💊", "🍎", "🧠")
    val emojiSuggestions = mapOf(
        "🏋️" to "Gym",
        "📚" to "Reading",
        "🧘" to "Meditation",
        "💧" to "Drink Water",
        "🏃" to "Running",
        "✍️" to "Writing",
        "🎯" to "Goal Tracking",
        "😴" to "Sleep Early",
        "🎨" to "Create Art",
        "💊" to "Take Meds",
        "🍎" to "Eat Fruit",
        "🧠" to "Study"
    )

    val onEmojiSelectedWithAnimation = { chosenEmoji: String ->
        emoji = chosenEmoji
        SoundService.playTap()
        HapticService.selectionClick()
        if (title.isBlank()) {
            val suggestion = emojiSuggestions[chosenEmoji]
            if (suggestion != null) {
                scope.launch {
                    title = ""
                    for (i in 1..suggestion.length) {
                        title = suggestion.substring(0, i)
                        SoundService.playTap()
                        delay(30)
                    }
                }
            }
        }
        scope.launch {
            emojiScale.snapTo(0.8f)
            emojiScale.animateTo(
                1.1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
            emojiScale.animateTo(
                1.0f,
                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
    }

    val onNextClick = {
        SoundService.playTap()
        HapticService.selectionClick()
        if (cameFromStep4) {
            currentStep = 4
            cameFromStep4 = false
        } else {
            currentStep = (currentStep + 1).coerceAtMost(4)
        }
    }

    val onBackClick = {
        SoundService.playTap()
        HapticService.selectionClick()
        if (cameFromStep4) {
            currentStep = 4
            cameFromStep4 = false
        } else {
            currentStep = (currentStep - 1).coerceAtLeast(1)
        }
    }

    val heightFraction = if (currentStep == 2) 0.85f else 0.60f
    val animatedFraction by animateFloatAsState(
        targetValue = heightFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = AppColors.bgSecondary,
        dragHandle = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BottomSheetDefaults.DragHandle()
                if (task != null) {
                    Text(
                        text = "Editing: ${task.title}",
                        style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold, color = AppColors.textHint),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(animatedFraction)
                .navigationBarsPadding()
        ) {
            // Segmented Progress Bar
            SegmentedProgressBar(currentStep = currentStep, accentColor = accentColor)

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut()
                        } else {
                            slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut()
                        }
                    }
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (step) {
                            1 -> Step1Identity(
                                title = title,
                                onTitleChange = { title = it },
                                description = description,
                                onDescriptionChange = { description = it },
                                selectedEmoji = emoji,
                                emojiScale = emojiScale.value,
                                onEmojiButtonTap = { showEmojiBottomSheet = true }
                            )
                            2 -> Step2Schedule(
                                hasTimeSet = hasTimeSet,
                                onHasTimeSetChange = {
                                    hasTimeSet = it
                                    if (it) {
                                        reminderEnabled = true
                                    } else {
                                        reminderEnabled = false
                                        showClockInline = false
                                    }
                                },
                                hour = selectedHour,
                                minute = selectedMinute,
                                isAM = isAM,
                                onTimeChange = { h, m, am ->
                                    selectedHour = h
                                    selectedMinute = m
                                    isAM = am
                                },
                                showClockInline = showClockInline,
                                onShowClockInlineChange = { showClockInline = it },
                                reminderEnabled = reminderEnabled,
                                onReminderEnabledChange = { reminderEnabled = it },
                                repeatOption = repeatOption,
                                onRepeatOptionChange = { repeatOption = it },
                                selectedCustomDays = selectedCustomDays,
                                accentColor = accentColor
                            )
                            3 -> Step3PriorityAndStyle(
                                selectedImportance = selectedImportance,
                                onImportanceChange = { selectedImportance = it },
                                accentColor = accentColor
                            )
                            4 -> Step4Confirmation(
                                title = title,
                                description = description,
                                emoji = emoji,
                                hasTimeSet = hasTimeSet,
                                hour = selectedHour,
                                minute = selectedMinute,
                                isAM = isAM,
                                reminderEnabled = reminderEnabled,
                                repeatOption = repeatOption,
                                selectedCustomDays = selectedCustomDays,
                                selectedImportance = selectedImportance,
                                selectedDifficulty = selectedDifficulty,
                                selectedColorIdx = selectedColorIdx,
                                onJumpToStep = { stepTarget ->
                                    cameFromStep4 = true
                                    currentStep = stepTarget
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textSecondary),
                        border = borderStroke(accentColor = accentColor)
                    ) {
                        Text(text = "← Back", style = AppTextStyles.actionButton.copy(fontSize = 15.sp))
                    }
                }

                val nextButtonEnabled = currentStep != 1 || title.trim().isNotEmpty()
                val nextLabel = if (currentStep == 4) {
                    if (task == null) "Add Habit 🔥" else "Save Changes ✅"
                } else {
                    if (currentStep == 2 && !hasTimeSet && repeatOption == "daily") "Skip" else "Next →"
                }

                Button(
                    onClick = {
                        if (currentStep == 4) {
                            if (task == null) {
                                SoundService.playAdd()
                            } else {
                                SoundService.playSuccess()
                            }
                            HapticService.confirm()

                            // Formulate frequency & weekdays
                            val frequency = when (repeatOption) {
                                "daily" -> "daily"
                                "once" -> "once"
                                else -> "weekly"
                            }
                            val weekDaysRaw = when (repeatOption) {
                                "weekdays" -> "1,2,3,4,5"
                                "weekends" -> "6,7"
                                "custom" -> selectedCustomDays.sorted().joinToString(",")
                                else -> ""
                            }

                            val timeLabel = if (hasTimeSet) {
                                val h = selectedHour.toString().padStart(2, '0')
                                val m = selectedMinute.toString().padStart(2, '0')
                                val p = if (isAM) "AM" else "PM"
                                "$h:$m $p"
                            } else null

                            val reminderHourVal = if (hasTimeSet) {
                                val hr24 = if (isAM) {
                                    if (selectedHour == 12) 0 else selectedHour
                                } else {
                                    if (selectedHour == 12) 12 else selectedHour + 12
                                }
                                hr24
                            } else null

                            val reminderMinuteVal = if (hasTimeSet) selectedMinute else null

                            scope.launch {
                                try {
                                    sheetState.hide()
                                } catch (e: Exception) {
                                    // Ignore
                                }
                                onConfirm(
                                    title.trim(),
                                    description.trim().ifEmpty { null },
                                    timeLabel,
                                    selectedColorIdx,
                                    frequency,
                                    weekDaysRaw,
                                    selectedImportance,
                                    emoji,
                                    reminderHourVal,
                                    reminderMinuteVal,
                                    hasTimeSet && reminderEnabled,
                                    selectedDifficulty
                                )
                            }
                        } else {
                            onNextClick()
                        }
                    },
                    enabled = nextButtonEnabled,
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.Black,
                        disabledContainerColor = AppColors.border,
                        disabledContentColor = AppColors.textHint
                    )
                ) {
                    Text(
                        text = nextLabel,
                        style = AppTextStyles.actionButton.copy(
                            color = if (nextButtonEnabled) Color.Black else AppColors.textHint,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }

    if (showEmojiSearchDialog) {
        EmojiSearchDialog(
            onDismiss = { showEmojiSearchDialog = false },
            onEmojiSelected = { chosen ->
                onEmojiSelectedWithAnimation(chosen)
                showEmojiSearchDialog = false
            },
            accentColor = accentColor
        )
    }

    if (showEmojiBottomSheet) {
        val searchEmojis = listOf(
            "🏋️", "📚", "🧘", "💧", "🏃", "✍️", "🎯", "😴", "🎨", "💊", "🍎", "🧠",
            "🚴", "🏊", "🚿", "🧹", "🪴", "🐶", "🍳", "🍵", "💵", "📈", "💻", "🎸",
            "🗣️", "🤝", "❤️", "⏰", "📅", "🧼", "🍏", "🍌", "🥗", "🚶", "⚽", "🏀", 
            "🎭", "🎮", "✉️", "🔑", "🍿", "🚗", "✈️", "☀️", "🌙", "🔥", "🌈", "🎈", 
            "🎁", "🧩", "🧸", "🎉", "👑", "💎"
        )
        ModalBottomSheet(
            onDismissRequest = { showEmojiBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = AppColors.bgSecondary,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Choose Emoji 🎯",
                    style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Bold, color = accentColor),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                val itemsPerRow = 5
                val rows = searchEmojis.chunked(itemsPerRow)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rows.forEach { rowEmojis ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowEmojis.forEach { singleEmoji ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppColors.bgTertiary)
                                        .clickable {
                                            showEmojiBottomSheet = false
                                            onEmojiSelectedWithAnimation(singleEmoji)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = singleEmoji, fontSize = 24.sp)
                                }
                            }
                            
                            if (rowEmojis.size < itemsPerRow) {
                                val slotsLeft = itemsPerRow - rowEmojis.size
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppColors.bgTertiary)
                                        .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
                                        .clickable {
                                            showEmojiBottomSheet = false
                                            showEmojiSearchDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "More Emojis",
                                        tint = AppColors.textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                if (slotsLeft > 1) {
                                    repeat(slotsLeft - 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    
                    if (searchEmojis.size % itemsPerRow == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.bgTertiary)
                                    .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
                                    .clickable {
                                        showEmojiBottomSheet = false
                                        showEmojiSearchDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "More Emojis",
                                    tint = AppColors.textSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            repeat(itemsPerRow - 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun borderStroke(accentColor: Color) = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border)

@Composable
fun SegmentedProgressBar(currentStep: Int, accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (step in 1..4) {
                val isActive = step == currentStep
                val isCompleted = step < currentStep
                val color = if (isActive || isCompleted) accentColor else AppColors.bgSecondary
                val alpha = if (isActive) pulseAlpha else 1.0f

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .graphicsLayer(alpha = alpha)
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "Step $currentStep of 4",
            color = AppColors.textHint,
            style = AppTextStyles.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun Step1Identity(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedEmoji: String,
    emojiScale: Float,
    onEmojiButtonTap: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "What's the habit? 🎯",
            style = AppTextStyles.headingMedium.copy(fontWeight = FontWeight.Black)
        )
        Text(
            text = "Give it a name and personality",
            style = AppTextStyles.bodyMedium,
            color = AppColors.textSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer(scaleX = emojiScale, scaleY = emojiScale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.bgSecondary)
                    .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
                    .clickable { onEmojiButtonTap() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = selectedEmoji, fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        if (it.length <= 30) {
                            onTitleChange(it)
                        }
                    },
                    placeholder = { Text("e.g. Morning Run", color = AppColors.textHint) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LocalAccentColor.current,
                        unfocusedBorderColor = AppColors.border,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "${title.length} / 30",
                style = AppTextStyles.caption.copy(fontSize = 11.sp),
                color = if (title.length >= 27) LocalAccentColor.current else AppColors.textHint
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = { onDescriptionChange(it) },
            placeholder = { Text("Add a note... (optional)", color = AppColors.textHint) },
            maxLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalAccentColor.current,
                unfocusedBorderColor = AppColors.border,
                focusedTextColor = AppColors.textPrimary,
                unfocusedTextColor = AppColors.textPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun Step2Schedule(
    hasTimeSet: Boolean,
    onHasTimeSetChange: (Boolean) -> Unit,
    hour: Int,
    minute: Int,
    isAM: Boolean,
    onTimeChange: (hour: Int, minute: Int, isAM: Boolean) -> Unit,
    showClockInline: Boolean,
    onShowClockInlineChange: (Boolean) -> Unit,
    reminderEnabled: Boolean,
    onReminderEnabledChange: (Boolean) -> Unit,
    repeatOption: String,
    onRepeatOptionChange: (String) -> Unit,
    selectedCustomDays: MutableList<Int>,
    accentColor: Color
) {
    val getFormattedTime = {
        val h = hour.toString().padStart(2, '0')
        val m = minute.toString().padStart(2, '0')
        val amPm = if (isAM) "AM" else "PM"
        "$h : $m $amPm"
    }

    val presets = listOf(
        Triple("🌅 6AM", 6, true),
        Triple("🌄 7AM", 7, true),
        Triple("☀️ 8AM", 8, true),
        Triple("🌞 12PM", 12, false),
        Triple("🌆 6PM", 6, false),
        Triple("🌙 9PM", 9, false)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "When & how often? ⏰",
            style = AppTextStyles.headingMedium.copy(fontWeight = FontWeight.Black)
        )
        Text(
            text = "Set your schedule and reminders",
            style = AppTextStyles.bodyMedium,
            color = AppColors.textSecondary
        )

        // Time Selection Card
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bgTertiary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tappable Card Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!hasTimeSet) {
                                onHasTimeSetChange(true)
                            }
                            onShowClockInlineChange(!showClockInline)
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time Alert",
                        style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (hasTimeSet) getFormattedTime() else "No time set — tap to add",
                        color = if (hasTimeSet) accentColor else AppColors.textHint,
                        style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Inline Wheel Clock & presets
                AnimatedVisibility(
                    visible = hasTimeSet && showClockInline,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(color = AppColors.border.copy(alpha = 0.5f))

                        // Quick Presets Row (more compact padding)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presets.forEach { (lbl, hr, am) ->
                                val isChosen = hour == hr && minute == 0 && isAM == am
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isChosen) accentColor else AppColors.bgSecondary)
                                        .clickable {
                                            SoundService.playTap()
                                            HapticService.selectionClick()
                                            onTimeChange(hr, 0, am)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = lbl,
                                        color = if (isChosen) Color.Black else AppColors.textSecondary,
                                        style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Large interactive WheelPicker selectors layout
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Highlight band
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .background(AppColors.bgPrimary, RoundedCornerShape(8.dp))
                            )

                            // Top divider line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(accentColor.copy(alpha = 0.3f))
                                    .align(Alignment.Center)
                                    .offset(y = (-26).dp)
                            )

                            // Bottom divider line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(accentColor.copy(alpha = 0.3f))
                                    .align(Alignment.Center)
                                    .offset(y = 26.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val hours = (1..12).map { it.toString().padStart(2, '0') }
                                val minutes = (0..59).map { it.toString().padStart(2, '0') }
                                val phases = listOf("AM", "PM")

                                WheelPicker(
                                    items = hours,
                                    value = hour.toString().padStart(2, '0'),
                                    onValueChange = { selectedStr ->
                                        onTimeChange(selectedStr.toInt(), minute, isAM)
                                    },
                                    accentColor = accentColor
                                )

                                Text(
                                    text = ":",
                                    style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = AppColors.textSecondary
                                )

                                WheelPicker(
                                    items = minutes,
                                    value = minute.toString().padStart(2, '0'),
                                    onValueChange = { selectedStr ->
                                        onTimeChange(hour, selectedStr.toInt(), isAM)
                                    },
                                    accentColor = accentColor
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                WheelPicker(
                                    items = phases,
                                    value = if (isAM) "AM" else "PM",
                                    onValueChange = { selectedStr ->
                                        onTimeChange(hour, minute, selectedStr == "AM")
                                    },
                                    accentColor = accentColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Notification Switch Row
        if (hasTimeSet) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.bgTertiary)
                    .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Remind me at this time",
                        style = AppTextStyles.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = {
                        SoundService.playToggle()
                        HapticService.selectionClick()
                        onReminderEnabledChange(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = accentColor,
                        uncheckedThumbColor = AppColors.textSecondary,
                        uncheckedTrackColor = AppColors.border
                    )
                )
            }
        }

        // Repeat Options Wrapped in FlowRow (Fixes cutoff)
        Text(
            text = "Repeat Schedule",
            style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val repeatOptions = listOf(
                Pair("Daily", "daily"),
                Pair("Weekdays", "weekdays"),
                Pair("Weekends", "weekends")
            )
            val individualDays = listOf(
                Pair("Mon", 1), Pair("Tue", 2), Pair("Wed", 3),
                Pair("Thu", 4), Pair("Fri", 5), Pair("Sat", 6), Pair("Sun", 7)
            )

            repeatOptions.forEach { (lbl, opt) ->
                val isSelected = repeatOption == opt
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) accentColor else AppColors.bgSecondary)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) accentColor else AppColors.border,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            SoundService.playToggle()
                            HapticService.selectionClick()
                            onRepeatOptionChange(opt)
                            selectedCustomDays.clear()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = lbl,
                        color = if (isSelected) Color.Black else AppColors.textSecondary,
                        style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            individualDays.forEach { (lbl, day) ->
                val isSelected = repeatOption == "custom" && selectedCustomDays.contains(day)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) accentColor else AppColors.bgSecondary)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) accentColor else AppColors.border,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            SoundService.playToggle()
                            HapticService.selectionClick()
                            onRepeatOptionChange("custom")
                            if (selectedCustomDays.contains(day)) {
                                selectedCustomDays.remove(day)
                                if (selectedCustomDays.isEmpty()) {
                                    onRepeatOptionChange("daily")
                                }
                            } else {
                                selectedCustomDays.add(day)
                                if (selectedCustomDays.size == 7) {
                                    selectedCustomDays.clear()
                                    onRepeatOptionChange("daily")
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = lbl,
                        color = if (isSelected) Color.Black else AppColors.textSecondary,
                        style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

data class PriorityOption(
    val value: String,
    val title: String,
    val emoji: String,
    val desc: String,
    val color: Color
)

@Composable
fun Step3PriorityAndStyle(
    selectedImportance: String,
    onImportanceChange: (String) -> Unit,
    accentColor: Color
) {
    val getLabel = { en: String, hi: String, mr: String ->
        // Return english labels as standard, or resolve based on app state
        en
    }

    val categories = remember(accentColor) {
        listOf(
            PriorityOption("regular", "Easy", "🟢", "Low effort, build the habit", Color(0xFF4CAF50)),
            PriorityOption("moderate", "Medium", "🟡", "Focused and consistent", Color(0xFFFFC107)),
            PriorityOption("priority", "High", "🔴", "Critical — top priority", accentColor)
        )
    }

    val motivationalText = when (selectedImportance) {
        "regular" -> "Every habit starts small 🌱"
        "moderate" -> "Consistency is the key 🔑"
        else -> "This one matters most 🔥"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "How important is this? 🔥",
            style = AppTextStyles.headingMedium.copy(fontWeight = FontWeight.Black)
        )
        Text(
            text = "Select the priority of this habit",
            style = AppTextStyles.bodyMedium,
            color = AppColors.textSecondary
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.forEach { option ->
                val isSelected = selectedImportance == option.value
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.02f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )

                val cardBg = if (isSelected) option.color.copy(alpha = 0.15f) else AppColors.bgSecondary
                val borderCol = if (isSelected) option.color else AppColors.border

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = borderCol,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            SoundService.playTap()
                            HapticService.selectionClick()
                            onImportanceChange(option.value)
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = option.emoji, fontSize = 32.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = option.title,
                            style = AppTextStyles.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = AppColors.textPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = option.desc,
                            style = AppTextStyles.bodySmall.copy(
                                color = AppColors.textSecondary
                            )
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(option.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (option.color == accentColor) Color.Black else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = motivationalText,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                }
            ) { targetText ->
                Text(
                    text = targetText,
                    style = AppTextStyles.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = accentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun Step4Confirmation(
    title: String,
    description: String,
    emoji: String,
    hasTimeSet: Boolean,
    hour: Int,
    minute: Int,
    isAM: Boolean,
    reminderEnabled: Boolean,
    repeatOption: String,
    selectedCustomDays: List<Int>,
    selectedImportance: String,
    selectedDifficulty: String,
    selectedColorIdx: Int,
    onJumpToStep: (Int) -> Unit
) {
    val accentColor = LocalAccentColor.current
    val optionColors = AppColors.taskCategoryColors

    val priorityColor = when (selectedImportance) {
        "regular" -> Color(0xFF4CAF50)
        "moderate" -> Color(0xFFFFC107)
        else -> accentColor
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Looking good! 👀",
            style = AppTextStyles.headingMedium.copy(fontWeight = FontWeight.Black)
        )
        Text(
            text = "Review your habit before saving",
            style = AppTextStyles.bodyMedium,
            color = AppColors.textSecondary
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bgTertiary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border, RoundedCornerShape(20.dp))
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Colored left border representing priority
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(priorityColor)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Identity row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(optionColors.getOrNull(selectedColorIdx)?.copy(alpha = 0.15f) ?: AppColors.bgSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 28.sp)
                        }
                        Column {
                            Text(
                                text = title,
                                style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Black)
                            )
                            if (!description.isNullOrBlank()) {
                                Text(
                                    text = description,
                                    style = AppTextStyles.bodySmall,
                                    color = AppColors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = AppColors.border.copy(alpha = 0.4f), thickness = 1.dp)

                    // Schedule row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        val repeatText = when (repeatOption) {
                            "daily" -> "Daily"
                            "once" -> "Once"
                            "weekdays" -> "Weekdays"
                            "weekends" -> "Weekends"
                            else -> "Custom: " + selectedCustomDays.map {
                                when (it) {
                                    1 -> "Mon"
                                    2 -> "Tue"
                                    3 -> "Wed"
                                    4 -> "Thu"
                                    5 -> "Fri"
                                    6 -> "Sat"
                                    else -> "Sun"
                                }
                            }.joinToString(", ")
                        }
                        Text(
                            text = "Schedule: $repeatText",
                            style = AppTextStyles.bodyMedium,
                            color = AppColors.textPrimary
                        )
                    }

                    if (hasTimeSet) {
                        HorizontalDivider(color = AppColors.border.copy(alpha = 0.4f), thickness = 1.dp)

                        // Reminder row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            val amPmStr = if (isAM) "AM" else "PM"
                            val timeStr = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPmStr"
                            Text(
                                text = "Alert at $timeStr" + if (reminderEnabled) " (Bell notification on 🔔)" else " (No Notification)",
                                style = AppTextStyles.bodyMedium,
                                color = AppColors.textPrimary
                            )
                        }
                    }

                    HorizontalDivider(color = AppColors.border.copy(alpha = 0.4f), thickness = 1.dp)

                    // Priority & Difficulty row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Priority chip
                        val prioLabel = when (selectedImportance) {
                            "regular" -> "🟢 Easy"
                            "moderate" -> "🟡 Medium"
                            else -> "🔴 High"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.bgSecondary)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Priority: $prioLabel",
                                style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Difficulty chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.bgSecondary)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Effort: $selectedDifficulty",
                                style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Color swatch
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(priorityColor)
                        )
                    }
                }
            }
        }

        // Edit steps shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit Identity",
                color = accentColor,
                style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .clickable { onJumpToStep(1) }
                    .padding(vertical = 4.dp)
            )

            Text(
                text = "Edit Schedule",
                color = accentColor,
                style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .clickable { onJumpToStep(2) }
                    .padding(vertical = 4.dp)
            )

            Text(
                text = "Edit Priority & Style",
                color = accentColor,
                style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .clickable { onJumpToStep(3) }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun EmojiSearchDialog(
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit,
    accentColor: Color
) {
    var query by remember { mutableStateOf("") }
    val searchEmojis = listOf(
        "🏋️", "📚", "🧘", "💧", "🏃", "✍️", "🎯", "😴", "🎨", "💊", "🍎", "🧠",
        "🚴", "🏊", "🚿", "🧹", "🪴", "🐶", "🍳", "🍵", "💵", "📈", "💻", "🎸",
        "🗣️", "🤝", "❤️", "⏰", "📅", "🧼", "🍏", "🍌", "🥗", "🚶", "⚽", "🏀", 
        "🎭", "🎮", "✉️", "🔑", "🍿", "🚗", "✈️", "☀️", "🌙", "🔥", "🌈", "🎈", 
        "🎁", "🧩", "🧸", "🎉", "👑", "💎"
    )
    val emojiKeywords = mapOf(
        "🏋️" to "gym exercise workout lift train fitness",
        "📚" to "read book study learn study library",
        "🧘" to "meditate yoga peace mindfulness relax zen",
        "💧" to "water drink hydrate fluid glass",
        "🏃" to "run jog walk cardio sprint athlete",
        "✍️" to "write note journal essay poetry draft pen",
        "🎯" to "goal target focus hit target tracking aim",
        "😴" to "sleep bed rest night tired dream",
        "🎨" to "art paint draw craft creative sketch color",
        "💊" to "pill medicine vitamins health sick doctor",
        "🍎" to "apple fruit eat healthy food diet",
        "🧠" to "brain study mind smart memory logic learn",
        "🚴" to "cycle bike ride wheel speed travel",
        "🏊" to "swim pool water sea dive float",
        "🚿" to "shower bath clean wash routine",
        "🧹" to "sweep clean tidy house dust broom",
        "🪴" to "plant grow water green garden leaf pot",
        "🐶" to "dog pet animal walk feed puppy play",
        "🍳" to "cook food egg breakfast kitchen meal",
        "🍵" to "tea drink warm green mug",
        "💵" to "money cash save budget spend rich finance",
        "📈" to "chart grow success stats improve up",
        "💻" to "code work program type screen desk tech",
        "🎸" to "guitar music play song instrument chords audio",
        "🗣️" to "speak talk language practice communicate vocal",
        "🤝" to "meet help friend social partner deal shake",
        "❤️" to "love heart care partner family relationship",
        "⏰" to "alarm clock time morning wake alert",
        "📅" to "calendar date schedule plan year track",
        "🧼" to "soap wash hands clean hygiene bubble",
        "🍏" to "green apple fruit food health",
        "🍌" to "banana fruit snack potassium healthy",
        "🥗" to "salad eat veg diet health green organic",
        "🚶" to "walk step foot stroll movement outdoor",
        "⚽" to "soccer football sport play team outdoor kick",
        "🏀" to "basketball sport play team hoops court bounce",
        "🎭" to "drama theater movie act play mask show",
        "🎮" to "game play playstation xbox nintendo fun controller",
        "✉️" to "mail letter write contact post send inbox",
        "🔑" to "key lock open security home house access",
        "🍿" to "popcorn snack watch movie cinema theater",
        "🚗" to "drive car travel commute road trip wheel",
        "✈️" to "fly plane travel airport vacation sky trip",
        "☀️" to "sun light day morning summer warm weather",
        "🌙" to "moon night dark sleep stars sky",
        "🔥" to "fire hot streak burn spark match ignite",
        "🌈" to "rainbow color sky beauty weather hope happy",
        "🎈" to "balloon party celebrate fly air fun kids",
        "🎁" to "gift present birthday give wrap surprise",
        "🧩" to "puzzle solve logic game mind pieces brainstorm",
        "🧸" to "toy teddy bear kids soft play sleep cozy",
        "🎉" to "party celebrate celebrate confetti win cheer",
        "👑" to "king queen crown royal win success lead tier",
        "💎" to "diamond gem stone rich success luxury premium"
    )

    val filtered = remember(query) {
        if (query.isBlank()) searchEmojis
        else {
            searchEmojis.filter { emoji ->
                val keywords = emojiKeywords[emoji] ?: ""
                keywords.contains(query, ignoreCase = true) || emoji == query
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bgSecondary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Search Emojis 🔍",
                    style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search... (e.g. gym, clean)", color = AppColors.textHint) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = AppColors.border
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No emojis found", color = AppColors.textSecondary)
                        }
                    } else {
                        // Simple vertical grid using Columns/Rows
                        val itemsPerRow = 5
                        val rows = filtered.chunked(itemsPerRow)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rows.forEach { rowEmojis ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    rowEmojis.forEach { singleEmoji ->
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(AppColors.bgTertiary)
                                                .clickable { onEmojiSelected(singleEmoji) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = singleEmoji, fontSize = 22.sp)
                                        }
                                    }
                                    // Empty placeholders to align columns if last row is incomplete
                                    if (rowEmojis.size < itemsPerRow) {
                                        repeat(itemsPerRow - rowEmojis.size) {
                                            Spacer(modifier = Modifier.size(42.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "Close", color = AppColors.textSecondary)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    value: String,
    onValueChange: (String) -> Unit,
    accentColor: Color
) {
    val paddedItems = remember(items) {
        listOf("") + items + listOf("")
    }
    
    val initialIndex = items.indexOf(value).coerceAtLeast(0) + 1
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (initialIndex - 1).coerceAtLeast(0))
    val flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = listState)
    
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf initialIndex
            
            val center = layoutInfo.viewportEndOffset / 2
            var closestIndex = initialIndex
            var minDiff = Int.MAX_VALUE
            
            for (itemInfo in visibleItemsInfo) {
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                val diff = kotlin.math.abs(itemCenter - center)
                if (diff < minDiff) {
                    minDiff = diff
                    closestIndex = itemInfo.index
                }
            }
            closestIndex
        }
    }
    
    LaunchedEffect(centerIndex) {
        if (centerIndex in 1..(paddedItems.size - 2)) {
            val selectedValue = paddedItems[centerIndex]
            if (selectedValue.isNotEmpty() && selectedValue != value) {
                onValueChange(selectedValue)
            }
        }
    }
    
    LaunchedEffect(value) {
        val targetIndex = paddedItems.indexOf(value)
        if (targetIndex != -1 && targetIndex != centerIndex && !listState.isScrollInProgress) {
            listState.scrollToItem((targetIndex - 1).coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .width(55.dp)
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            itemsIndexed(paddedItems) { index, item ->
                val isSelected = index == centerIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = AppTextStyles.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (isSelected) 22.sp else 16.sp
                        ),
                        color = if (isSelected) accentColor else AppColors.textSecondary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
