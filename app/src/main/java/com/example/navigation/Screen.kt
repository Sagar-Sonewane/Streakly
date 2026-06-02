package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val titleEn: String,
    val titleHi: String,
    val titleMr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        titleEn = "Home",
        titleHi = "होम",
        titleMr = "होम",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Heatmap : Screen(
        route = "heatmap",
        titleEn = "Heatmap",
        titleHi = "कैलेंडर",
        titleMr = "कॅलेंडर",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )

    object Stats : Screen(
        route = "stats",
        titleEn = "Stats",
        titleHi = "आंकड़े",
        titleMr = "आकडेवारी",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )

    object Reflect : Screen(
        route = "reflect",
        titleEn = "Reflect",
        titleHi = "सुझाव",
        titleMr = "विचार",
        selectedIcon = Icons.Filled.Edit,
        unselectedIcon = Icons.Outlined.Edit
    )

    object Settings : Screen(
        route = "settings",
        titleEn = "Settings",
        titleHi = "सेटिंग्स",
        titleMr = "सेटिंग्ज",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    fun getTitle(lang: String): String {
        return when (lang) {
            "hi" -> titleHi
            "mr" -> titleMr
            else -> titleEn
        }
    }

    companion object {
        val items: List<Screen>
            get() = listOf(Home, Heatmap, Stats, Reflect, Settings)
    }
}
