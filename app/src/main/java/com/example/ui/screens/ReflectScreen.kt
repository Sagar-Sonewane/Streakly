package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.core.utils.DateUtils
import com.example.core.utils.Responsive
import com.example.data.models.ReflectionModel
import com.example.providers.Providers
import com.example.shared.widgets.SectionHeader
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReflectScreen(
    modifier: Modifier = Modifier
) {
    val settingsProvider = Providers.getSettings()
    val reflectionProvider = Providers.getReflection()

    val settingsState by settingsProvider.settingsState.collectAsState()
    val reflectionsList by reflectionProvider.reflectionsState.collectAsState()
    val todayReflection by reflectionProvider.todayReflectionState.collectAsState()

    val language = settingsState.language
    val accentColor = AppColors.accentColorOptions[settingsState.accentColorIndex]

    val getLabel = { en: String, hi: String, mr: String ->
        when (language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }

    val focusManager = LocalFocusManager.current

    // Entry inputs
    var selectedMoodIndex by remember { mutableIntStateOf(2) } // Default: Good
    var reflectionText by remember { mutableStateOf("") }

    // Synchronize yesterday/today's existing journal reflection to allow quick edits
    LaunchedEffect(todayReflection) {
        todayReflection?.let {
            selectedMoodIndex = it.moodIndex
            reflectionText = it.text
        } ?: run {
            selectedMoodIndex = 2
            reflectionText = ""
        }
    }

    val moodOptions = remember {
        listOf(
            MoodOption("😞", "Low", "उदासीन", "कमी", Icons.Default.SentimentVeryDissatisfied),
            MoodOption("😐", "Okay", "ठीक है", "ठीक", Icons.Default.SentimentNeutral),
            MoodOption("🙂", "Good", "अच्छा", "चांगले", Icons.Default.SentimentSatisfied),
            MoodOption("🤩", "Epic", "शानदार", "खूप छान", Icons.Default.SentimentVerySatisfied)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        contentPadding = PaddingValues(top = 16.dp, bottom = Responsive.h(12f))
    ) {
        // Today's reflection block card
        item {
            com.example.shared.widgets.StreaklyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                padding = 14.dp,
                borderRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = getLabel("How is your day going?", "आज का दिन कैसा रहा?", "आजचा दिवस कसा होता?"),
                        style = AppTextStyles.sectionHeader(AppColors.textPrimary),
                        color = accentColor
                    )

                    // Mood selector buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        moodOptions.forEachIndexed { idx, mood ->
                            val isSelected = selectedMoodIndex == idx
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) accentColor else AppColors.border,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedMoodIndex = idx }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = mood.icon,
                                    contentDescription = mood.en,
                                    tint = if (isSelected) accentColor else AppColors.textSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = getLabel(mood.en, mood.hi, mood.mr),
                                    style = AppTextStyles.hint(
                                        if (isSelected) accentColor else AppColors.textSecondary
                                    ).copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Journal textbox paragraph entry
                    OutlinedTextField(
                        value = reflectionText,
                        onValueChange = { reflectionText = it },
                        textStyle = LocalTextStyle.current.copy(fontSize = Responsive.fp(14f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("reflection_input_field"),
                        placeholder = {
                            Text(
                                text = getLabel(
                                    "Write down lessons learned or wins...",
                                    "आज सीखे गए सबक़ या जीत लिखें...",
                                    "आज शिकलेल्या गोष्टी किंवा यश नोंदवा..."
                                ),
                                style = AppTextStyles.bodyMedium.copy(fontSize = Responsive.fp(14f)),
                                color = AppColors.textHint
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = AppColors.border,
                            focusedContainerColor = AppColors.bgPrimary,
                            unfocusedContainerColor = AppColors.bgPrimary
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    // Save reflection submit CTA
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val selectedEmoji = moodOptions[selectedMoodIndex].emoji
                            reflectionProvider.saveReflection(
                                moodIndex = selectedMoodIndex,
                                moodEmoji = selectedEmoji,
                                text = reflectionText
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Responsive.sp(46f))
                            .testTag("save_reflection_button")
                    ) {
                        Text(
                            text = getLabel("Save Reflection", "अनुभव रिकॉर्ड करें", "अनुभव जतन करा"),
                            style = AppTextStyles.actionButton.copy(fontSize = Responsive.fp(14f)),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Section header historical journal Feed
        item {
            SectionHeader(
                title = getLabel("Journal Feed", "पिछली यादें", "मागील नोंदी"),
                subtitle = getLabel("Your History", "इतिहास", "इतिहास")
            )
        }

        if (reflectionsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getLabel("Your recorded journals will appear here.", "आपके जर्नल इतिहास यहाँ दिखाई देंगे।", "तुमचा इतिहास येथे दिसेल."),
                        style = AppTextStyles.bodyMedium,
                        color = AppColors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(reflectionsList, key = { it.id }) { journal ->
                JournalItemRow(
                    journal = journal,
                    language = language,
                    accentColor = accentColor,
                    onDelete = { reflectionProvider.deleteReflection(journal.id) }
                )
            }
        }
    }
}

@Composable
fun JournalItemRow(
    journal: ReflectionModel,
    language: String,
    accentColor: Color,
    onDelete: () -> Unit
) {
    com.example.shared.widgets.StreaklyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("journal_card_${journal.id}"),
        padding = 12.dp,
        borderRadius = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = getMoodIcon(journal.moodEmoji),
                        contentDescription = "Mood icon status review",
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        val fmt = SimpleDateFormat("EEEE, dd MMM yyyy", Locale(language))
                        val calendar = Calendar.getInstance().apply { time = Date(journal.createdAt) }
                        Text(
                            text = fmt.format(calendar.time),
                            style = AppTextStyles.cardTitle(AppColors.textPrimary).copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = DateUtils.getFormattedDate(journal.dateKey, language),
                            style = AppTextStyles.hint(AppColors.textSecondary)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_journal_button_${journal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = AppColors.red.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (journal.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = journal.text,
                    style = AppTextStyles.label(AppColors.textPrimary),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private data class MoodOption(
    val emoji: String,
    val en: String,
    val hi: String,
    val mr: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun getMoodIcon(emoji: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (emoji) {
        "😞" -> Icons.Default.SentimentVeryDissatisfied
        "😐" -> Icons.Default.SentimentNeutral
        "🙂" -> Icons.Default.SentimentSatisfied
        else -> Icons.Default.SentimentVerySatisfied
    }
}
