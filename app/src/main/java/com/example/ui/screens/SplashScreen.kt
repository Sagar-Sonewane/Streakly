package com.example.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.StreaklyApp
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.example.providers.Providers

@Composable
fun SplashScreen(
    accentColorIndex: Int,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("streakly_prefs", Context.MODE_PRIVATE)
    }
    val seenSplash = remember { sharedPrefs.getBoolean("seen_splash", false) }
    val firstLaunchDone = remember { sharedPrefs.getBoolean("first_launch_done", false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val settingsProvider = Providers.getSettings()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val app = StreaklyApp.instance
        if (isGranted) {
            settingsProvider.updateNotificationsEnabled(true)
            settingsProvider.updateReminderTime(21, 0)
            app.notificationService.scheduleDailyReminder(21, 0)
        } else {
            settingsProvider.updateNotificationsEnabled(false)
        }
        sharedPrefs.edit().putBoolean("first_launch_done", true).apply()
        showPermissionDialog = false
        onNavigateToHome()
    }

    val activeAccent = AppColors.accentColorOptions.getOrNull(accentColorIndex) ?: AppColors.accentOrange

    // Animation values
    val circleScale = remember { Animatable(if (seenSplash) 1.0f else 0.6f) }
    val circleAlpha = remember { Animatable(if (seenSplash) 1.0f else 0.0f) }

    val flameScale = remember { Animatable(if (seenSplash) 1.0f else 0.0f) }
    val flameAlpha = remember { Animatable(if (seenSplash) 1.0f else 0.0f) }

    val glowAlpha = remember { Animatable(0.25f) }

    val nameAlpha = remember { Animatable(if (seenSplash) 1.0f else 0.0f) }
    val nameOffsetY = remember { Animatable(if (seenSplash) 0f else 20f) }

    val taglineAlpha = remember { Animatable(if (seenSplash) 1.0f else 0.0f) }
    val taglineOffsetY = remember { Animatable(if (seenSplash) 0f else 15f) }

    val versionAlpha = remember { Animatable(if (seenSplash) 1.0f else 0.0f) }

    val versionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName ?: "1.0.0"}"
        } catch (e: Exception) {
            "v1.0.0"
        }
    }

    LaunchedEffect(Unit) {
        if (!seenSplash) {
            // First Launch Animation Sequence
            // Logo circle: scale(0.6->1) + fadeIn, duration 600ms, elastic curve
            launch {
                circleScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                circleAlpha.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 600)
                )
            }

            // 300ms -> flame icon: scale(0->1) + fadeIn, duration 400ms
            launch {
                delay(300)
                launch {
                    flameScale.animateTo(
                        targetValue = 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                launch {
                    flameAlpha.animateTo(
                        targetValue = 1.0f,
                        animationSpec = tween(durationMillis = 400)
                    )
                }
            }

            // 600ms -> glow pulse: opacity(0.1->0.4->0.1) loop
            launch {
                delay(600)
                glowAlpha.animateTo(
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }

            // 700ms -> "STREAKLY" text: slideY(20px->0) + fadeIn, duration 400ms
            launch {
                delay(700)
                launch {
                    nameAlpha.animateTo(
                        targetValue = 1.0f,
                        animationSpec = tween(durationMillis = 400)
                    )
                }
                launch {
                    nameOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 400)
                    )
                }
            }

            // 900ms -> tagline: slideY(15px->0) + fadeIn, duration 350ms
            launch {
                delay(900)
                launch {
                    taglineAlpha.animateTo(
                        targetValue = 1.0f,
                        animationSpec = tween(durationMillis = 350)
                    )
                }
                launch {
                    taglineOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 350)
                    )
                }
            }

            // 1100ms -> version: fadeIn, duration 300ms
            launch {
                delay(1100)
                versionAlpha.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 300)
                )
            }

            // Mark as seen for subsequent launches
            sharedPrefs.edit().putBoolean("seen_splash", true).apply()

            // Navigate after 1800ms
            delay(1800)
            if (!firstLaunchDone) {
                showPermissionDialog = true
            } else {
                onNavigateToHome()
            }
        } else {
            // Subsequent Launch Sequence: Show brand moment for 800ms
            launch {
                glowAlpha.animateTo(
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            delay(800)
            if (!firstLaunchDone) {
                showPermissionDialog = true
            } else {
                onNavigateToHome()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo glow circle
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = circleScale.value
                        scaleY = circleScale.value
                        alpha = circleAlpha.value
                    }
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    activeAccent.copy(alpha = glowAlpha.value),
                                    activeAccent.copy(alpha = 0.0f)
                                ),
                                radius = size.width * 1.5f
                            ),
                            radius = size.width * 1.5f
                        )
                    }
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(activeAccent, AppColors.accentAmber)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = activeAccent,
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            scaleX = flameScale.value
                            scaleY = flameScale.value
                            alpha = flameAlpha.value
                        }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name "STREAKLY"
            Text(
                text = "STREAKLY",
                style = AppTextStyles.headingLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = activeAccent,
                    letterSpacing = 6.sp
                ),
                modifier = Modifier
                    .alpha(nameAlpha.value)
                    .offset { IntOffset(0, nameOffsetY.value.dp.roundToPx()) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline below name
            Text(
                text = "Build Discipline. Stay Consistent.",
                style = AppTextStyles.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = AppColors.textSecondary,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier
                    .alpha(taglineAlpha.value)
                    .offset { IntOffset(0, taglineOffsetY.value.dp.roundToPx()) }
            )
        }

        // Version Number at Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-40).dp)
        ) {
            Text(
                text = versionName,
                style = AppTextStyles.bodySmall.copy(
                    fontSize = 11.sp,
                    color = AppColors.textHint
                ),
                modifier = Modifier.alpha(versionAlpha.value)
            )
        }
    }

    if (showPermissionDialog) {
        Dialog(onDismissRequest = {}) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.bgSecondary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, AppColors.border, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(56.dp)
                    )

                    Text(
                        text = "Stay on Track!",
                        style = AppTextStyles.headingMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Get daily reminders to complete your tasks, build discipline, and keep your consistency streak alive.",
                        style = AppTextStyles.bodyMedium,
                        color = AppColors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val app = StreaklyApp.instance
                                settingsProvider.updateNotificationsEnabled(true)
                                settingsProvider.updateReminderTime(21, 0)
                                app.notificationService.scheduleDailyReminder(21, 0)
                                sharedPrefs.edit().putBoolean("first_launch_done", true).apply()
                                showPermissionDialog = false
                                onNavigateToHome()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "Allow Notifications",
                            color = Color.Black,
                            style = AppTextStyles.actionButton
                        )
                    }

                    TextButton(
                        onClick = {
                            settingsProvider.updateNotificationsEnabled(false)
                            sharedPrefs.edit().putBoolean("first_launch_done", true).apply()
                            showPermissionDialog = false
                            onNavigateToHome()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "Maybe Later",
                            color = AppColors.textSecondary,
                            style = AppTextStyles.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}
