package com.example.ui.screens

import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.providers.Providers
import com.example.shared.widgets.TimePickerSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// Particle class for Screen 7 Confetti celebration
private class ConfettiParticle(
    var x: Float,
    var y: Float,
    val color: Color,
    val radius: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1.0f
)

@Composable
fun OnboardingProgressHeader(progress: Float, accentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accentColor,
            trackColor = AppColors.border
        )
    }
}

// Screen 1: Splash Screen
@Composable
fun OnboardingSplashScreen(
    onNavigateNext: () -> Unit
) {
    val settingsProvider = Providers.getSettings()
    val settingsState by settingsProvider.settingsState.collectAsState()
    val accentColor = AppColors.accentColorOptions.getOrNull(settingsState.accentColorIndex) ?: AppColors.accentOrange
    
    var startAnim by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }
    
    val flameScale by animateFloatAsState(
        targetValue = if (startAnim) 1.2f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    LaunchedEffect(Unit) {
        startAnim = true
        delay(400)
        taglineVisible = true
        delay(1400) // 400 + 1400 = 1800ms
        onNavigateNext()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer(scaleX = flameScale, scaleY = flameScale)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AnimatedVisibility(
                visible = taglineVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Streakly",
                        style = AppTextStyles.screenTitle(AppColors.textPrimary).copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Build. Track. Win. 🔥",
                        style = AppTextStyles.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textSecondary
                        )
                    )
                }
            }
        }
    }
}

// Screen 2: Welcome Slides
@Composable
fun OnboardingWelcomeSlides(
    onNavigateNext: () -> Unit,
    onSkip: () -> Unit
) {
    val settingsProvider = Providers.getSettings()
    val settingsState by settingsProvider.settingsState.collectAsState()
    val accentColor = AppColors.accentColorOptions.getOrNull(settingsState.accentColorIndex) ?: AppColors.accentOrange
    
    val getLabel = { en: String, hi: String, mr: String ->
        when (settingsState.language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }
    
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    val slides = listOf(
        Triple(
            getLabel("Define Your Goals", "अपने लक्ष्य निर्धारित करें", "आपली ध्येये निश्चित करा"),
            getLabel(
                "Set clear, actionable habits that fit your daily routine.",
                "स्पष्ट और व्यावहारिक आदतें निर्धारित करें जो आपकी दैनिक दिनचर्या में फिट हों।",
                "आपल्या दैनंदिन दिनचर्येत बसतील अशा स्पष्ट आणि व्यावहारिक सवयी ठरवा."
            ),
            "🎯"
        ),
        Triple(
            getLabel("Maintain Consistency", "निरंतरता बनाए रखें", "सातत्य राखा"),
            getLabel(
                "Track your streaks day by day and feel the momentum build.",
                "दिन-प्रतिदिन अपनी लकीरों को ट्रैक करें और गति के निर्माण को महसूस करें।",
                "दिवसेंदिवस आपल्या रेषा ट्रॅक करा आणि गतीची भावना अनुभवा."
            ),
            "🔥"
        ),
        Triple(
            getLabel("Unlock Milestones", "मील के पत्थर अनलॉक करें", "टप्पे अनलॉक करा"),
            getLabel(
                "Earn rewards and celebrate consistency milestones along the way.",
                "रास्ते में पुरस्कार अर्जित करें और निरंतरता के मील के पत्थरों का जश्न मनाएं।",
                "मार्गात बक्षिसे मिळवा आणि सातत्याचे टप्पे साजरे करा."
            ),
            "🏆"
        )
    )
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        containerColor = AppColors.bgPrimary,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                        val color = if (isSelected) accentColor else AppColors.border
                        Box(
                            modifier = Modifier
                                .size(width = width, height = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                }
                
                // Actions
                if (pagerState.currentPage < 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getLabel("Skip", "छोड़ें", "वगळा"),
                            style = AppTextStyles.body(AppColors.textSecondary).copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .clickable {
                                    com.example.core.utils.SoundService.playTap()
                                    com.example.core.utils.HapticService.selectionClick()
                                    onSkip()
                                }
                                .padding(12.dp)
                        )
                        
                        Button(
                            onClick = {
                                com.example.core.utils.SoundService.playTap()
                                com.example.core.utils.HapticService.selectionClick()
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = getLabel("Next", "अगला", "पुढील"),
                                style = AppTextStyles.actionButton.copy(color = Color.Black)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.confirm()
                            onNavigateNext()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = getLabel("Get Started 🔥", "शुरू करें 🔥", "सुरू करा 🔥"),
                            style = AppTextStyles.actionButton.copy(color = Color.Black, fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val (title, subtitle, emoji) = slides[page]
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Parallax Emoji Container
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            // Translate emoji faster than layout to create parallax
                            translationX = pageOffset * size.width * 0.4f
                            alpha = 1f - (pageOffset.absoluteValue * 1.5f).coerceIn(0f, 1f)
                        }
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.08f))
                        .border(1.dp, accentColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 52.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    text = title,
                    style = AppTextStyles.screenTitle(AppColors.textPrimary).copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = subtitle,
                    style = AppTextStyles.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                )
            }
        }
    }
}

