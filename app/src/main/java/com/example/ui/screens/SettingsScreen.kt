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
import com.example.providers.Providers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
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

    val getLabel = { en: String, hi: String, mr: String ->
        when (language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        contentPadding = PaddingValues(top = 16.dp, bottom = Responsive.h(12f)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Appearance Theme
        item {
            SettingsGroupCard(
                title = getLabel("APPEARANCE STYLE", "सजावट शैली", "सजावट आकार"),
                accentColor = accentColor
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = getLabel("Theme Mode", "थीम का प्रकार", "थीम पर्याय"),
                        style = AppTextStyles.titleMedium.copy(fontSize = Responsive.fp(14f))
                    )

                    ThemeModeSelector(
                        currentThemeModeIndex = settingsState.themeModeIndex,
                        onThemeModeSelected = { settingsProvider.updateThemeModeIndex(it) },
                        accentColor = accentColor,
                        getLabel = getLabel
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = getLabel("Accent Color Options", "मुख्य थीम रंग बदलें", "मुख्य थीम रंग बदला"),
                        style = AppTextStyles.titleMedium.copy(fontSize = Responsive.fp(14f))
                    )

                    // Accent switches row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppColors.accentColorOptions.forEachIndexed { index, color ->
                            val isSelected = index == accentColorIndex
                            Box(
                                modifier = Modifier
                                    .size(Responsive.sp(34f))
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { settingsProvider.updateAccentColor(index) }
                                    .testTag("accent_color_picker_$index")
                            )
                        }
                    }
                }
            }
        }

        // Section: Language selection
        item {
            SettingsGroupCard(
                title = getLabel("LOCALIZATION LANGUAGE", "भाषा विकल्प", "भाषा पर्याय"),
                accentColor = accentColor
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = getLabel("Select App Language", "ऐप की भाषा चुनें", "अॅपची भाषा निवडा"),
                        style = AppTextStyles.titleMedium.copy(fontSize = Responsive.fp(14f))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Triple("en", "English", "EN"),
                            Triple("hi", "हिंदी", "HI"),
                            Triple("mr", "मराठी", "MR")
                        ).forEach { (code, name, abbreviation) ->
                            val isSelected = code == language

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(Responsive.sp(44f))
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) accentColor else AppColors.border,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(if (isSelected) accentColor.copy(alpha = 0.12f) else AppColors.bgPrimary)
                                    .clickable { settingsProvider.updateLanguage(code) }
                                    .testTag("lang_picker_$code"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    style = AppTextStyles.titleMedium.copy(
                                        fontSize = Responsive.fp(13f),
                                        color = if (isSelected) accentColor else AppColors.textPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }


        // Section: Mechanics sound & haptics
        item {
            SettingsGroupCard(
                title = getLabel("AUDIOS & TACTILITY", "आवाज़ और कंपन", "ध्वनी आणि कंपने"),
                accentColor = accentColor
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Sound effect switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = getLabel("Sound Effects", "सफलता की आवाज़ें", "यशाचे आवाज"),
                                style = AppTextStyles.titleMedium.copy(fontSize = Responsive.fp(14f))
                            )
                            Text(
                                text = getLabel("Play sounds during completions", "कार्य पूरा करते समय आवाज़ खेलें", "कृती पूर्ण झाल्यावर आवाज येईल"),
                                style = AppTextStyles.bodySmall.copy(fontSize = Responsive.fp(10f)),
                                color = AppColors.textSecondary
                            )
                        }

                        Switch(
                            checked = settingsState.soundEnabled,
                            onCheckedChange = { settingsProvider.updateSoundEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = AppColors.textSecondary,
                                uncheckedTrackColor = AppColors.border
                            )
                        )
                    }

                    // Haptics switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = getLabel("Haptic Vibrations", "हैप्टिक स्पर्श", "हॅप्टिक कंपने"),
                                style = AppTextStyles.titleMedium.copy(fontSize = Responsive.fp(14f))
                            )
                            Text(
                                text = getLabel("Vibrate on interactions", "स्पर्श / क्लिक पर हल्के कंपन", "बटण दाबल्यावर सौम्य कंपन जाणवेल"),
                                style = AppTextStyles.bodySmall.copy(fontSize = Responsive.fp(10f)),
                                color = AppColors.textSecondary
                            )
                        }

                        Switch(
                            checked = settingsState.hapticEnabled,
                            onCheckedChange = { settingsProvider.updateHapticEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = AppColors.textSecondary,
                                uncheckedTrackColor = AppColors.border
                            )
                        )
                    }
                }
            }
        }

        // Section: Danger zone
        item {
            com.example.shared.widgets.StreaklyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                padding = 14.dp,
                borderRadius = 20.dp,
                borderColor = AppColors.red.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = getLabel("DANGER ZONE", "जोखिम क्षेत्र", "धोकादायक क्षेत्र"),
                        style = AppTextStyles.label(AppColors.red).copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    )

                    Text(
                        text = getLabel("Clear Database", "डेटाबेस पूरी तरह साफ़ करें", "डेटाबेस पूर्णपणे पुसून टाका"),
                        style = AppTextStyles.cardTitle(AppColors.textPrimary).copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = getLabel(
                            "This action will permanently delete all daily streaks progress maps and logged habits records. You cannot undo this.",
                            "यह आपके सभी निरंतरता रिकॉर्ड और बनाए गए आदतें हमेशा के लिए हटा देगा। यह क्रिया पूर्ववत नहीं की जा सकती।",
                            "हे आपल्या सवयी, आकडेवारी आणि यशाचे मागील सर्व रेकॉर्ड कायमस्वरूपी काढून टाकेल."
                        ),
                        style = AppTextStyles.hint(AppColors.textSecondary)
                    )

                    Button(
                        onClick = { showResetDatabaseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.red),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("database_clear_danger_button")
                    ) {
                        Text(
                            text = getLabel("Factory Reset Progress", "ऐप पूरी तरह रीसेट करें", "प्रगती पूर्णपणे रीसेट करा"),
                            style = AppTextStyles.actionButton.copy(fontSize = 14.sp),
                            color = Color.White
                        )
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
                showTimePickerDialog = false
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
    var selectedHour by remember { mutableIntStateOf(hour) }
    var selectedMinute by remember { mutableIntStateOf(minute) }

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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour adjuster
                    NumberSpinnerColumn(
                        value = selectedHour,
                        range = 0..23,
                        label = getLabel("Hour", "घंटा", "तास"),
                        onValChange = { selectedHour = it }
                    )

                    Text(
                        text = ":",
                        style = AppTextStyles.headingLarge.copy(fontSize = 32.sp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Minute adjuster
                    NumberSpinnerColumn(
                        value = selectedMinute,
                        range = 0..59,
                        label = getLabel("Minute", "मिनट", "मिनिट"),
                        onValChange = { selectedMinute = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = getLabel("Cancel", "रद्द करें", "रद्द करा"), color = AppColors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onSave(selectedHour, selectedMinute) },
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
                    TextButton(onClick = onDismiss) {
                        Text(text = getLabel("Cancel", "रद्द करें", "रद्द करा"), color = AppColors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.red)
                    ) {
                        Text(text = getLabel("Reset Now", "रीसेट करें", "आता रिसेट करा"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeModeSelector(
    currentThemeModeIndex: Int,
    onThemeModeSelected: (Int) -> Unit,
    accentColor: Color,
    getLabel: (String, String, String) -> String
) {
    val options = listOf(
        Triple(2, Icons.Default.DarkMode, getLabel("Dark", "डार्क", "गडद")),
        Triple(1, Icons.Default.LightMode, getLabel("Light", "लाइट", "प्रकाश")),
        Triple(0, Icons.Default.Settings, getLabel("System", "सिस्टम", "सिस्टम"))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.bgTertiary)
            .border(
                width = 0.8.dp,
                color = AppColors.border,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (index, icon, label) ->
            val isSelected = currentThemeModeIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) accentColor else Color.Transparent)
                    .clickable { onThemeModeSelected(index) }
                    .padding(vertical = Responsive.sp(8f))
                    .testTag("theme_mode_selector_$index"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(Responsive.sp(18f)),
                        tint = if (isSelected) Color.Black else AppColors.textHint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = AppTextStyles.caption.copy(
                            fontSize = Responsive.fp(10f),
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else AppColors.textHint
                        )
                    )
                }
            }
        }
    }
}
