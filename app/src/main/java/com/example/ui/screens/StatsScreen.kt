package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val isDark = AppColors.isDark
    
    // Define three different pastel colors for metrics cards
    // Active (Accent Color), Longest (Blue), Total Days (Purple)
    val activeBg = if (isDark) accentColor.copy(alpha = 0.18f) else accentColor.copy(alpha = 0.12f)
    val longestBg = if (isDark) AppColors.blue.copy(alpha = 0.18f) else AppColors.blue.copy(alpha = 0.12f)
    val totalBg = if (isDark) AppColors.purple.copy(alpha = 0.18f) else AppColors.purple.copy(alpha = 0.12f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(Responsive.sp(10f))
    ) {
        // Score 1: Active
        MetricCard(
            title = getLabel("ACTIVE", "सक्रिय", "सक्रिय"),
            value = "${streakModel.currentStreak}",
            unit = getLabel(" Days", " दिन", " दिवस"),
            textColor = AppColors.getLegibleColor(accentColor),
            bgColor = activeBg,
            modifier = Modifier.weight(1f)
        )

        // Score 2: Longest
        MetricCard(
            title = getLabel("LONGEST", "सर्वोच्च", "सर्वोच्च"),
            value = "${streakModel.longestStreak}",
            unit = getLabel(" Days", " दिन", " दिवस"),
            textColor = AppColors.getLegibleColor(AppColors.blue),
            bgColor = longestBg,
            modifier = Modifier.weight(1f)
        )

        // Score 3: Total Completed days
        MetricCard(
            title = getLabel("TOTAL DAYS", "कुल दिन", "एकूण दिवस"),
            value = "${streakModel.totalStreakDays}",
            unit = getLabel(" Done", " पूर्ण", " पूर्ण"),
            textColor = AppColors.getLegibleColor(AppColors.purple),
            bgColor = totalBg,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    com.example.shared.widgets.StreaklyCard(
        modifier = modifier,
        padding = 14.dp,
        borderRadius = 24.dp, // Premium pill-shaped style
        color = bgColor,
        borderColor = textColor.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = AppTextStyles.hint(textColor.copy(alpha = 0.7f)).copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontSize = 10.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = value,
                style = AppTextStyles.statMedium(textColor).copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    letterSpacing = (-0.5).sp
                ),
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = unit,
                style = AppTextStyles.hint(textColor.copy(alpha = 0.6f)).copy(
                    fontWeight = FontWeight.Bold
                ),
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
    val isDark = AppColors.isDark
    
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

    // Get completion percentages for the last 7 days
    val pcts = remember(dayRecords, lastSevenDays) {
        lastSevenDays.map { dateKey ->
            val record = dayRecords.find { it.dateKey == dateKey }
            if (record == null || record.tasksTotal == 0) {
                0f
            } else {
                record.completionPct.toFloat()
            }
        }
    }

    com.example.shared.widgets.StreaklyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        padding = 20.dp,
        borderRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = getLabel("EFFICIENCY PROGRESS", "सक्षम प्रगति", "कार्यक्षमता प्रगती आलेख"),
                style = AppTextStyles.hint(AppColors.textHint).copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Black
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Canvas drawing the Bezier area chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val gridLineColor = AppColors.border
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw horizontal grid lines (at 0%, 50%, 100%)
                    val gridYs = listOf(0f, height / 2f, height)
                    gridYs.forEach { yVal ->
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, yVal),
                            end = Offset(width, yVal),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        )
                    }

                    if (pcts.isNotEmpty()) {
                        val points = pcts.mapIndexed { idx, pct ->
                            val x = idx * (width / 6f)
                            // Invert pct so 100% is at y=0, 0% is at y=height (add bounds cushion)
                            val y = height - (pct / 100f) * (height - 10f) - 5f
                            Offset(x, y)
                        }

                        // Create Bezier Path
                        val strokePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val controlX = p0.x + (p1.x - p0.x) / 2f
                                cubicTo(
                                    controlX, p0.y,
                                    controlX, p1.y,
                                    p1.x, p1.y
                                )
                            }
                        }

                        // Create closed path for gradient area fill
                        val fillPath = Path().apply {
                            addPath(strokePath)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }

                        // Draw filled area with gradient
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )

                        // Draw path stroke
                        drawPath(
                            path = strokePath,
                            color = accentColor,
                            style = Stroke(
                                width = 6f,
                                cap = StrokeCap.Round
                            )
                        )

                        // Draw circular node points
                        points.forEach { pt ->
                            drawCircle(
                                color = accentColor,
                                radius = 10f,
                                center = pt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 5f,
                                center = pt
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // X-axis Day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                lastSevenDays.forEach { dateKey ->
                    val label = DateUtils.getDayOfWeekLabel(dateKey, language)
                    Text(
                        text = label,
                        style = AppTextStyles.label(AppColors.textSecondary).copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    
                    // Unlocked / Claimed badge uses soft accent color, locked is subtle gray
                    val badgeCardBg = if (isClaimed) {
                        accentColor.copy(alpha = 0.08f)
                    } else if (isUnlocked) {
                        accentColor.copy(alpha = 0.04f)
                    } else {
                        AppColors.bgSecondary
                    }

                    val badgeBorderColor = if (isClaimed) {
                        accentColor.copy(alpha = 0.35f)
                    } else if (isUnlocked) {
                        accentColor.copy(alpha = 0.2f)
                    } else {
                        AppColors.border
                    }

                    com.example.shared.widgets.StreaklyCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("milestone_badge_$milestone"),
                        padding = 18.dp,
                        borderRadius = 24.dp, // Soft large rounded cards
                        color = badgeCardBg,
                        borderColor = badgeBorderColor
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isClaimed) accentColor.copy(alpha = 0.15f)
                                        else if (isUnlocked) accentColor.copy(alpha = 0.08f)
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
                                        tint = accentColor.copy(alpha = 0.6f),
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
                                    if (isClaimed) AppColors.getLegibleColor(accentColor)
                                    else if (isUnlocked) AppColors.getLegibleColor(accentColor).copy(alpha = 0.8f)
                                    else AppColors.textPrimary
                                ).copy(fontWeight = FontWeight.Black, fontSize = 16.sp),
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = getLabel("Goal: $milestone Days", "लक्ष्य: $milestone दिन", "ध्येय: $milestone दिवस"),
                                style = AppTextStyles.hint(
                                    if (isClaimed || isUnlocked) AppColors.getLegibleColor(accentColor).copy(alpha = 0.6f)
                                    else AppColors.textSecondary
                                ).copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
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
