package com.example.shared.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.theme.AppColors

@Composable
fun StreakFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = AppColors.accentOrange
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .shadow(elevation = 8.dp, shape = CircleShape)
            .size(56.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor,
                        accentColor.copy(alpha = 0.85f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .testTag("add_task_fab"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Task",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}
