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
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HeatmapScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.ReflectScreen
import com.example.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

import com.example.core.utils.Responsive
import com.example.core.utils.Responsive.Init

@OptIn(ExperimentalAnimationApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Responsive.Init()
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
        if (milestoneToClaim != null) {
            com.example.core.utils.SoundService.playMilestone()
            com.example.core.utils.HapticService.heavyImpact()
        }
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
            MilestonePopup(
                milestone = milestone,
                language = currentLanguage,
                onClaimClick = { streakProvider.claimMilestone(milestone) },
                accentColor = AppColors.accentColorOptions[accentColorIndex]
            )
        }

        if (showNotificationSheet) {
            com.example.ui.screens.NotificationSheet(
                onDismissRequest = { showNotificationSheet = false }
            )
        }

        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.bgPrimary)
        ) {
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

                            NavigationBar(
                                containerColor = AppColors.bgSecondary,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(Responsive.sp(62f))
                            ) {
                                Screen.items.forEachIndexed { index, screen ->
                                    val isSelected = pagerState.currentPage == index
                                    
                                    val scale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.15f else 1.0f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )

                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (pagerState.currentPage != index) {
                                                com.example.core.utils.SoundService.playTap()
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(index)
                                                }
                                            }
                                        },
                                        icon = {
                                            Box(
                                                modifier = Modifier
                                                    .graphicsLayer(scaleX = scale, scaleY = scale)
                                                    .background(
                                                        color = if (isSelected) AppColors.accentColorOptions[accentColorIndex].copy(alpha = 0.12f) else Color.Transparent,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(if (isSelected) 4.dp else 0.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                    contentDescription = screen.getTitle(currentLanguage),
                                                    modifier = Modifier.size(Responsive.sp(22f)),
                                                    tint = if (isSelected) AppColors.accentColorOptions[accentColorIndex] else AppColors.textSecondary
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = screen.getTitle(currentLanguage),
                                                style = AppTextStyles.caption.copy(
                                                    fontSize = Responsive.fp(10f),
                                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                                    color = if (isSelected) AppColors.accentColorOptions[accentColorIndex] else AppColors.textSecondary
                                                )
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = AppColors.accentColorOptions[accentColorIndex],
                                            unselectedIconColor = AppColors.textSecondary,
                                            selectedTextColor = AppColors.accentColorOptions[accentColorIndex],
                                            unselectedTextColor = AppColors.textSecondary,
                                            indicatorColor = Color.Transparent
                                        )
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
                                    4 -> SettingsScreen()
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
