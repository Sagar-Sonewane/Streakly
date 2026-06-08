package com.example.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.core.theme.StreaklyTheme
import com.example.providers.Providers
import com.example.shared.widgets.MilestonePopup
import com.example.shared.widgets.AppHeader
import com.example.shared.widgets.NameInputDialog
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HeatmapScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.ReflectScreen
import com.example.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

import com.example.core.utils.Responsive
import com.example.core.utils.Responsive.Init

import androidx.navigation.navigation
import com.example.ui.screens.MilestoneCelebrationScreen
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import com.example.ui.screens.OnboardingSplashScreen
import com.example.ui.screens.OnboardingWelcomeSlides
import com.example.ui.screens.OnboardingUsernameSetup
import com.example.ui.screens.OnboardingNotificationPermission
import com.example.ui.screens.OnboardingSetAlertTime
import com.example.ui.screens.OnboardingFirstHabitSetup
import com.example.ui.screens.OnboardingYouAreAllSet

@OptIn(ExperimentalAnimationApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "onboarding"
) {
    Responsive.Init()
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("streakly_prefs", Context.MODE_PRIVATE)
    }
    var userName by remember {
        mutableStateOf(sharedPrefs.getString("user_name", "") ?: "")
    }

    val settingsProvider = Providers.getSettings()
    val streakProvider = Providers.getStreak()

    val settingsState by settingsProvider.settingsState.collectAsState()
    val milestoneToClaim by streakProvider.newlyCrossedMilestone.collectAsState()
    val streakState by streakProvider.streakState.collectAsState()

    val accentColorIndex = settingsState.accentColorIndex
    val themeModeIndex = settingsState.themeModeIndex
    val currentLanguage = settingsState.language

    // Play milestone fanfare sound in response to unlocking
    LaunchedEffect(milestoneToClaim) {
        // Audio and haptics handled inside MilestoneCelebrationScreen
    }

    // Dynamic label helper
    val getLabel = { en: String, hi: String, mr: String ->
        when (currentLanguage) {
            "hi" -> hi
            "mr" -> mr
            else -> en
        }
    }

    StreaklyTheme(accentColorIndex = accentColorIndex, themeModeIndex = themeModeIndex) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: "splash"
        var showNotificationSheet by remember { mutableStateOf(false) }

        // Render milestone completion overlay if user has unlocked a new milestone
        milestoneToClaim?.let { milestone ->
            MilestoneCelebrationScreen(
                milestone = milestone,
                onDismiss = { streakProvider.claimMilestone(milestone) }
            )
        }

        if (showNotificationSheet) {
            com.example.ui.screens.NotificationSheet(
                onDismissRequest = { showNotificationSheet = false }
            )
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.bgPrimary)
        ) {
            navigation(
                startDestination = "onboarding_splash",
                route = "onboarding"
            ) {
                composable(
                    route = "onboarding_splash",
                    exitTransition = { fadeOut(animationSpec = tween(400)) }
                ) {
                    OnboardingSplashScreen(
                        onNavigateNext = {
                            navController.navigate("onboarding_welcome") {
                                popUpTo("onboarding_splash") { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "onboarding_welcome",
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                ) {
                    OnboardingWelcomeSlides(
                        onNavigateNext = {
                            navController.navigate("onboarding_username")
                        },
                        onSkip = {
                            navController.navigate("onboarding_username")
                        }
                    )
                }

                composable(
                    route = "onboarding_username",
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                ) {
                    OnboardingUsernameSetup(
                        onNavigateNext = {
                            navController.navigate("onboarding_notifications")
                        }
                    )
                }

                composable(
                    route = "onboarding_notifications",
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                ) {
                    OnboardingNotificationPermission(
                        onNavigateNext = {
                            navController.navigate("onboarding_alert_time")
                        }
                    )
                }

                composable(
                    route = "onboarding_alert_time",
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                ) {
                    OnboardingSetAlertTime(
                        onNavigateNext = {
                            navController.navigate("onboarding_habit_setup")
                        }
                    )
                }

                composable(
                    route = "onboarding_habit_setup",
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                ) {
                    OnboardingFirstHabitSetup(
                        onNavigateNext = {
                            navController.navigate("onboarding_all_set")
                        }
                    )
                }

                composable(
                    route = "onboarding_all_set",
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    exitTransition = { fadeOut(animationSpec = tween(400)) }
                ) {
                    OnboardingYouAreAllSet(
                        onNavigateNext = {
                            userName = sharedPrefs.getString("user_name", "") ?: ""
                            navController.navigate("main") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable("splash") {
                com.example.ui.screens.SplashScreen(
                    accentColorIndex = accentColorIndex,
                    onNavigateToHome = {
                        navController.navigate("main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            composable("main") {
                val pagerState = rememberPagerState(initialPage = 0) { 5 }
                val coroutineScope = rememberCoroutineScope()

                // Intercept back presses to move back to the Home tab (page 0) before exiting the app
                BackHandler(enabled = pagerState.currentPage != 0) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppHeader(
                            currentIndex = pagerState.currentPage,
                            currentStreak = streakState.currentStreak,
                            accentColorIndex = accentColorIndex,
                            notificationsEnabled = settingsState.notificationsEnabled,
                            onNotificationsTap = {
                                showNotificationSheet = true
                            },
                            language = currentLanguage,
                            userName = userName,
                            onStreakBadgeTap = {
                                if (pagerState.currentPage != 0) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(0)
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .background(AppColors.bgPrimary)
                                .navigationBarsPadding()
                        ) {
                            // Accent border line separating screen content from navigation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.8.dp)
                                    .background(AppColors.border)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Responsive.sp(72f))
                                    .background(AppColors.bgSecondary)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Screen.items.forEachIndexed { index, screen ->
                                    val isSelected = pagerState.currentPage == index
                                    
                                    val scale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.05f else 1.0f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )

                                    val activeAccent = AppColors.accentColorOptions[accentColorIndex]
                                    val contentColor = if (isSelected) activeAccent else AppColors.textSecondary
                                    val backgroundColor = if (isSelected) activeAccent.copy(alpha = 0.15f) else Color.Transparent

                                    Box(
                                        modifier = Modifier
                                            .graphicsLayer(scaleX = scale, scaleY = scale)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(backgroundColor)
                                            .clickable {
                                                if (pagerState.currentPage != index) {
                                                    com.example.core.utils.SoundService.playTap()
                                                    com.example.core.utils.HapticService.selectionClick()
                                                    coroutineScope.launch {
                                                        pagerState.animateScrollToPage(index)
                                                    }
                                                }
                                            }
                                            .padding(horizontal = if (isSelected) 14.dp else 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.getTitle(currentLanguage),
                                                modifier = Modifier.size(Responsive.sp(20f)),
                                                tint = contentColor
                                            )
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = screen.getTitle(currentLanguage),
                                                    style = AppTextStyles.label(contentColor).copy(
                                                        fontSize = Responsive.fp(12f),
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.bgPrimary)
                            .padding(innerPadding)
                    ) { page ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .widthIn(max = 650.dp)
                                    .fillMaxWidth()
                            ) {
                                when (page) {
                                    0 -> HomeScreen()
                                    1 -> HeatmapScreen()
                                    2 -> StatsScreen()
                                    3 -> ReflectScreen()
                                    4 -> SettingsScreen(
                                        userName = userName,
                                        onUserNameChanged = { newName ->
                                            userName = newName
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
                .background(AppColors.bgCard)
                .padding(32.dp)
        ) {
            Text(
                text = title,
                style = AppTextStyles.headingLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = description,
                style = AppTextStyles.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun SettingsPreviewScreen(
    currentAccentIdx: Int,
    onAccentSelected: (Int) -> Unit,
    currentLang: String,
    onLangSelected: (String) -> Unit,
    getLabel: (String, String, String) -> String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bgPrimary)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
                .background(AppColors.bgCard)
                .padding(24.dp)
        ) {
            Text(
                text = getLabel("Appearance & Settings", "सजावट और सेटिंग्स", "सजावट आणि सेटिंग्ज"),
                style = AppTextStyles.headingMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = getLabel("Accent Color Theme", "मुख्य रंग बदलें", "मुख्य रंग बदला"),
                style = AppTextStyles.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Accent pickers
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(bottom = 24.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                AppColors.accentColorOptions.forEachIndexed { index, color ->
                    val isSelected = index == currentAccentIdx
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .height(40.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color.White else AppColors.border,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(color)
                            .clickable { onAccentSelected(index) }
                    )
                }
            }

            Text(
                text = getLabel("App Language", "भाषा बदलें", "भाषा बदला"),
                style = AppTextStyles.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Language picker row
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    Triple("en", "English", "EN"),
                    Triple("hi", "हिंदी", "HI"),
                    Triple("mr", "मराठी", "MR")
                ).forEach { (code, name, abbr) ->
                    val isSelected = code == currentLang
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AppColors.accentColorOptions[currentAccentIdx] else AppColors.border,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(if (isSelected) AppColors.bgTertiary else AppColors.bgSecondary)
                            .clickable { onLangSelected(code) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            style = AppTextStyles.titleMedium.copy(
                                color = if (isSelected) AppColors.accentColorOptions[currentAccentIdx] else AppColors.textPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}
