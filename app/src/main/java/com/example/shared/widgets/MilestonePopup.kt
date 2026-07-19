package com.example.shared.widgets

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import com.example.core.utils.CategoryUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles

@Composable
fun MilestonePopup(
    milestone: Int,
    language: String,
    onClaimClick: () -> Unit,
    accentColor: Color = AppColors.accentOrange,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Dynamic titles and subtitles per language and milestones
    val details = rememberMilestoneDetails(milestone, language)

    Dialog(
        onDismissRequest = { /* Must claim */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.bgSecondary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(accentColor, AppColors.border)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("milestone_popup_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Pulsating vector icon container
                    Icon(
                        imageVector = CategoryUtils.getIconForEmoji(details.emoji),
                        contentDescription = "Milestone Icon",
                        tint = accentColor,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(pulseScale)
                            .padding(bottom = 12.dp)
                    )

                    Text(
                        text = details.title,
                        style = AppTextStyles.headingLarge.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = details.daysLabel,
                        style = AppTextStyles.caption.copy(color = AppColors.textSecondary, letterSpacing = 2.sp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = details.description,
                        style = AppTextStyles.bodyMedium,
                        color = AppColors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Claim Badge Button
                    Button(
                        onClick = onClaimClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(accentColor, accentColor.copy(alpha = 0.8f))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("claim_badge_button")
                    ) {
                        Text(
                            text = details.btnText,
                            style = AppTextStyles.actionButton,
                            modifier = Modifier.padding(vertical = 14.dp)
                        )
                    }
                }
            }
        }
    }
}

private class MilestoneInfo(
    val emoji: String,
    val title: String,
    val daysLabel: String,
    val description: String,
    val btnText: String
)

@Composable
private fun rememberMilestoneDetails(milestone: Int, lang: String): MilestoneInfo {
    return when (lang) {
        "hi" -> {
            val emoji = when(milestone) {
                3 -> "🔥"
                7 -> "⚔️"
                10 -> "☄️"
                20 -> "🏆"
                50 -> "💎"
                else -> "👑"
            }
            val title = when(milestone) {
                3 -> "३-दिन की चिंगारी"
                7 -> "एक सप्ताह के योद्धा"
                10 -> "लपटों के उस्ताद"
                20 -> "निरंतरता के राजा"
                50 -> "असाधारण स्ट्रीक"
                else -> "महान सम्राट"
            }
            val days = "$milestone दिनों की स्ट्रीक पूरी!"
            val desc = when(milestone) {
                3 -> "आपने अनुशासन की चिंगारी सुलगाई है! इसे चालू रखें!"
                7 -> "अतुलनीय प्रयास देखा गया। पूरे एक सप्ताह का अनुशासन!"
                10 -> "आपकी आग अब पूरी तरह से फैल रही है। रुकना मना है!"
                20 -> "सच्चे चैंपियन की तरह २० दिन पूरे किये। अद्भुत!"
                50 -> "उत्कृष्टता का नया स्तर! आपके हौसले चट्टान की तरह हैं!"
                else -> "१०० दिन की बेमिसाल यात्रा! आप महानता की मिसाल हैं!"
            }
            MilestoneInfo(emoji, title, days, desc, "बैज प्राप्त करें")
        }
        "mr" -> {
            val emoji = when(milestone) {
                3 -> "🔥"
                7 -> "⚔️"
                10 -> "☄️"
                20 -> "🏆"
                50 -> "💎"
                else -> "👑"
            }
            val title = when(milestone) {
                3 -> "३-दिवसांची ठिणगी"
                7 -> "एक आठवड्याचा योद्धा"
                10 -> "ज्वालांचा स्वामी"
                20 -> "सातत्याचा विजेता"
                50 -> "उत्कृष्ट स्ट्रीक"
                else -> "महान राजा"
            }
            val days = "$milestone दिवसांची अखंड स्ट्रीक!"
            val desc = when(milestone) {
                3 -> "तुम्ही शिस्तीची ठिणगी पेटवली आहे! ती अशीच धगधगत ठेवा!"
                7 -> "प्रचंड चिकाटी! संपूर्ण एक आठवडा सलग यशाचा टप्पा गाठला!"
                10 -> "तुमची ज्वाला आता जोरात आहे. सलग दिवस करत रहा!"
                20 -> "२० दिवस पूर्ण! तुम्ही खरोखर सातत्याचे राजे आहात!"
                50 -> "अतुलनीय अनुशासन! तुम्ही नवीन यशाचे शिखर गाठले आहे!"
                else -> "१०० दिवस सलग! तुमची ही यात्रा ऐतिहासिक आणि प्रेरणादायी आहे!"
            }
            MilestoneInfo(emoji, title, days, desc, "बॅज मिळवा")
        }
        else -> {
            val emoji = when(milestone) {
                3 -> "🔥"
                7 -> "⚔️"
                10 -> "☄️"
                20 -> "🏆"
                50 -> "💎"
                else -> "👑"
            }
            val title = when(milestone) {
                3 -> "3-Day Spark"
                7 -> "One Week Warrior"
                10 -> "Flame Master"
                20 -> "Consistency Champion"
                50 -> "Elite Streak"
                else -> "Legendary Lord"
            }
            val days = "$milestone DAYS STREAK COMPLETED!"
            val desc = when(milestone) {
                3 -> "You sparked your discipline! Keep it hot and shining!"
                7 -> "Earned One Week Warrior badge. Incredible perseverance!"
                10 -> "Consistency flame is fully active. Feeling the raw discipline!"
                20 -> "You have conquered 20 full days. True consistency champion!"
                50 -> "Elite Streak badge earned. High tier discipline, masterclass!"
                else -> "100-Day Legendary stream unlocked! Absolutely historic achievement!"
            }
            MilestoneInfo(emoji, title, days, desc, "Claim Badge")
        }
    }
}
