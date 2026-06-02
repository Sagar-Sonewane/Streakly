package com.example.shared.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IconBadge(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.27f)) // 12dp on 44dp size is 0.27 padding factor
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.48f) // ~21dp on 44dp size
        )
    }
}

object BadgeColors {
    val orange  = Color(0xFFFF6B35)
    val blue    = Color(0xFF4A90FF)
    val green   = Color(0xFF2ECC71)
    val purple  = Color(0xFF7B5CF0)
    val teal    = Color(0xFF00C9A7)
    val red     = Color(0xFFFF3D71)
    val amber   = Color(0xFFFFAB00)
    val navy    = Color(0xFF3D5AF1)
}
