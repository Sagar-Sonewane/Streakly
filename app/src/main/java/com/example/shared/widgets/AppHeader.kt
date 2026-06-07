package com.example.shared.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppHeader(
    currentIndex: Int,
    currentStreak: Int,
    accentColorIndex: Int,
    notificationsEnabled: Boolean,
    onNotificationsTap: () -> Unit,
    modifier: Modifier = Modifier,
    language: String = "en",
    userName: String = "",
    onStreakBadgeTap: () -> Unit = {}
) {
    val isDark = AppColors.isDark
    val accent = AppColors.accentColorOptions[accentColorIndex]

    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val displayName = if (userName.isBlank()) {
        when (language) {
            "hi" -> "वहाँ"
            "mr" -> "तेथे"
            else -> "there"
        }
    } else userName

    val greeting = remember(hour, language, displayName) {
        when (language) {
            "hi" -> when {
                hour in 0..11 -> "सुप्रभात, $displayName"
                hour in 12..16 -> "नमस्कार, $displayName"
                else -> "शुभ संध्या, $displayName"
            }
            "mr" -> when {
                hour in 0..11 -> "शुभ सकाळ, $displayName"
                hour in 12..16 -> "नमस्कार, $displayName"
                else -> "शुभ संध्याकाळ, $displayName"
            }
            else -> when {
                hour in 0..11 -> "Good morning, $displayName"
                hour in 12..16 -> "Good afternoon, $displayName"
                else -> "Good evening, $displayName"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDark) AppColors.bgPrimary else Color(0xFFF2F3F7))
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── LEFT: Bold Title / Greeting ──
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "TitleAnimation"
                ) { targetIndex ->
                    val textColor = if (isDark) AppColors.textPrimary else Color(0xFF0D0D0D)
                    if (targetIndex == 0) {
                        Column {
                            Text(
                                text = greeting,
                                style = AppTextStyles.screenTitle(textColor).copy(
                                    fontSize = com.example.core.utils.Responsive.fp(19f),
                                    fontWeight = FontWeight.Black
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = com.example.core.utils.DateUtils.getFormattedDate(com.example.core.utils.DateUtils.getTodayKey(), language),
                                style = AppTextStyles.caption.copy(
                                    color = AppColors.textSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = com.example.core.utils.Responsive.fp(11f)
                                )
                            )
                        }
                    } else {
                        val titleText = when (targetIndex) {
                            1 -> if (language == "hi") "कैलेंडर" else if (language == "mr") "कॅलेंडर" else "HEATMAP"
                            2 -> if (language == "hi") "आंकड़े" else if (language == "mr") "आकडेवारी" else "ANALYTICS"
                            3 -> if (language == "hi") "सुझाव" else if (language == "mr") "विचार" else "JOURNAL"
                            4 -> if (language == "hi") "सेटिंग्स" else if (language == "mr") "सेटिंग्ज" else "SETTINGS"
                            else -> "STREAKLY"
                        }
                        Text(
                            text = titleText,
                            style = AppTextStyles.screenTitle(textColor)
                        )
                    }
                }
            }

            // ── RIGHT: Icons ──
            
            // Streak Flame Badge (like notification icon in reference)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .then(
                        if (!isDark) {
                            Modifier.shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                clip = false,
                                ambientColor = Color.Black.copy(alpha = 0.07f),
                                spotColor = Color.Black.copy(alpha = 0.07f)
                            )
                        } else Modifier
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) AppColors.bgTertiary else Color.White)
                    .clickable {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        onStreakBadgeTap()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak flame badge",
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
                
                // Current streak badge overlay
                Box(
                    modifier = Modifier
                        .offset(x = 12.dp, y = (-12).dp)
                        .background(accent, RoundedCornerShape(10.dp))
                        .border(
                            width = 1.5.dp,
                            color = if (isDark) AppColors.bgPrimary else Color(0xFFF2F3F7),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$currentStreak",
                        style = AppTextStyles.hint(Color.White).copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = com.example.core.utils.Responsive.fp(9f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Notifications Bell Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .then(
                        if (!isDark) {
                            Modifier.shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                clip = false,
                                ambientColor = Color.Black.copy(alpha = 0.07f),
                                spotColor = Color.Black.copy(alpha = 0.07f)
                            )
                        } else Modifier
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) AppColors.bgTertiary else Color.White)
                    .clickable {
                        onNotificationsTap()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notifications Bell",
                    tint = if (isDark) AppColors.textSecondary else Color(0xFF7A7A8C),
                    modifier = Modifier.size(20.dp)
                )

                if (notificationsEnabled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(7.dp)
                            .background(accent, CircleShape)
                    )
                }
            }
        }

        // Bottom thin border (0.5.dp border separator)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(AppColors.border)
        )
    }
}