// Screen 3: Username Setup
@Composable
fun OnboardingUsernameSetup(
    onNavigateNext: () -> Unit
) {
    val settingsProvider = Providers.getSettings()
    val settingsState by settingsProvider.settingsState.collectAsState()
    val accentColor = AppColors.accentColorOptions.getOrNull(settingsState.accentColorIndex) ?: AppColors.accentOrange
    
    val getLabel = { en: String, hi: String, mr: String ->
        when (settingsState.language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }
    
    val context = LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("streakly_prefs", Context.MODE_PRIVATE) }
    var nameInput by remember { mutableStateOf("") }
    
    // Wave animation
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        containerColor = AppColors.bgPrimary,
        topBar = { OnboardingProgressHeader(progress = 0.2f, accentColor = accentColor) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        val trimmed = nameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.confirm()
                            sharedPrefs.edit().putString("user_name", trimmed).apply()
                            onNavigateNext()
                        }
                    },
                    enabled = nameInput.trim().isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.Black,
                        disabledContainerColor = AppColors.border,
                        disabledContentColor = AppColors.textHint
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = getLabel("Let's Go! 🔥", "चलें! 🔥", "चला जाऊया! 🔥"),
                        style = AppTextStyles.actionButton.copy(color = if (nameInput.trim().isNotEmpty()) Color.Black else AppColors.textHint, fontSize = 16.sp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.graphicsLayer { rotationZ = rotation }
            ) {
                Text("👋", fontSize = 64.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = getLabel("What should we call you?", "हमें आपको क्या बुलाना चाहिए?", "आम्ही तुम्हाला काय म्हणावे?"),
                style = AppTextStyles.screenTitle(AppColors.textPrimary).copy(textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = getLabel("Your name will be shown on the home screen greetings.", "आपका नाम होम स्क्रीन की शुभकामनाओं पर दिखाया जाएगा।", "तुमचे नाव मुख्य स्क्रीनच्या शुभेच्छांवर दाखवले जाईल."),
                style = AppTextStyles.bodyMedium.copy(textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    if (it.length <= 20) {
                        nameInput = it
                    }
                },
                placeholder = {
                    Text(
                        text = getLabel("Enter your name", "अपना नाम दर्ज करें", "तुमचे नाव प्रविष्ट करा"),
                        color = AppColors.textHint
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val trimmed = nameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.confirm()
                            sharedPrefs.edit().putString("user_name", trimmed).apply()
                            onNavigateNext()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = AppColors.border,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = AppColors.textSecondary,
                    cursorColor = accentColor,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                supportingText = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        val count = nameInput.length
                        val counterColor = if (count >= 18) accentColor else AppColors.textHint
                        Text(
                            text = "$count / 20",
                            style = AppTextStyles.caption.copy(fontSize = 11.sp),
                            color = counterColor
                        )
                    }
                }
            )
        }
    }
}

