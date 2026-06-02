package com.example.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun StreaklyTheme(
    accentColorIndex: Int = 0, // 0=orange, 1=blue, 2=purple, 3=green, 4=amber, 5=red
    themeModeIndex: Int = 2,    // 0=system, 1=light, 2=dark
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeModeIndex) {
        0 -> isSystemDark
        1 -> false
        2 -> true
        else -> true
    }

    // Update the reactive isDark flag in AppColors
    AppColors.isDark = isDark

    val view = LocalView.current
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

    val activeAccent = AppColors.accentColorOptions.getOrNull(accentColorIndex) ?: AppColors.accentOrange

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = activeAccent,
            onPrimary = Color(0xFF000000),
            primaryContainer = activeAccent.copy(alpha = 0.2f),
            onPrimaryContainer = AppColors.textPrimary,
            secondary = activeAccent,
            onSecondary = Color(0xFF000000),
            background = AppColors.bgPrimary,
            onBackground = AppColors.textPrimary,
            surface = AppColors.bgSecondary,
            onSurface = AppColors.textPrimary,
            surfaceVariant = AppColors.bgCard,
            onSurfaceVariant = AppColors.textSecondary,
            outline = AppColors.border,
            error = AppColors.red,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = activeAccent,
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = activeAccent.copy(alpha = 0.15f),
            onPrimaryContainer = AppColors.textPrimary,
            secondary = activeAccent,
            onSecondary = Color(0xFFFFFFFF),
            background = AppColors.bgPrimary,
            onBackground = AppColors.textPrimary,
            surface = AppColors.bgSecondary,
            onSurface = AppColors.textPrimary,
            surfaceVariant = AppColors.bgCard,
            onSurfaceVariant = AppColors.textSecondary,
            outline = AppColors.border,
            error = AppColors.red,
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
