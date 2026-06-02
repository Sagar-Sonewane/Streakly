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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onSettingsTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppColors.isDark
    val accent = AppColors.accentColorOptions[accentColorIndex]

    val titleText = when (currentIndex) {
        0 -> "HOME"
        1 -> "HEATMAP"
        2 -> "ANALYTICS"
        3 -> "JOURNAL"
        4 -> "SETTINGS"
        else -> "STREAKLY"
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
                .height(60.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── LEFT: Bold Title ──
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                AnimatedContent(
                    targetState = titleText,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "TitleAnimation"
                ) { targetTitle ->
                    Text(
                        text = targetTitle,
                        style = AppTextStyles.screenTitle(
                            if (isDark) AppColors.textPrimary else Color(0xFF0D0D0D)
                        )
                    )
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

            // Settings button (Tune Icon)
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
                        onSettingsTap()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Settings Tune Menu",
                    tint = if (isDark) AppColors.textSecondary else Color(0xFF7A7A8C),
                    modifier = Modifier.size(20.dp)
                )
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
