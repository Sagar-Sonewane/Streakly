package com.example.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class AccentColorInfo(
    val name: String,
    val hex: String,
    val primary: Color,
    val lighter: Color,
    val emoji: String
)

object AppColors {
    var isDark: Boolean by mutableStateOf(true)

    val bgPrimary: Color @Composable get() = LocalBgPrimary.current
    val bgSecondary: Color @Composable get() = LocalBgSecondary.current
    val bgTertiary: Color @Composable get() = LocalBgTertiary.current
    val bgCard: Color @Composable get() = LocalBgCard.current
    val bgElevated: Color @Composable get() = LocalBgElevated.current

    val textPrimary: Color @Composable get() = LocalTextPrimary.current
    val textSecondary: Color @Composable get() = LocalTextSecondary.current
    val textHint: Color @Composable get() = LocalTextHint.current

    val border: Color @Composable get() = LocalBorder.current


    // Accents
    val accentNeonCyan = Color(0xFF00BCD4)
    val accentElectricBlue = Color(0xFF2979FF)
    val accentPurplePulse = Color(0xFF7C4DFF)
    val accentEmeraldGreen = Color(0xFF00E676)
    val accentSunsetOrange = Color(0xFFFF5722)
    val accentRosePink = Color(0xFFF06292)

    val accentNeonCyanLighter = Color(0xFF26C6DA)
    val accentElectricBlueLighter = Color(0xFF82B1FF)
    val accentPurplePulseLighter = Color(0xFF9E6FFF)
    val accentEmeraldGreenLighter = Color(0xFF69F0AE)
    val accentSunsetOrangeLighter = Color(0xFFFF7043)
    val accentRosePinkLighter = Color(0xFFF48FB1)

    val accentOptions = listOf(
        AccentColorInfo("Neon Cyan", "#00BCD4", accentNeonCyan, accentNeonCyanLighter, ""),
        AccentColorInfo("Electric Blue", "#2979FF", accentElectricBlue, accentElectricBlueLighter, ""),
        AccentColorInfo("Purple Pulse", "#7C4DFF", accentPurplePulse, accentPurplePulseLighter, ""),
        AccentColorInfo("Emerald Green", "#00E676", accentEmeraldGreen, accentEmeraldGreenLighter, ""),
        AccentColorInfo("Sunset Orange", "#FF5722", accentSunsetOrange, accentSunsetOrangeLighter, ""),
        AccentColorInfo("Rose Pink", "#F06292", accentRosePink, accentRosePinkLighter, "")
    )

    val red = Color(0xFFFF3D71)
    val blue = Color(0xFF1565C0)
    val purple = Color(0xFF5C6BC0)
    val accentAmber = accentElectricBlue
    val accentOrange = accentSunsetOrange
    val electricViolet = Color(0xFF6C63FF)
    val cyberTeal = Color(0xFF00D4AA)
    val neonGreen = Color(0xFF00C853)
    val goldAmber = Color(0xFFFFAB00)
    val hotPink = Color(0xFFFF3D71)

    // Accent options by index
    val accentColorOptions = listOf(
        accentNeonCyan,         // Index 0
        accentElectricBlue,     // Index 1
        accentPurplePulse,      // Index 2
        accentEmeraldGreen,     // Index 3
        accentSunsetOrange,     // Index 4
        accentRosePink          // Index 5
    )

    val accentNames = listOf(
        "Neon Cyan",
        "Electric Blue",
        "Purple Pulse",
        "Emerald Green",
        "Sunset Orange",
        "Rose Pink"
    )

    val accentColor: Color @Composable get() = LocalAccentColor.current
    val accentColorLighter: Color @Composable get() = LocalAccentColorLighter.current

    val taskCategoryColors: List<Color> get() = if (isDark) {
        listOf(
            Color(0xFFFF5722), // Ember Orange
            Color(0xFF5C6BC0), // Electric Indigo
            Color(0xFF2E7D52), // Momentum Green
            Color(0xFF1565C0), // Sapphire Blue
            Color(0xFFF9A825), // Royal Gold
            Color(0xFFFF3D71), // Hot Pink
            Color(0xFF00D4AA), // Cyber Teal
            Color(0xFF00C853)  // Neon Green
        )
    } else {
        listOf(
            Color(0xFFD84315), // Ember Orange (Darker)
            Color(0xFF3F51B5), // Electric Indigo (Darker)
            Color(0xFF1B5E20), // Momentum Green (Darker)
            Color(0xFF0D47A1), // Sapphire Blue (Darker)
            Color(0xFFE65100), // Royal Gold (Darker)
            Color(0xFFC2185B), // Hot Pink (Darker)
            Color(0xFF004D40), // Cyber Teal (Darker)
            Color(0xFF1B5E20)  // Neon Green (Darker)
        )
    }

    // Semantic
    val success = Color(0xFF2ECC71)
    val danger = Color(0xFFE74C3C)
    val warning = Color(0xFFFFB347)
    val info = Color(0xFF4A90FF)

    // ── IMPORTANCE SYSTEM ─────────────────
    val importanceColorMap = mapOf(
        "regular" to Color(0xFF4A90FF),   // calm blue
        "moderate" to Color(0xFFFFAB00),   // warm amber
        "priority" to Color(0xFFFF3D71)    // hot pink-red
    )

    val importanceLabel = mapOf(
        "regular" to "Regular",
        "moderate" to "Moderate",
        "priority" to "Priority"
    )

    // Importance sort order (lower = higher priority)
    val importanceOrder = mapOf(
        "priority" to 0,
        "moderate" to 1,
        "regular" to 2
    )

    fun getImportanceColor(importance: String): Color {
        return importanceColorMap[importance] ?: Color(0xFF4A90FF)
    }

    fun getLegibleColor(color: Color): Color {
        if (isDark) return color
        return when (color) {
            accentNeonCyan -> Color(0xFF006064)
            accentElectricBlue -> Color(0xFF0D47A1)
            accentPurplePulse -> Color(0xFF4527A0)
            accentEmeraldGreen -> Color(0xFF1B5E20)
            accentSunsetOrange -> Color(0xFFD84315)
            accentRosePink -> Color(0xFFC2185B)
            else -> color
        }
    }
}
