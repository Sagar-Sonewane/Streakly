package com.example.core.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object AppColors {
    var isDark: Boolean by mutableStateOf(true)

    val bgPrimary: Color get() = if (isDark) Color(0xFF080B14) else Color(0xFFF2F3F7)
    val bgSecondary: Color get() = if (isDark) Color(0xFF0F1320) else Color(0xFFFFFFFF)
    val bgTertiary: Color get() = if (isDark) Color(0xFF161C2E) else Color(0xFFEBEDF5)
    val bgCard: Color get() = if (isDark) Color(0xFF1A2035) else Color(0xFFFFFFFF)
    val bgElevated: Color get() = if (isDark) Color(0xFF212843) else Color(0xFFE2E5F0)

    val textPrimary: Color get() = if (isDark) Color(0xFFEAECF5) else Color(0xFF0D0D0D)
    val textSecondary: Color get() = if (isDark) Color(0xFF8892B0) else Color(0xFF7A7A8C)
    val textHint: Color get() = if (isDark) Color(0xFF4A5480) else Color(0xFFB0B0C0)

    val border: Color get() = if (isDark) Color(0xFF252D45) else Color(0xFFE8E8F0)

    // Accents
    val accentOrange = Color(0xFFFF6B35)   // Index 0 (Flame Orange)
    val electricViolet = Color(0xFF6C63FF) // Index 1 (Electric Violet)
    val cyberTeal = Color(0xFF00D4AA)      // Index 2 (Cyber Teal)
    val neonGreen = Color(0xFF00C853)      // Index 3 (Neon Green)
    val goldAmber = Color(0xFFFFAB00)      // Index 4 (Gold Amber)
    val hotPink = Color(0xFFFF3D71)        // Index 5 (Hot Pink)

    // Backward compatibility mappings
    val red = hotPink
    val blue = electricViolet
    val purple = cyberTeal
    val accentAmber = goldAmber

    // Accent options by index
    val accentColorOptions = listOf(
        accentOrange,   // Index 0
        electricViolet, // Index 1
        cyberTeal,      // Index 2
        neonGreen,      // Index 3
        goldAmber,      // Index 4
        hotPink         // Index 5
    )

    val accentNames = listOf(
        "Orange",
        "Violet",
        "Teal",
        "Green",
        "Amber",
        "Pink"
    )

    val taskCategoryColors: List<Color> get() = if (isDark) {
        listOf(
            Color(0xFF4A90FF),  // blue
            Color(0xFF9B59B6),  // purple
            Color(0xFF2ECC71),  // green
            Color(0xFFFFB347),  // amber
            Color(0xFFE74C3C),  // red
            Color(0xFF1ABC9C)   // teal
        )
    } else {
        listOf(
            Color(0xFF2B6FD4),  // blue (deeper for light bg)
            Color(0xFF7B3FA6),  // purple
            Color(0xFF1A9E55),  // green
            Color(0xFFCC8800),  // amber
            Color(0xFFCC2222),  // red
            Color(0xFF0E8870)   // teal
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
}
