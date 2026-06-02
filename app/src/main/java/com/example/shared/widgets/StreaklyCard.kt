package com.example.shared.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.theme.AppColors

@Composable
fun StreaklyCard(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    borderRadius: Dp = 20.dp,
    color: Color? = null,
    borderColor: Color? = null,
    onTap: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isDark = AppColors.isDark
    val bg = color ?: AppColors.bgSecondary
    
    // In light mode, reference app style uses modern white cards on gray bg with subtle elevation
    var cardModifier = modifier
    if (!isDark) {
        cardModifier = cardModifier.shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(borderRadius),
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.08f),
            spotColor = Color.Black.copy(alpha = 0.08f)
        )
    }
    
    cardModifier = cardModifier
        .clip(RoundedCornerShape(borderRadius))
        .background(bg)
        
    if (borderColor != null) {
        cardModifier = cardModifier.border(
            width = 1.dp,
            color = borderColor,
            shape = RoundedCornerShape(borderRadius)
        )
    } else if (isDark) {
        cardModifier = cardModifier.border(
            width = 0.8.dp,
            color = AppColors.border,
            shape = RoundedCornerShape(borderRadius)
        )
    }
    
    if (onTap != null) {
        cardModifier = cardModifier.clickable { onTap() }
    }
    
    Box(
        modifier = cardModifier.padding(padding)
    ) {
        content()
    }
}
