package com.example.shared.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerSheet(
    selectedHour: Int,
    selectedMinute: Int,
    isAM: Boolean,
    onTimeChanged: (hour: Int, minute: Int, isAM: Boolean) -> Unit,
    accentColor: Color,
    getLabel: (String, String, String) -> String,
    showQuickPick: Boolean = true
) {
    if (showQuickPick) {
        var showNativeTimePicker by remember { mutableStateOf(false) }

        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bgTertiary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.8.dp, AppColors.border, RoundedCornerShape(14.dp)),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = getLabel("Quick Pick", "त्वरित चयन", "द्रुत निवड"),
                    style = AppTextStyles.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                // Quick time chips (Grid 2 rows of 3 columns)
                val quickTimes = listOf(
                    Triple("🌅 6 AM", 6, true),
                    Triple("🌄 7 AM", 7, true),
                    Triple("🌞 9 AM", 9, true),
                    Triple("☀️ 12 PM", 12, false),
                    Triple("🌆 6 PM", 6, false),
                    Triple("🌙 9 PM", 9, false)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickTimes.take(3).forEach { (label, hr, am) ->
                            val isChosen = selectedHour == hr && selectedMinute == 0 && isAM == am
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isChosen) accentColor.copy(alpha = 0.15f) else AppColors.bgPrimary)
                                    .border(
                                        width = if (isChosen) 1.2.dp else 0.8.dp,
                                        color = if (isChosen) accentColor else AppColors.border,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        com.example.core.utils.SoundService.playTap()
                                        com.example.core.utils.HapticService.selectionClick()
                                        onTimeChanged(hr, 0, am)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = AppTextStyles.caption.copy(
                                        fontSize = 12.sp,
                                        fontWeight = if (isChosen) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isChosen) accentColor else AppColors.textSecondary
                                    )
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickTimes.takeLast(3).forEach { (label, hr, am) ->
                            val isChosen = selectedHour == hr && selectedMinute == 0 && isAM == am
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isChosen) accentColor.copy(alpha = 0.15f) else AppColors.bgPrimary)
                                    .border(
                                        width = if (isChosen) 1.2.dp else 0.8.dp,
                                        color = if (isChosen) accentColor else AppColors.border,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        com.example.core.utils.SoundService.playTap()
                                        com.example.core.utils.HapticService.selectionClick()
                                        onTimeChanged(hr, 0, am)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = AppTextStyles.caption.copy(
                                        fontSize = 12.sp,
                                        fontWeight = if (isChosen) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isChosen) accentColor else AppColors.textSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = AppColors.border, thickness = 0.8.dp)

                Text(
                    text = getLabel("Custom Time", "कस्टम समय", "सानुकूल वेळ"),
                    style = AppTextStyles.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                Button(
                    onClick = {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        showNativeTimePicker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.bgPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.8.dp, AppColors.border, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getLabel("Select Custom Time", "कस्टम समय चुनें", "सानुकूल वेळ निवडा"),
                            color = AppColors.textPrimary,
                            style = AppTextStyles.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (showNativeTimePicker) {
            Material3TimePickerDialog(
                initialHour = selectedHour,
                initialMinute = selectedMinute,
                initialIsAM = isAM,
                accentColor = accentColor,
                getLabel = getLabel,
                onDismiss = { showNativeTimePicker = false },
                onConfirm = { hour, minute, am ->
                    onTimeChanged(hour, minute, am)
                    showNativeTimePicker = false
                }
            )
        }
    } else {
        val initialHour24 = remember(selectedHour, isAM) {
            if (isAM) {
                if (selectedHour == 12) 0 else selectedHour
            } else {
                if (selectedHour == 12) 12 else selectedHour + 12
            }
        }
        val state = rememberTimePickerState(
            initialHour = initialHour24,
            initialMinute = selectedMinute,
            is24Hour = false
        )

        var prevIsAM by remember { mutableStateOf(initialHour24 < 12) }
        LaunchedEffect(state.hour) {
            val currentIsAM = state.hour < 12
            if (currentIsAM != prevIsAM) {
                com.example.core.utils.SoundService.playToggle()
                com.example.core.utils.HapticService.selectionClick()
                prevIsAM = currentIsAM
            }
        }

        // Sync local TimePicker state changes back to parent
        LaunchedEffect(state.hour, state.minute) {
            val isAMResult = state.hour < 12
            val hour12Result = when {
                state.hour == 0 -> 12
                state.hour == 12 -> 12
                state.hour > 12 -> state.hour - 12
                else -> state.hour
            }
            onTimeChanged(hour12Result, state.minute, isAMResult)
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialSelectedContentColor = Color.Black,
                    clockDialUnselectedContentColor = AppColors.textPrimary,
                    selectorColor = accentColor,
                    periodSelectorSelectedContainerColor = accentColor.copy(alpha = 0.2f),
                    periodSelectorUnselectedContainerColor = AppColors.bgTertiary,
                    periodSelectorSelectedContentColor = accentColor,
                    periodSelectorUnselectedContentColor = AppColors.textSecondary,
                    timeSelectorSelectedContainerColor = accentColor.copy(alpha = 0.2f),
                    timeSelectorUnselectedContainerColor = AppColors.bgTertiary,
                    timeSelectorSelectedContentColor = accentColor,
                    timeSelectorUnselectedContentColor = AppColors.textPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    initialIsAM: Boolean,
    accentColor: Color,
    getLabel: (String, String, String) -> String,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Boolean) -> Unit
) {
    val initialHour24 = if (initialIsAM) {
        if (initialHour == 12) 0 else initialHour
    } else {
        if (initialHour == 12) 12 else initialHour + 12
    }
    val state = rememberTimePickerState(
        initialHour = initialHour24,
        initialMinute = initialMinute,
        is24Hour = false
    )

    var prevIsAM by remember { mutableStateOf(initialHour24 < 12) }
    LaunchedEffect(state.hour) {
        val currentIsAM = state.hour < 12
        if (currentIsAM != prevIsAM) {
            com.example.core.utils.SoundService.playToggle()
            com.example.core.utils.HapticService.selectionClick()
            prevIsAM = currentIsAM
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    com.example.core.utils.SoundService.playTap()
                    com.example.core.utils.HapticService.confirm()
                    val isAMResult = state.hour < 12
                    val hour12Result = when {
                        state.hour == 0 -> 12
                        state.hour == 12 -> 12
                        state.hour > 12 -> state.hour - 12
                        else -> state.hour
                    }
                    onConfirm(hour12Result, state.minute, isAMResult)
                }
            ) {
                Text(getLabel("Confirm", "पुष्टि करें", "निश्चित करा"), color = accentColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                com.example.core.utils.SoundService.playTap()
                com.example.core.utils.HapticService.selectionClick()
                onDismiss()
            }) {
                Text(getLabel("Cancel", "रद्द करें", "रद्द करा"), color = AppColors.textSecondary)
            }
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialSelectedContentColor = Color.Black,
                        clockDialUnselectedContentColor = AppColors.textPrimary,
                        selectorColor = accentColor,
                        periodSelectorSelectedContainerColor = accentColor.copy(alpha = 0.2f),
                        periodSelectorUnselectedContainerColor = AppColors.bgTertiary,
                        periodSelectorSelectedContentColor = accentColor,
                        periodSelectorUnselectedContentColor = AppColors.textSecondary,
                        timeSelectorSelectedContainerColor = accentColor.copy(alpha = 0.2f),
                        timeSelectorUnselectedContainerColor = AppColors.bgTertiary,
                        timeSelectorSelectedContentColor = accentColor,
                        timeSelectorUnselectedContentColor = AppColors.textPrimary
                    )
                )
            }
        },
        containerColor = AppColors.bgSecondary,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.border(1.dp, AppColors.border, RoundedCornerShape(28.dp))
    )
}
