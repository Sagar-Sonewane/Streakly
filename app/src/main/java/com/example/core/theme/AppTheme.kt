package com.example.core.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.content.Context

val LocalAccentColor = staticCompositionLocalOf { Color(0xFFFF5722) }
val LocalAccentColorLighter = staticCompositionLocalOf { Color(0xFFFF7043) }

val LocalBgPrimary = staticCompositionLocalOf { Color(0xFF080B14) }
val LocalBgSecondary = staticCompositionLocalOf { Color(0xFF0F1320) }
val LocalBgTertiary = staticCompositionLocalOf { Color(0xFF161C2E) }
val LocalBgCard = staticCompositionLocalOf { Color(0xFF1A2035) }
val LocalBgElevated = staticCompositionLocalOf { Color(0xFF212843) }
val LocalTextPrimary = staticCompositionLocalOf { Color(0xFFEAECF5) }
val LocalTextSecondary = staticCompositionLocalOf { Color(0xFF8892B0) }
val LocalTextHint = staticCompositionLocalOf { Color(0xFF4A5480) }
val LocalBorder = staticCompositionLocalOf { Color(0xFF252D45) }

@Composable
fun StreaklyTheme(
    accentColorIndex: Int = 4, // 0=neon cyan, 1=electric blue, 2=purple pulse, 3=emerald green, 4=sunset orange, 5=rose pink
    themeModeIndex: Int = 0,    // 0=system, 1=light, 2=dark
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    
    val isSystemDark = isSystemInDarkTheme()
    
    val isDark = when (themeModeIndex) {
        1 -> false
        2 -> true
        else -> isSystemDark
    }

    // Update the reactive isDark flag in AppColors
    AppColors.isDark = isDark

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val accentInfo = remember(accentColorIndex) {
        AppColors.accentOptions.getOrNull(accentColorIndex) ?: AppColors.accentOptions[4]
    }

    val activeAccent = accentInfo.primary

    // Dynamic Theme Color Animations
    val targetBgPrimary = if (isDark) Color(0xFF080B14) else Color(0xFFF2F3F7)
    val targetBgSecondary = if (isDark) Color(0xFF0F1320) else Color(0xFFFFFFFF)
    val targetBgTertiary = if (isDark) Color(0xFF161C2E) else Color(0xFFEBEDF5)
    val targetBgCard = if (isDark) Color(0xFF1A2035) else Color(0xFFFFFFFF)
    val targetBgElevated = if (isDark) Color(0xFF212843) else Color(0xFFE2E5F0)
    val targetTextPrimary = if (isDark) Color(0xFFEAECF5) else Color(0xFF0D0D0D)
    val targetTextSecondary = if (isDark) Color(0xFF8892B0) else Color(0xFF7A7A8C)
    val targetTextHint = if (isDark) Color(0xFF4A5480) else Color(0xFFB0B0C0)
    val targetBorder = if (isDark) Color(0xFF252D45) else Color(0xFFE8E8F0)

    val bgPrimaryAnim by animateColorAsState(targetBgPrimary, label = "bgPrimary")
    val bgSecondaryAnim by animateColorAsState(targetBgSecondary, label = "bgSecondary")
    val bgTertiaryAnim by animateColorAsState(targetBgTertiary, label = "bgTertiary")
    val bgCardAnim by animateColorAsState(targetBgCard, label = "bgCard")
    val bgElevatedAnim by animateColorAsState(targetBgElevated, label = "bgElevated")
    val textPrimaryAnim by animateColorAsState(targetTextPrimary, label = "textPrimary")
    val textSecondaryAnim by animateColorAsState(targetTextSecondary, label = "textSecondary")
    val textHintAnim by animateColorAsState(targetTextHint, label = "textHint")
    val borderAnim by animateColorAsState(targetBorder, label = "border")

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = activeAccent,
            onPrimary = Color(0xFF000000),
            primaryContainer = activeAccent.copy(alpha = 0.2f),
            onPrimaryContainer = textPrimaryAnim,
            secondary = activeAccent,
            onSecondary = Color(0xFF000000),
            background = bgPrimaryAnim,
            onBackground = textPrimaryAnim,
            surface = bgSecondaryAnim,
            onSurface = textPrimaryAnim,
            surfaceVariant = bgCardAnim,
            onSurfaceVariant = textSecondaryAnim,
            outline = borderAnim,
            error = AppColors.red,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = activeAccent,
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = activeAccent.copy(alpha = 0.15f),
            onPrimaryContainer = textPrimaryAnim,
            secondary = activeAccent,
            onSecondary = Color(0xFFFFFFFF),
            background = bgPrimaryAnim,
            onBackground = textPrimaryAnim,
            surface = bgSecondaryAnim,
            onSurface = textPrimaryAnim,
            surfaceVariant = bgCardAnim,
            onSurfaceVariant = textSecondaryAnim,
            outline = borderAnim,
            error = AppColors.red,
            onError = Color.White
        )
    }

    CompositionLocalProvider(
        LocalAccentColor provides activeAccent,
        LocalAccentColorLighter provides accentInfo.lighter,
        LocalBgPrimary provides bgPrimaryAnim,
        LocalBgSecondary provides bgSecondaryAnim,
        LocalBgTertiary provides bgTertiaryAnim,
        LocalBgCard provides bgCardAnim,
        LocalBgElevated provides bgElevatedAnim,
        LocalTextPrimary provides textPrimaryAnim,
        LocalTextSecondary provides textSecondaryAnim,
        LocalTextHint provides textHintAnim,
        LocalBorder provides borderAnim
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

