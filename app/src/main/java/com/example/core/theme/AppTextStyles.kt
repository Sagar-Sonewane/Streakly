package com.example.core.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

object AppTextStyles {
    // Modern styled design system typography scale
    fun screenTitle(color: Color): TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        letterSpacing = 0.5.sp,
        color = color
    )

    fun sectionHeader(color: Color): TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = color
    )

    fun cardTitle(color: Color): TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = color
    )

    fun statNumber(color: Color): TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        letterSpacing = (-1).sp,
        color = color
    )

    fun statMedium(color: Color): TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = color
    )

    fun body(color: Color): TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp, // 1.5 height scaling factor
        color = color
    )

    fun label(color: Color): TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.3.sp,
        color = color
    )

    fun hint(color: Color): TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.2.sp,
        color = color
    )

    // Backward compatibility getters to keep existing screens functioning flawlessly
    val displayLarge: TextStyle @Composable get() = statNumber(AppColors.textPrimary)
    val headingLarge: TextStyle @Composable get() = screenTitle(AppColors.textPrimary)
    val headingMedium: TextStyle @Composable get() = statMedium(AppColors.textPrimary)
    val titleLarge: TextStyle @Composable get() = sectionHeader(AppColors.textPrimary)
    val titleMedium: TextStyle @Composable get() = cardTitle(AppColors.textPrimary)
    val bodyLarge: TextStyle @Composable get() = body(AppColors.textPrimary)
    val bodyMedium: TextStyle @Composable get() = body(AppColors.textSecondary)
    val bodySmall: TextStyle @Composable get() = label(AppColors.textSecondary)
    val actionButton: TextStyle @Composable get() = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = 1.sp,
        color = AppColors.textPrimary
    )
    val caption: TextStyle @Composable get() = hint(AppColors.textHint)
}