// Screen 4: Notification Permission Setup
@Composable
fun OnboardingNotificationPermission(
    onNavigateNext: () -> Unit
) {
    val settingsProvider = Providers.getSettings()
    val settingsState by settingsProvider.settingsState.collectAsState()
    val accentColor = AppColors.accentColorOptions.getOrNull(settingsState.accentColorIndex) ?: AppColors.accentOrange
    
    val getLabel = { en: String, hi: String, mr: String ->
        when (settingsState.language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        settingsProvider.updateNotificationsEnabled(isGranted)
        if (isGranted) {
            com.example.StreaklyApp.instance.notificationService.scheduleDailyReminder(
                settingsState.reminderHour,
                settingsState.reminderMinute
            )
        }
        onNavigateNext()
    }
    
    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        containerColor = AppColors.bgPrimary,
        topBar = { OnboardingProgressHeader(progress = 0.4f, accentColor = accentColor) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.selectionClick()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            settingsProvider.updateNotificationsEnabled(true)
                            com.example.StreaklyApp.instance.notificationService.scheduleDailyReminder(
                                settingsState.reminderHour,
                                settingsState.reminderMinute
                            )
                            onNavigateNext()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = getLabel("Allow Notifications 🔔", "सूचनाएं अनुमति दें 🔔", "सूचनांना परवानगी द्या 🔔"),
                        style = AppTextStyles.actionButton.copy(color = Color.Black, fontSize = 16.sp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = getLabel("Maybe Later", "शायद बाद में", "कदाचित नंतर"),
                    style = AppTextStyles.body(AppColors.textSecondary).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            settingsProvider.updateNotificationsEnabled(false)
                            onNavigateNext()
                        }
                        .padding(12.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(AppColors.bgTertiary)
                        .border(1.dp, AppColors.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = getLabel("Stay on Track", "ट्रैक पर रहें", "ट्रॅकवर रहा"),
                style = AppTextStyles.screenTitle(AppColors.textPrimary).copy(textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = getLabel("Streakly works best when it can remind you to log your tasks and protect your progress.", "जब Streakly आपको अपने कार्यों को लॉग करने और अपनी प्रगति की रक्षा करने की याद दिला सकता है, तो यह सबसे अच्छा काम करता है।", "जेव्हा Streakly तुम्हाला तुमचे कार्य लॉग इन करण्याची आणि तुमच्या प्रगतीचे रक्षण करण्याची आठवण करून देऊ शकते तेव्हा ते सर्वोत्तम कार्य करते."),
                style = AppTextStyles.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                ),
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
    }
}

// Screen 5: Alert Time Selector
@Composable
fun OnboardingSetAlertTime(
    onNavigateNext: () -> Unit
) {
    val settingsProvider = Providers.getSettings()
    val settingsState by settingsProvider.settingsState.collectAsState()
    val accentColor = AppColors.accentColorOptions.getOrNull(settingsState.accentColorIndex) ?: AppColors.accentOrange
    
    val getLabel = { en: String, hi: String, mr: String ->
        when (settingsState.language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }
    
    class Preset(
        val title: String,
        val timeLabel: String,
        val subtitle: String,
        val emoji: String,
        val hour: Int,
        val minute: Int,
        val isAM: Boolean
    )
    
    val presets = remember(settingsState.language) {
        listOf(
            Preset(
                title = getLabel("Early Bird", "अर्ली बर्ड", "अर्ली बर्ड"),
                timeLabel = "6:00 AM",
                subtitle = getLabel("Rise and conquer", "जल्दी उठें और जीतें", "लवकर उठा आणि जिंका"),
                emoji = "🌅",
                hour = 6,
                minute = 0,
                isAM = true
            ),
            Preset(
                title = getLabel("Morning", "सुबह", "सकाळ"),
                timeLabel = "8:00 AM",
                subtitle = getLabel("Start strong", "मजबूत शुरुआत करें", "खंबीर सुरुवात करा"),
                emoji = "☀️",
                hour = 8,
                minute = 0,
                isAM = true
            ),
            Preset(
                title = getLabel("Evening", "शाम", "संध्याकाळ"),
                timeLabel = "6:00 PM",
                subtitle = getLabel("Wind down right", "सही से दिन समाप्त करें", "योग्य प्रकारे दिवस संपवा"),
                emoji = "🌆",
                hour = 6,
                minute = 0,
                isAM = false
            ),
            Preset(
                title = getLabel("Night Owl", "नाइट आउल", "नाईट आऊल"),
                timeLabel = "9:00 PM",
                subtitle = getLabel("Reflect and rest", "विचार करें और आराम करें", "विचार करा आणि विश्रांती घ्या"),
                emoji = "🌙",
                hour = 9,
                minute = 0,
                isAM = false
            )
        )
    }
    
    var selectedIndex by remember { mutableStateOf(1) }
    val selectedPreset = presets[selectedIndex]
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        containerColor = AppColors.bgPrimary,
        topBar = { OnboardingProgressHeader(progress = 0.6f, accentColor = accentColor) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Button(
                    onClick = {
                        val hr24 = if (selectedPreset.isAM) {
                            if (selectedPreset.hour == 12) 0 else selectedPreset.hour
                        } else {
                            if (selectedPreset.hour == 12) 12 else selectedPreset.hour + 12
                        }
                        com.example.core.utils.SoundService.playTap()
                        com.example.core.utils.HapticService.confirm()
                        settingsProvider.updateReminderTime(hr24, selectedPreset.minute)
                        com.example.StreaklyApp.instance.notificationService.scheduleDailyReminder(hr24, selectedPreset.minute)
                        onNavigateNext()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = getLabel("Save & Continue", "सहेजें और जारी रखें", "जतन करा आणि पुढे चला"),
                        style = AppTextStyles.actionButton.copy(color = Color.Black, fontSize = 16.sp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = getLabel("Set Daily Reminder Time", "दैनिक अनुस्मारक समय निर्धारित करें", "दैनिक स्मरणपत्र वेळ सेट करा"),
                    style = AppTextStyles.screenTitle(AppColors.textPrimary).copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = getLabel("You can always change this later in Settings", "आप इसे बाद में सेटिंग्स में कभी भी बदल सकते हैं", "आपण नंतर सेटिंग्जमध्ये हे कधीही बदलू शकता"),
                    style = AppTextStyles.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        color = AppColors.textSecondary
                    ),
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                presets.forEachIndexed { index, preset ->
                    val isSelected = index == selectedIndex
                    
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.0f else 0.96f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                    
                    val cardBgColor by animateColorAsState(
                        targetValue = if (isSelected) AppColors.bgCard else AppColors.bgSecondary,
                        animationSpec = tween(durationMillis = 200)
                    )
                    
                    Row(
                        modifier = Modifier
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBgColor)
                            .clickable {
                                if (!isSelected) {
                                    com.example.core.utils.SoundService.playTap()
                                    com.example.core.utils.HapticService.selectionClick()
                                    selectedIndex = index
                                }
                            }
                            .drawBehind {
                                if (isSelected) {
                                    val strokeWidth = 4.dp.toPx()
                                    drawLine(
                                        color = accentColor,
                                        start = Offset(strokeWidth / 2, 0f),
                                        end = Offset(strokeWidth / 2, size.height),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            }
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) accentColor.copy(alpha = 0.15f) else AppColors.bgTertiary)
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = AppColors.border,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(accentColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Text(text = preset.emoji, fontSize = 22.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = preset.title,
                                style = AppTextStyles.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = preset.subtitle,
                                style = AppTextStyles.bodySmall.copy(
                                    color = AppColors.textSecondary
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = preset.timeLabel,
                            style = AppTextStyles.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) accentColor else AppColors.textPrimary
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Screen 6: First Habit Setup
@Composable
fun OnboardingFirstHabitSetup(
    onNavigateNext: () -> Unit
) {
    val settingsProvider = Providers.getSettings()
    val settingsState by settingsProvider.settingsState.collectAsState()
    val accentColor = AppColors.accentColorOptions.getOrNull(settingsState.accentColorIndex) ?: AppColors.accentOrange
    
    val getLabel = { en: String, hi: String, mr: String ->
        when (settingsState.language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }
    
    val taskProvider = Providers.getTask()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var habitTitle by remember { mutableStateOf("") }
    var selectedFreqIndex by remember { mutableStateOf(0) } // 0: Daily, 1: Weekdays, 2: Weekends
    
    val focusRequester = remember { FocusRequester() }
    
    val presets = listOf(
        Pair("🏋️ Gym", getLabel("Gym", "जिम", "व्यायामशाळा")),
        Pair("📚 Read", getLabel("Read", "पढ़ें", "वाचन")),
        Pair("🧘 Meditate", getLabel("Meditate", "ध्यान", "ध्यान")),
        Pair("💧 Water", getLabel("Water", "पानी", "पाणी")),
        Pair("🚶 Walk", getLabel("Walk", "टहलें", "चालणे")),
        Pair("🍎 Eat Healthy", getLabel("Eat Healthy", "स्वस्थ खाएं", "निरोगी आहार"))
    )
    
    val onPresetClick: (String) -> Unit = { cleanName ->
        scope.launch {
            habitTitle = ""
            for (i in 1..cleanName.length) {
                habitTitle = cleanName.substring(0, i)
                com.example.core.utils.SoundService.playTap()
                delay(40)
            }
        }
    }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        containerColor = AppColors.bgPrimary,
        topBar = { OnboardingProgressHeader(progress = 0.8f, accentColor = accentColor) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        val title = habitTitle.trim()
                        if (title.isNotEmpty()) {
                            keyboardController?.hide()
                            com.example.core.utils.SoundService.playAdd()
                            com.example.core.utils.HapticService.confirm()
                            
                            val frequency = if (selectedFreqIndex == 0) "daily" else "weekly"
                            val weekDaysRaw = when (selectedFreqIndex) {
                                1 -> "1,2,3,4,5"
                                2 -> "6,7"
                                else -> ""
                            }
                            
                            taskProvider.addTask(
                                title = title,
                                description = null,
                                timeLabel = null,
                                colorIndex = (settingsState.accentColorIndex + 1) % 6,
                                frequency = frequency,
                                weekDaysRaw = weekDaysRaw,
                                importance = "regular"
                            )
                            onNavigateNext()
                        }
                    },
                    enabled = habitTitle.trim().isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.Black,
                        disabledContainerColor = AppColors.border,
                        disabledContentColor = AppColors.textHint
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = getLabel("Add Habit & Continue", "आदत जोड़ें और जारी रखें", "सवय जोडा आणि पुढे जा"),
                        style = AppTextStyles.actionButton.copy(color = if (habitTitle.trim().isNotEmpty()) Color.Black else AppColors.textHint, fontSize = 16.sp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = getLabel("Skip for now", "अभी छोड़ें", "सध्या वगळा"),
                    style = AppTextStyles.body(AppColors.textSecondary).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable {
                            com.example.core.utils.SoundService.playTap()
                            com.example.core.utils.HapticService.selectionClick()
                            keyboardController?.hide()
                            onNavigateNext()
                        }
                        .padding(12.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getLabel("Create Your First Habit", "अपनी पहली आदत बनाएं", "तुमची पहिली सवय तयार करा"),
                style = AppTextStyles.screenTitle(AppColors.textPrimary).copy(textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = getLabel("What is one task you want to complete consistently?", "वह कौन सा कार्य है जिसे आप निरंतर पूरा करना चाहते हैं?", "असे कोणते काम आहे जे तुम्हाला सातत्याने पूर्ण करायचे आहे?"),
                style = AppTextStyles.bodyMedium.copy(textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Preset Habit Grid (2 rows of 3 chips)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(3).forEach { (display, cleanName) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.bgSecondary)
                                .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
                                .clickable { onPresetClick(cleanName) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = display,
                                style = AppTextStyles.caption.copy(
                                    fontSize = 12.sp,
                                    color = AppColors.textPrimary
                                )
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.takeLast(3).forEach { (display, cleanName) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.bgSecondary)
                                .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
                                .clickable { onPresetClick(cleanName) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = display,
                                style = AppTextStyles.caption.copy(
                                    fontSize = 12.sp,
                                    color = AppColors.textPrimary
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            OutlinedTextField(
                value = habitTitle,
                onValueChange = {
                    if (it.length <= 30) {
                        habitTitle = it
                    }
                },
                placeholder = {
                    Text(
                        text = getLabel("e.g., Read a book", "उदा. किताब पढ़ें", "उदा. पुस्तक वाचा"),
                        color = AppColors.textHint
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val title = habitTitle.trim()
                        if (title.isNotEmpty()) {
                            keyboardController?.hide()
                            com.example.core.utils.SoundService.playAdd()
                            com.example.core.utils.HapticService.confirm()
                            
                            val frequency = if (selectedFreqIndex == 0) "daily" else "weekly"
                            val weekDaysRaw = when (selectedFreqIndex) {
                                1 -> "1,2,3,4,5"
                                2 -> "6,7"
                                else -> ""
                            }
                            
                            taskProvider.addTask(
                                title = title,
                                description = null,
                                timeLabel = null,
                                colorIndex = (settingsState.accentColorIndex + 1) % 6,
                                frequency = frequency,
                                weekDaysRaw = weekDaysRaw,
                                importance = "regular"
                            )
                            onNavigateNext()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = AppColors.border,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = AppColors.textSecondary,
                    cursorColor = accentColor,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Frequency segmented capsule picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.bgSecondary)
                    .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val freqOptions = listOf(
                    getLabel("Daily", "दैनिक", "रोज"),
                    getLabel("Weekdays", "कार्यदिवस", "कामाचे दिवस"),
                    getLabel("Weekends", "सप्ताहांत", "शनि-रवि")
                )
                
                freqOptions.forEachIndexed { index, option ->
                    val isSelected = selectedFreqIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accentColor else Color.Transparent)
                            .clickable {
                                com.example.core.utils.SoundService.playTap()
                                com.example.core.utils.HapticService.selectionClick()
                                selectedFreqIndex = index
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = AppTextStyles.caption.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else AppColors.textSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

// Screen 7: Celebration / You're All Set
@Composable
fun OnboardingYouAreAllSet(
    onNavigateNext: () -> Unit
) {
    val settingsProvider = Providers.getSettings()
    val settingsState by settingsProvider.settingsState.collectAsState()
    val accentColor = AppColors.accentColorOptions.getOrNull(settingsState.accentColorIndex) ?: AppColors.accentOrange
    
    val getLabel = { en: String, hi: String, mr: String ->
        when (settingsState.language) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }
    
    val context = LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("streakly_prefs", Context.MODE_PRIVATE) }
    val username = remember { sharedPrefs.getString("user_name", "") ?: "" }
    
    // Spring pulse flame animation
    var startAnim by remember { mutableStateOf(false) }
    val flameScale by animateFloatAsState(
        targetValue = if (startAnim) 1.25f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // Confetti physics state
    var isAnimatingConfetti by remember { mutableStateOf(true) }
    val confettiStartTime = remember { System.currentTimeMillis() }
    var tick by remember { mutableStateOf(0) }
    
    val particles = remember {
        val random = java.util.Random()
        val colors = listOf(
            Color(0xFFFF6B35), Color(0xFF6C63FF), Color(0xFF00D4AA),
            Color(0xFF00C853), Color(0xFFFFAB00), Color(0xFFFF3D71),
            Color.Cyan, Color.Yellow, Color.Magenta
        )
        List(90) {
            ConfettiParticle(
                x = 0f,
                y = 0f,
                color = colors[random.nextInt(colors.size)],
                radius = random.nextFloat() * 12f + 6f,
                vx = (random.nextFloat() - 0.5f) * 32f,
                vy = -random.nextFloat() * 40f - 15f
            )
        }
    }
    
    LaunchedEffect(isAnimatingConfetti) {
        startAnim = true
        if (isAnimatingConfetti) {
            while (System.currentTimeMillis() - confettiStartTime < 2000) {
                withFrameMillis {
                    tick++
                }
            }
            isAnimatingConfetti = false
        }
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary),
        containerColor = AppColors.bgPrimary,
        topBar = { OnboardingProgressHeader(progress = 1.0f, accentColor = accentColor) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        com.example.core.utils.SoundService.playSuccess()
                        com.example.core.utils.HapticService.celebrate()
                        sharedPrefs.edit().putBoolean("onboarding_complete", true).apply()
                        onNavigateNext()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = getLabel("Start Streaking 🔥", "सफर शुरू करें 🔥", "प्रवास सुरू करा 🔥"),
                        style = AppTextStyles.actionButton.copy(color = Color.Black, fontSize = 16.sp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Confetti canvas overlay
            if (isAnimatingConfetti) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dummy = tick // Re-render triggers on tick updates
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val elapsed = System.currentTimeMillis() - confettiStartTime
                    val alphaProgress = (1f - (elapsed / 2000f)).coerceIn(0f, 1f)
                    
                    particles.forEach { p ->
                        if (p.x == 0f && p.y == 0f) {
                            p.x = canvasWidth / 2f
                            p.y = canvasHeight * 0.7f
                        }
                        
                        p.x += p.vx
                        p.y += p.vy
                        p.vy += 0.85f // gravity
                        p.vx *= 0.97f // drag
                        
                        drawCircle(
                            color = p.color.copy(alpha = alphaProgress),
                            radius = p.radius,
                            center = Offset(p.x, p.y)
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer(scaleX = flameScale, scaleY = flameScale)
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(60.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(36.dp))
                
                val titleGreeting = if (username.isNotEmpty()) {
                    getLabel("You're All Set, $username!", "आप तैयार हैं, $username!", "तुम्ही तयार आहात, $username!")
                } else {
                    getLabel("You're All Set!", "आप पूरी तरह तैयार हैं!", "तुम्ही सर्व सेट आहात!")
                }
                Text(
                    text = titleGreeting,
                    style = AppTextStyles.screenTitle(AppColors.textPrimary).copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = getLabel("Welcome to Streakly. Your consistency journey begins today. Let's make every day count!", "Streakly में आपका स्वागत है। आपकी निरंतरता की यात्रा आज से शुरू हो रही है। आइए हर दिन को खास बनाएं!", "Streakly मध्ये आपले स्वागत आहे. तुमचा सातत्याचा प्रवास आजपासून सुरू होत आहे. चला प्रत्येक दिवस यशस्वी करूया!"),
                    style = AppTextStyles.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
        }
    }
}
