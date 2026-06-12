package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.window.Dialog
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.core.utils.Responsive
import android.content.Context
import com.example.providers.Providers
import com.example.shared.widgets.NameInputDialog
import com.example.shared.widgets.TimePickerSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    userName: String = "",
    onUserNameChanged: (String) -> Unit = {}
) {
    val settingsProvider = Providers.getSettings()
    val streakProvider = Providers.getStreak()
    val taskProvider = Providers.getTask()
    val reflectionProvider = Providers.getReflection()

    val settingsState by settingsProvider.settingsState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            settingsProvider.updateNotificationsEnabled(false)
        }
    }

    val language = settingsState.language
    val accentColorIndex = settingsState.accentColorIndex
    val accentColor = AppColors.accentColorOptions[accentColorIndex]

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showResetDatabaseDialog by remember { mutableStateOf(false) }
    var showChangeNameDialog by remember { mutableStateOf(false) }
    
    // Interactive choice popup selectors
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val getLabel = { en: String, hi: String, mr: String ->
        when (language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }

    val activeThemeName = when (settingsState.themeModeIndex) {
        0 -> getLabel("System Default", "सिस्टम डिफ़ॉल्ट", "सिस्टम पर्याय")
        1 -> getLabel("Light Mode", "लाइट मोड", "प्रकाश मोड")
        2 -> getLabel("Dark Mode", "डार्क मोड", "गडद मोड")
        else -> getLabel("Dark Mode", "डार्क मोड", "गडद मोड")
    }

    val activeAccentName = AppColors.accentNames.getOrNull(accentColorIndex) ?: "Orange"
    val activeLangName = when (language) {
        "en" -> "English"
        "hi" -> "हिंदी"
        "mr" -> "मराठी"
        else -> "English"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.bgPrimary),
            contentPadding = PaddingValues(top = 16.dp, bottom = Responsive.h(12f)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Group 1: APPEARANCE STYLE
        item {
            SettingsGroupCard(
                title = getLabel("APPEARANCE STYLE", "सजावट शैली", "सजावट आकार"),
                accentColor = accentColor
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Person,
                        iconColor = accentColor,
                        title = getLabel("Change Name", "नाम बदलें", "नाव बदला"),
                        subtitle = userName.ifBlank { getLabel("Not set", "सेट नहीं", "सेट नाही") },
                        onClick = {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            showChangeNameDialog = true
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppColors.border))

                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        iconColor = accentColor,
                        title = getLabel("Theme Mode", "थीम का प्रकार", "थीम पर्याय"),
                        subtitle = activeThemeName,
                        onClick = {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            showThemeDialog = true
                        }
                    )
                    
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppColors.border))

                    SettingsRow(
                        icon = Icons.Default.Brush,
                        iconColor = accentColor,
                        title = getLabel("Accent Color", "मुख्य रंग", "मुख्य रंग"),
                        subtitle = activeAccentName,
                        onClick = {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            showAccentDialog = true
                        }
                    )
                }
            }
        }

        // Group 2: LOCALIZATION LANGUAGE
        item {
            SettingsGroupCard(
                title = getLabel("LOCALIZATION LANGUAGE", "भाषा विकल्प", "भाषा पर्याय"),
                accentColor = accentColor
            ) {
                SettingsRow(
                    icon = Icons.Default.Language,
                    iconColor = AppColors.blue,
                    title = getLabel("App Language", "भाषा बदलें", "भाषा बदला"),
                    subtitle = activeLangName,
                    onClick = {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        showLangDialog = true
                    }
                )
            }
        }

        // Group 3: AUDIOS & TACTILITY
        item {
            SettingsGroupCard(
                title = getLabel("AUDIOS & TACTILITY", "आवाज़ और कंपन", "ध्वनी आणि कंपने"),
                accentColor = accentColor
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        iconColor = AppColors.purple,
                        title = getLabel("Sound Effects", "सफलता की आवाज़ें", "यशाचे आवाज"),
                        subtitle = getLabel("Play sounds during completions", " कार्य पूरा करते समय आवाज़ खेलें", "कृती पूर्ण झाल्यावर आवाज येईल"),
                        onClick = {
                            com.example.core.utils.SoundService.playToggle()
                            com.example.core.utils.HapticService.selectionClick()
                            settingsProvider.updateSoundEnabled(!settingsState.soundEnabled)
                        },
                        trailingContent = {
                            Switch(
                                checked = settingsState.soundEnabled,
                                onCheckedChange = {
                                    com.example.core.utils.SoundService.playToggle()
                                    com.example.core.utils.HapticService.selectionClick()
                                    settingsProvider.updateSoundEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = accentColor,
                                    uncheckedThumbColor = AppColors.textSecondary,
                                    uncheckedTrackColor = AppColors.border
                                )
                            )
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppColors.border))

                    SettingsRow(
                        icon = Icons.Default.Fingerprint,
                        iconColor = AppColors.purple,
                        title = getLabel("Haptic Vibrations", "हैप्टिक स्पर्श", "हॅप्टिक कंपने"),
                        subtitle = getLabel("Vibrate on interactions", "स्पर्श / क्लिक पर हल्के कंपन", "बटण दाबल्यावर सौम्य कंपन जाणवेल"),
                        onClick = {
                            com.example.core.utils.SoundService.playToggle()
                            com.example.core.utils.HapticService.selectionClick()
                            settingsProvider.updateHapticEnabled(!settingsState.hapticEnabled)
                        },
                        trailingContent = {
                            Switch(
                                checked = settingsState.hapticEnabled,
                                onCheckedChange = {
                                    com.example.core.utils.SoundService.playToggle()
                                    com.example.core.utils.HapticService.selectionClick()
                                    settingsProvider.updateHapticEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = accentColor,
                                    uncheckedThumbColor = AppColors.textSecondary,
                                    uncheckedTrackColor = AppColors.border
                                )
                            )
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppColors.border))

                    SettingsRow(
                        icon = Icons.Default.AccessTime,
                        iconColor = AppColors.purple,
                        title = getLabel("Alert Time", "अनुस्मारक समय", "स्मरण वेळ"),
                        subtitle = getLabel("Tap to change daily notification time", "दैनिक सूचना समय बदलने के लिए टैप करें", "दररोजची सूचना वेळ बदलण्यासाठी दाबा"),
                        onClick = { showTimePickerDialog = true },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val formattedTime = String.format("%02d:%02d", settingsState.reminderHour, settingsState.reminderMinute)
                                Text(
                                    text = formattedTime,
                                    style = AppTextStyles.bodyMedium.copy(
                                        color = AppColors.textSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Go",
                                    tint = AppColors.textHint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                }
            }
        }

        // Group 4: DANGER ZONE
        item {
            SettingsGroupCard(
                title = getLabel("DANGER ZONE", "जोखिम क्षेत्र", "धोकादायक क्षेत्र"),
                accentColor = AppColors.red
            ) {
                SettingsRow(
                    icon = Icons.Default.Warning,
                    iconColor = AppColors.red,
                    title = getLabel("Clear Database", "डेटाबेस पूरी तरह साफ़ करें", "डेटाबेस पूर्णपणे पुसून टाका"),
                    subtitle = getLabel("Permanently wipe all streak progress logs", "सभी निरंतरता और आदतें हमेशा के लिए हटा दें", "सवयी आणि प्रगती कायमस्वरूपी पुसून टाका"),
                    onClick = { showResetDatabaseDialog = true }
                )
            }
        }

        // Group 5: About Card
        item {
            com.example.shared.widgets.StreaklyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                padding = 24.dp,
                borderRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak flame icon",
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "Ignited with ❤️ in India 🇮🇳",
                        style = AppTextStyles.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppColors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "by Sagar Sonewane",
                        style = AppTextStyles.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = AppColors.textPrimary
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Popup choices selectors logic


    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentThemeModeIndex = settingsState.themeModeIndex,
            onThemeModeSelected = { index ->
                val sharedPrefs = context.getSharedPreferences("streakly_prefs", Context.MODE_PRIVATE)
                when (index) {
                    1 -> sharedPrefs.edit().putString("theme_mode", "light").apply()
                    2 -> sharedPrefs.edit().putString("theme_mode", "dark").apply()
                    0 -> sharedPrefs.edit().remove("theme_mode").apply()
                }
                settingsProvider.updateThemeModeIndex(index)
            },
            accentColor = accentColor,
            getLabel = getLabel,
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showAccentDialog) {
        ColorSelectionDialog(
            selectedColorIndex = accentColorIndex,
            onColorSelected = { index ->
                val selectedOption = AppColors.accentOptions[index]
                val sharedPrefs = context.getSharedPreferences("streakly_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("accent_color", selectedOption.hex).apply()

                settingsProvider.updateAccentColor(index)

                val displayName = when (index) {
                    0 -> getLabel("Neon Cyan", "नियोन स्यान", "नियोन स्यान")
                    1 -> getLabel("Electric Blue", "इलेक्ट्रिक ब्लू", "इलेक्ट्रिक ब्लू")
                    2 -> getLabel("Purple Pulse", "पर्पल पल्स", "पर्पल पल्स")
                    3 -> getLabel("Emerald Green", "पन्ना हरा", "एमराल्ड हिरवा")
                    4 -> getLabel("Sunset Orange", "सूर्यास्त नारंगी", "सूर्यास्त नारिंगी")
                    5 -> getLabel("Rose Pink", "गुलाब गुलाबी", "गुलाब गुलाबी")
                    else -> selectedOption.name
                }

                scope.launch {
                    val message = when (language) {
                        "hi" -> "थीम अपडेट की गई! $displayName"
                        "mr" -> "थीम अपडेट केली! $displayName"
                        else -> "Theme updated! $displayName"
                    }
                    snackbarHostState.showSnackbar(message = message)
                }
            },
            accentColor = accentColor,
            getLabel = getLabel,
            onDismiss = { showAccentDialog = false }
        )
    }

    if (showLangDialog) {
        Dialog(onDismissRequest = { showLangDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.bgSecondary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.border, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = getLabel("Select Language", "भाषा चुनें", "भाषा निवडा"),
                        style = AppTextStyles.headingMedium.copy(color = accentColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    )

                    val languages = listOf(
                        Triple("en", "English", "English"),
                        Triple("hi", "हिंदी", "हिंदी"),
                        Triple("mr", "मराठी", "मराठी")
                    )

                    languages.forEach { (code, displayName, abbr) ->
                        val isSelected = code == language
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable {
                                    com.example.core.utils.SoundService.playTap()
                                    com.example.core.utils.HapticService.selectionClick()
                                    settingsProvider.updateLanguage(code)
                                    showLangDialog = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = displayName,
                                style = AppTextStyles.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) accentColor else AppColors.textPrimary
                                )
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTimePickerDialog) {
        TimeSelectionDialog(
            hour = settingsState.reminderHour,
            minute = settingsState.reminderMinute,
            accentColor = accentColor,
            getLabel = getLabel,
            onDismiss = { showTimePickerDialog = false },
            onSave = { h, m ->
                settingsProvider.updateReminderTime(h, m)
                
                // Saving updates the SharedPreferences value
                val sharedPrefs = context.getSharedPreferences("streakly_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putInt("reminder_hour", h).putInt("reminder_minute", m).apply()
                
                // Reschedule the notification immediately
                if (settingsState.notificationsEnabled) {
                    com.example.StreaklyApp.instance.notificationService.scheduleDailyReminder(h, m)
                }
                
                showTimePickerDialog = false
            }
        )
    }

    if (showChangeNameDialog) {
        NameInputDialog(
            initialName = userName,
            accentColor = accentColor,
            getLabel = getLabel,
            onDismiss = { showChangeNameDialog = false },
            onConfirm = { newName ->
                val trimmedName = newName.trim()
                val sharedPrefs = context.getSharedPreferences("streakly_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("user_name", trimmedName).apply()
                onUserNameChanged(trimmedName)
                showChangeNameDialog = false
            }
        )
    }

    if (showResetDatabaseDialog) {
        ResetConfirmDialog(
            accentColor = accentColor,
            getLabel = getLabel,
            onDismiss = { showResetDatabaseDialog = false },
            onConfirm = {
                scope.launch {
                    streakProvider.resetAllStreakData()
                    taskProvider.deleteAll()
                    reflectionProvider.deleteAll()
                    showResetDatabaseDialog = false
                }
            }
        )
    }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val isDark = AppColors.isDark
    val bgIcon = iconColor.copy(alpha = 0.12f)
    val textPri = AppColors.textPrimary
    val textSec = AppColors.textSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(bgIcon),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = AppTextStyles.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textPri
                )
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppTextStyles.bodySmall.copy(
                        fontSize = 11.sp,
                        color = textSec
                    )
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Go",
                tint = AppColors.textHint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsGroupCard(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    com.example.shared.widgets.StreaklyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        padding = 14.dp,
        borderRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = AppTextStyles.label(accentColor).copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            )
            content()
        }
    }
}

@Composable
fun TimeSelectionDialog(
    hour: Int,
    minute: Int,
    accentColor: Color,
    getLabel: (String, String, String) -> String,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var selectedHour12 by remember {
        mutableIntStateOf(
            when {
                hour == 0 -> 12
                hour == 12 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
        )
    }
    var selectedMinute by remember { mutableIntStateOf(minute) }
    var isAM by remember { mutableStateOf(hour < 12) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bgSecondary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = getLabel("Alert Time Selection", "स्मरण घंटा", "अलार्मची वेळ निवडा"),
                    style = AppTextStyles.headingMedium,
                    color = accentColor
                )

                TimePickerSheet(
                    selectedHour = selectedHour12,
                    selectedMinute = selectedMinute,
                    isAM = isAM,
                    onTimeChanged = { h, m, am ->
                        selectedHour12 = h
                        selectedMinute = m
                        isAM = am
                    },
                    accentColor = accentColor,
                    getLabel = getLabel,
                    showQuickPick = false
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        onDismiss()
                    }) {
                        Text(text = getLabel("Cancel", "रद्द करें", "रद्द करा"), color = AppColors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.confirm()
                            val hour24 = if (isAM) {
                                if (selectedHour12 == 12) 0 else selectedHour12
                            } else {
                                if (selectedHour12 == 12) 12 else selectedHour12 + 12
                            }
                            onSave(hour24, selectedMinute)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text(text = getLabel("Save", "सुरक्षित करें", "जतन करा"), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NumberSpinnerColumn(
    value: Int,
    range: IntRange,
    label: String,
    onValChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = AppTextStyles.caption, color = AppColors.textSecondary)
        
        IconButton(onClick = {
            val next = if (value + 1 in range) value + 1 else range.first
            onValChange(next)
        }) {
            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Add", tint = AppColors.textPrimary)
        }

        Text(
            text = String.format("%02d", value),
            style = AppTextStyles.headingLarge
        )

        IconButton(onClick = {
            val prev = if (value - 1 in range) value - 1 else range.last
            onValChange(prev)
        }) {
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Minus", tint = AppColors.textPrimary)
        }
    }
}

@Composable
fun ResetConfirmDialog(
    accentColor: Color,
    getLabel: (String, String, String) -> String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var countdownState by remember { mutableStateOf(0) } // 0 = idle, 1..5 = counting down, -1 = ready to confirm

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bgSecondary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.red.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = getLabel("Are you absolutely sure?", "क्या आप निश्चित हैं?", "तुम्हाला खात्री आहे का?"),
                    style = AppTextStyles.headingMedium,
                    color = AppColors.red
                )

                Text(
                    text = getLabel(
                        "This will completely clear your Streakly records database and action history. All achievements and locked badges will be wiped.",
                        "यह आपके सभी निरंतरता रिकॉर्ड और बनाए गए आदतें हमेशा के लिए हटा देगा। यह क्रिया पूर्ववत नहीं की जा सकती।",
                        "या कृतीमुळे आपल्या सवयी, आकडेवारी आणि यशाचे मागील सर्व रेकॉर्ड कायमस्वरूपी पुसून टाकले जातील."
                    ),
                    style = AppTextStyles.bodyMedium,
                    color = AppColors.textSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        onDismiss()
                    }) {
                        Text(text = getLabel("Cancel", "रद्द करें", "रद्द करा"), color = AppColors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            when (countdownState) {
                                0 -> {
                                    coroutineScope.launch {
                                        // Play error sound and error haptics when countdown begins
                                        com.example.core.utils.SoundService.playError()
                                        com.example.core.utils.HapticService.error()
                                        for (i in 5 downTo 1) {
                                            countdownState = i
                                            delay(1000)
                                        }
                                        countdownState = -1
                                    }
                                }
                                -1 -> {
                                    com.example.core.utils.SoundService.playDelete()
                                    com.example.core.utils.HapticService.doubleClick()
                                    onConfirm()
                                }
                            }
                        },
                        enabled = countdownState == 0 || countdownState == -1,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.red,
                            contentColor = Color.White,
                            disabledContainerColor = AppColors.red.copy(alpha = 0.5f),
                            disabledContentColor = Color.White
                        )
                    ) {
                        val buttonText = when (countdownState) {
                            in 1..5 -> "Hold on... $countdownState"
                            -1 -> getLabel("Tap again to confirm", "पुष्टि करने के लिए फिर से दबाएं", "पुष्टी करण्यासाठी पुन्हा दाबा")
                            else -> getLabel("Reset Now", "रीसेट करें", "आता रिसेट करा")
                        }
                        Text(text = buttonText, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionDialog(
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bgSecondary),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = AppTextStyles.headingMedium.copy(
                            color = AppColors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                    IconButton(
                        onClick = {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            onDismissRequest()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppColors.textSecondary
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun SelectionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    leftContent: @Composable (() -> Unit)? = null
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
        label = "selection_container_color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else AppColors.textPrimary,
        label = "selection_content_color"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leftContent != null) {
            leftContent()
            Spacer(modifier = Modifier.width(16.dp))
        }

        Text(
            text = label,
            style = AppTextStyles.titleMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            ),
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(20.dp))
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentThemeModeIndex: Int,
    onThemeModeSelected: (Int) -> Unit,
    accentColor: Color,
    getLabel: (String, String, String) -> String,
    onDismiss: () -> Unit
) {
    val options = listOf(
        Pair(1, getLabel("Light Mode", "लाइट मोड", "प्रकाश मोड")),
        Pair(2, getLabel("Dark Mode", "डार्क मोड", "गडद मोड")),
        Pair(0, getLabel("System Default", "सिस्टम डिफ़ॉल्ट", "सिस्टम पर्याय"))
    )

    SelectionDialog(
        title = getLabel("Select Theme Mode", "थीम का प्रकार चुनें", "थीम पर्याय निवडा"),
        onDismissRequest = onDismiss
    ) {
        options.forEach { (index, label) ->
            SelectionRow(
                label = label,
                isSelected = currentThemeModeIndex == index,
                onClick = {
                    com.example.core.utils.SoundService.playToggle()
                    com.example.core.utils.HapticService.selectionClick()
                    onThemeModeSelected(index)
                    onDismiss()
                },
                accentColor = accentColor
            )
        }
    }
}

@Composable
fun ColorSelectionDialog(
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit,
    accentColor: Color,
    getLabel: (String, String, String) -> String,
    onDismiss: () -> Unit
) {
    SelectionDialog(
        title = getLabel("Select Accent Color", "मुख्य रंग चुनें", "मुख्य रंग निवडा"),
        onDismissRequest = onDismiss
    ) {
        for (index in 0 until AppColors.accentOptions.size) {
            val option = AppColors.accentOptions[index]
            val isSelected = index == selectedColorIndex
            val color = option.primary

            val displayName = when (index) {
                0 -> getLabel("Neon Cyan", "नियोन स्यान", "नियोन स्यान")
                1 -> getLabel("Electric Blue", "इलेक्ट्रिक ब्लू", "इलेक्ट्रिक ब्लू")
                2 -> getLabel("Purple Pulse", "पर्पल पल्स", "पर्पल पल्स")
                3 -> getLabel("Emerald Green", "पन्ना हरा", "एमराल्ड हिरवा")
                4 -> getLabel("Sunset Orange", "सूर्यास्त नारंगी", "सूर्यास्त नारिंगी")
                5 -> getLabel("Rose Pink", "गुलाब गुलाबी", "गुलाब गुलाबी")
                else -> option.name
            }

            SelectionRow(
                label = displayName,
                isSelected = isSelected,
                onClick = {
                    com.example.core.utils.SoundService.playTap()
                    com.example.core.utils.HapticService.confirm()
                    onColorSelected(index)
                    onDismiss()
                },
                accentColor = accentColor,
                leftContent = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color, CircleShape)
                    )
                }
            )
        }
    }
}


