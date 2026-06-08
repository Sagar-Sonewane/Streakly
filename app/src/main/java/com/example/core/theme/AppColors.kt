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
    val accentEmberOrange = Color(0xFFFF5722)
    val accentRoyalGold = Color(0xFFF9A825)
    val accentFlamingoPink = Color(0xFFF06292)
    val accentElectricPurple = Color(0xFF7C4DFF)
    val accentNeonCyan = Color(0xFF00BCD4)
    val accentNeonGreen = Color(0xFF00E676)

    val accentEmberOrangeLighter = Color(0xFFFF7043)
    val accentRoyalGoldLighter = Color(0xFFFFCA28)
    val accentFlamingoPinkLighter = Color(0xFFF48FB1)
    val accentElectricPurpleLighter = Color(0xFF9E6FFF)
    val accentNeonCyanLighter = Color(0xFF26C6DA)
    val accentNeonGreenLighter = Color(0xFF69F0AE)

    val accentOptions = listOf(
        AccentColorInfo("Ember Orange", "#FF5722", accentEmberOrange, accentEmberOrangeLighter, "🔥"),
        AccentColorInfo("Royal Gold", "#F9A825", accentRoyalGold, accentRoyalGoldLighter, "👑"),
        AccentColorInfo("Flamingo Pink", "#F06292", accentFlamingoPink, accentFlamingoPinkLighter, "💗"),
        AccentColorInfo("Electric Purple", "#7C4DFF", accentElectricPurple, accentElectricPurpleLighter, "⚡"),
        AccentColorInfo("Neon Cyan", "#00BCD4", accentNeonCyan, accentNeonCyanLighter, "🌊"),
        AccentColorInfo("Neon Green", "#00E676", accentNeonGreen, accentNeonGreenLighter, "🍀")
    )

    val red = Color(0xFFFF3D71)
    val blue = Color(0xFF1565C0)
    val purple = Color(0xFF5C6BC0)
    val accentAmber = accentRoyalGold
    val accentOrange = accentEmberOrange
    val electricViolet = Color(0xFF6C63FF)
    val cyberTeal = Color(0xFF00D4AA)
    val neonGreen = Color(0xFF00C853)
    val goldAmber = Color(0xFFFFAB00)
    val hotPink = Color(0xFFFF3D71)

    // Accent options by index
    val accentColorOptions = listOf(
        accentEmberOrange,      // Index 0
        accentRoyalGold,        // Index 1
        accentFlamingoPink,     // Index 2
        accentElectricPurple,   // Index 3
        accentNeonCyan,         // Index 4
        accentNeonGreen         // Index 5
    )

    val accentNames = listOf(
        "Ember Orange",
        "Royal Gold",
        "Flamingo Pink",
        "Electric Purple",
        "Neon Cyan",
        "Neon Green"
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
            accentEmberOrange -> Color(0xFFD84315)
            accentRoyalGold -> Color(0xFFE65100)
            accentFlamingoPink -> Color(0xFFC2185B)
            accentElectricPurple -> Color(0xFF4527A0)
            accentNeonCyan -> Color(0xFF006064)
            accentNeonGreen -> Color(0xFF1B5E20)
            else -> color
        }
    }
}
