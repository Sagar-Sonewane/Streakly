package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.core.utils.DateUtils
import com.example.core.utils.Responsive
import com.example.data.models.DayRecord
import com.example.data.models.StreakModel
import com.example.providers.Providers
import com.example.shared.widgets.SectionHeader
import java.util.*

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier
) {
    val settingsProvider = Providers.getSettings()
    val streakProvider = Providers.getStreak()

    val settingsState by settingsProvider.settingsState.collectAsState()
    val streakState by streakProvider.streakState.collectAsState()
    val dayRecords by streakProvider.dayRecordsState.collectAsState()

    val language = settingsState.language
    val accentColor = AppColors.accentColorOptions[settingsState.accentColorIndex]

    val getLabel = { en: String, hi: String, mr: String ->
        when (language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        contentPadding = PaddingValues(top = 16.dp, bottom = Responsive.h(12f))
    ) {
        // Metrics Grid Row
        item {
            MetricsGrid(
                streakModel = streakState,
                accentColor = accentColor,
                getLabel = getLabel
            )
        }

        // Native High-Contrast Weekly Completion Chart
        item {
            SectionHeader(
                title = getLabel("Last 7 Days Efficiency", "पिछले ७ दिनों की दर", "गेल्या ७ दिवसांचे सातत्य"),
                subtitle = getLabel("Progress Chart", "प्रगति ग्राफ", "प्रगती आलेख")
            )
            WeeklyProgressChart(
                dayRecords = dayRecords,
                accentColor = accentColor,
                language = language,
                getLabel = getLabel
            )
        }

        // Milestones Badges Board
        item {
            SectionHeader(
                title = getLabel("Discipline Badges", "अनुशासन पुरस्कार", "शिस्त पदके"),
                subtitle = getLabel("Milestones Board", "मील के पत्थर", "यशाचे टप्पे")
            )
        }

        item {
            MilestonesBoard(
                claimedMilestones = streakState.getMilestonesClaimedList(),
                currentStreak = streakState.currentStreak,
                accentColor = accentColor,
                getLabel = getLabel
            )
        }
    }
}

@Composable
fun MetricsGrid(
    streakModel: StreakModel,
    accentColor: Color,
    getLabel: (String, String, String) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(Responsive.sp(10f))
    ) {
        // Score 1: Flame streak
        MetricCard(
            title = getLabel("ACTIVE", "सक्रिय", "सक्रिय"),
            value = "${streakModel.currentStreak}",
            unit = getLabel(" Days", " दिन", " दिवस"),
            color = accentColor,
            modifier = Modifier.weight(1f)
        )

        // Score 2: Longest
        MetricCard(
            title = getLabel("LONGEST", "सर्वोच्च", "सर्वोच्च"),
            value = "${streakModel.longestStreak}",
            unit = getLabel(" Days", " दिन", " दिवस"),
            color = AppColors.blue,
            modifier = Modifier.weight(1f)
        )

        // Score 3: Total Completed days
        MetricCard(
            title = getLabel("TOTAL DAYS", "कुल दिन", "एकूण दिवस"),
            value = "${streakModel.totalStreakDays}",
            unit = getLabel(" Done", " पूर्ण", " पूर्ण"),
            color = AppColors.purple,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    com.example.shared.widgets.StreaklyCard(
        modifier = modifier,
        padding = 12.dp,
        borderRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = AppTextStyles.hint(AppColors.textSecondary).copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = value,
                style = AppTextStyles.statMedium(color).copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = unit,
                style = AppTextStyles.hint(AppColors.textHint),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun WeeklyProgressChart(
    dayRecords: List<DayRecord>,
    accentColor: Color,
    language: String,
    getLabel: (String, String, String) -> String
) {
    // Generate the last 7 calendar days
    val lastSevenDays = remember {
        val cal = Calendar.getInstance()
        val list = mutableListOf<String>()
        for (i in 0..6) {
            list.add(DateUtils.getDateKey(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        list.reverse()
        list
    }

    com.example.shared.widgets.StreaklyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        padding = 14.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Responsive.h(20f))
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                lastSevenDays.forEach { dateKey ->
                    // Find actual day completion record
                    val record = dayRecords.find { it.dateKey == dateKey }
                    val pct = record?.completionPct ?: 0.0

                    // Draw a column with height proportional to percentage completed
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Numeric percentage overhead
                        Text(
                            text = "${pct.toInt()}%",
                            style = AppTextStyles.hint(
                                if (pct >= 80.0) accentColor else AppColors.textSecondary
                            ).copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Colored Bar filled container
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight(fraction = (pct / 100.0).toFloat().coerceIn(0.1f..1.0f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            if (pct >= 80.0) accentColor else accentColor.copy(alpha = 0.5f),
                                            accentColor.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            // Days labels on X-axis and Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppColors.border)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                lastSevenDays.forEach { dateKey ->
                    val label = DateUtils.getDayOfWeekLabel(dateKey, language)
                    Text(
                        text = label,
                        style = AppTextStyles.label(AppColors.textSecondary).copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun MilestonesBoard(
    claimedMilestones: List<String>,
    currentStreak: Int,
    accentColor: Color,
    getLabel: (String, String, String) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val milestoneList = listOf(3, 7, 10, 20, 50, 100)

        milestoneList.chunked(2).forEach { pairs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pairs.forEach { milestone ->
                    val isClaimed = claimedMilestones.contains(milestone.toString())
                    val isUnlocked = currentStreak >= milestone
                    
                    val details = rememberBadgeDetails(milestone, getLabel)

                    com.example.shared.widgets.StreaklyCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("milestone_badge_$milestone"),
                        padding = 16.dp,
                        color = if (isClaimed) accentColor.copy(alpha = 0.05f) else null
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isClaimed) accentColor.copy(alpha = 0.15f)
                                        else AppColors.bgPrimary
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isClaimed) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Claimed",
                                        tint = accentColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                } else if (isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Unlocked",
                                        tint = accentColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(26.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = AppColors.textHint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = details.title,
                                style = AppTextStyles.cardTitle(
                                    if (isClaimed) accentColor else AppColors.textPrimary
                                ).copy(fontWeight = FontWeight.Black),
                                maxLines = 1
                            )

                            Text(
                                text = getLabel("Goal: $milestone Days", "लक्ष्य: $milestone दिन", "ध्येय: $milestone दिवस"),
                                style = AppTextStyles.hint(AppColors.textSecondary)
                            )
                        }
                    }
                }
            }
        }
    }
}

private class BadgeInfo(val title: String)

@Composable
private fun rememberBadgeDetails(milestone: Int, getLabel: (String, String, String) -> String): BadgeInfo {
    val title = when (milestone) {
        3 -> getLabel("Spark", "चिंगारी", "ठिणगी")
        7 -> getLabel("Warrior", "योद्धा", "योद्धा")
        10 -> getLabel("Flame Master", "लपटें उस्ताद", "ज्वाला स्वामी")
        20 -> getLabel("Champion", "चैंपियन", "विजेता")
        50 -> getLabel("Elite", "असाधारण", "उत्कृष्ट")
        else -> getLabel("Legendary", "महान सम्राट", "महान")
    }
    return BadgeInfo(title)
}
