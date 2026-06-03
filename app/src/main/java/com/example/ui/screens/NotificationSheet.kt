package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.StreaklyApp
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.core.utils.Responsive
import com.example.providers.Providers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsProvider = Providers.getSettings()
    val settingsState by settingsProvider.settingsState.collectAsState()

    val language = settingsState.language
    val accentColorIndex = settingsState.accentColorIndex
    val accentColor = AppColors.accentColorOptions[accentColorIndex]
    val isDark = AppColors.isDark

    var showTimePickerInSheet by remember { mutableStateOf(false) }

    // Helper for permission check
    fun checkHasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    var isPermissionGranted by remember { mutableStateOf(checkHasPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isPermissionGranted = granted
        if (granted) {
            settingsProvider.updateNotificationsEnabled(true)
            StreaklyApp.instance.notificationService.scheduleDailyReminder(
                settingsState.reminderHour,
                settingsState.reminderMinute
            )
        } else {
            settingsProvider.updateNotificationsEnabled(false)
        }
    }

    // Refresh permission status when sheet is displayed
    LaunchedEffect(Unit) {
        isPermissionGranted = checkHasPermission()
    }

    val getLabel = { en: String, hi: String, mr: String ->
        when (language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }

    val formattedTime = String.format("%02d:%02d", settingsState.reminderHour, settingsState.reminderMinute)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = AppColors.bgSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle(color = AppColors.border) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title
            Text(
                text = getLabel("Notifications", "सूचनाएं", "सूचना"),
                style = AppTextStyles.headingMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )

            // Warning banner if permissions are disabled but user turned them on (or if we detect denied state)
            if (!isPermissionGranted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.red.copy(alpha = 0.1f))
                        .border(1.dp, AppColors.red.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AppColors.red,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getLabel("Permission Required", "अनुमति आवश्यक है", "परवानगी आवश्यक आहे"),
                            style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = getLabel(
                                "Notification permissions are disabled. Please enable them in system settings to receive reminders.",
                                "सूचना अनुमतियां बंद हैं। याद दिलाने के लिए कृपया सिस्टम सेटिंग्स में जाकर उन्हें चालू करें।",
                                "सूचना परवानग्या बंद आहेत. स्मरणपत्रे मिळवण्यासाठी कृपया सिस्टम सेटिंग्जमध्ये चालू करा."
                            ),
                            style = AppTextStyles.bodySmall,
                            color = AppColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.red),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = getLabel("Open Settings", "सेटिंग्स खोलें", "सेटिंग्ज उघडा"),
                                style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Daily alerts toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.bgPrimary)
                    .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = getLabel("Daily Reminders", "दैनिक याद दिलाएं", "दैनिक आठवण"),
                            style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = if (settingsState.notificationsEnabled && isPermissionGranted) {
                                getLabel("Active at $formattedTime", "$formattedTime बजे सक्रिय", "$formattedTime वाजता सक्रिय")
                            } else {
                                getLabel("Disabled", "निष्क्रिय", "बंद")
                            },
                            style = AppTextStyles.bodySmall,
                            color = AppColors.textSecondary
                        )
                    }
                }

                Switch(
                    checked = settingsState.notificationsEnabled && isPermissionGranted,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (checkHasPermission()) {
                                settingsProvider.updateNotificationsEnabled(true)
                                StreaklyApp.instance.notificationService.scheduleDailyReminder(
                                    settingsState.reminderHour,
                                    settingsState.reminderMinute
                                )
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    settingsProvider.updateNotificationsEnabled(true)
                                    StreaklyApp.instance.notificationService.scheduleDailyReminder(
                                        settingsState.reminderHour,
                                        settingsState.reminderMinute
                                    )
                                }
                            }
                        } else {
                            settingsProvider.updateNotificationsEnabled(false)
                            StreaklyApp.instance.notificationService.cancelReminder()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = accentColor,
                        uncheckedThumbColor = AppColors.textSecondary,
                        uncheckedTrackColor = AppColors.border
                    )
                )
            }

            // Time selector card (only when notifications active)
            if (settingsState.notificationsEnabled && isPermissionGranted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColors.bgPrimary)
                        .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
                        .clickable { showTimePickerInSheet = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.border),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = AppColors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = getLabel("Alert Time", "अनुस्मारक समय", "स्मरण वेळ"),
                                style = AppTextStyles.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AppColors.textPrimary
                            )
                            Text(
                                text = getLabel("Tap to modify alarm time", "समय बदलने के लिए टैप करें", "वेळ बदलण्यासाठी दाबा"),
                                style = AppTextStyles.bodySmall,
                                color = AppColors.textSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            style = AppTextStyles.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                        Button(
                            onClick = { showTimePickerInSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.border),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = getLabel("Change", "बदलें", "बदला"),
                                style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                                color = AppColors.textPrimary
                            )
                        }
                    }
                }

                // Info Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.08f))
                        .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    val nextRemLabel = getLabel(
                        "Next reminder at $formattedTime daily",
                        "अगला अनुस्मारक रोजाना $formattedTime बजे",
                        "पुढील सूचना दररोज $formattedTime वाजता"
                    )
                    Text(
                        text = nextRemLabel,
                        style = AppTextStyles.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = AppColors.textPrimary
                    )
                }
            }

            // Debug send test notification button
            if (com.example.BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        StreaklyApp.instance.notificationService.sendTestNotification()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Send Test Notification (5s Delay)",
                        color = accentColor,
                        style = AppTextStyles.actionButton.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showTimePickerInSheet) {
        TimeSelectionDialog(
            hour = settingsState.reminderHour,
            minute = settingsState.reminderMinute,
            accentColor = accentColor,
            getLabel = getLabel,
            onDismiss = { showTimePickerInSheet = false },
            onSave = { h, m ->
                settingsProvider.updateReminderTime(h, m)
                if (settingsState.notificationsEnabled && checkHasPermission()) {
                    StreaklyApp.instance.notificationService.scheduleDailyReminder(h, m)
                }
                showTimePickerInSheet = false
            }
        )
    }
}
