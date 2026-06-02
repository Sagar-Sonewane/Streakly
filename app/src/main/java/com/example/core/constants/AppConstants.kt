package com.example.core.constants

object AppConstants {
    // Milestones list on which we trigger animated celebration popup
    val MILESTONES = listOf(3, 7, 10, 20, 50, 100)

    // Minimum completion rate to maintain/increment a streak (e.g. 80%)
    const val STREAK_INCREMENT_THRESHOLD = 0.80

    // Maximum completion rate to reset streak (less than 50% resets streak, 50% to 79% is the grace zone)
    const val STREAK_RESET_THRESHOLD = 0.50

    // Supported languages setup
    const val LANG_EN = "en"
    const val LANG_HI = "hi"
    const val LANG_MR = "mr"
}
