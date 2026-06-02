package com.example.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Responsive {
    private var screenWidthDp: Float = 390f
    private var screenHeightDp: Float = 844f
    private var density: Float = 2.75f
    private var fontScale: Float = 1.0f

    @Composable
    fun Init() {
        val configuration = LocalConfiguration.current
        screenWidthDp = configuration.screenWidthDp.toFloat()
        screenHeightDp = configuration.screenHeightDp.toFloat()
        density = LocalDensity.current.density
        fontScale = LocalDensity.current.fontScale
    }

    // Percentage of screen width
    fun w(percent: Float): Dp {
        return (screenWidthDp * percent / 100f).dp
    }

    // Percentage of screen height
    fun h(percent: Float): Dp {
        return (screenHeightDp * percent / 100f).dp
    }

    // Scale a size relative to 390px baseline (iPhone 14 / standard Android flagship screen width)
    fun sp(size: Float): Dp {
        val ratio = (screenWidthDp / 390f).coerceIn(0.85f, 1.25f)
        return (size * ratio).dp
    }

    // Font scale — resists system font size changes blowing up layouts
    fun fp(size: Float): TextUnit {
        // Clamps system text scale to narrow band to prevent extreme text size blowing up layouts
        val clampedFontScale = fontScale.coerceIn(0.85f, 1.1f)
        val rawSize = size / fontScale
        return (rawSize * clampedFontScale).sp
    }

    // Device category determinations
    val isSmall: Boolean get() = screenWidthDp < 360f
    val isMedium: Boolean get() = screenWidthDp >= 360f && screenWidthDp < 410f
    val isLarge: Boolean get() = screenWidthDp >= 410f

    // Adaptive chooser
    fun <T> adaptive(small: T, medium: T, large: T): T {
        return when {
            isSmall -> small
            isMedium -> medium
            else -> large
        }
    }
}
