package com.frei.app.presentation.home.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val cardBackground: Brush,
    val cardOutlineColor: Color,
    val iconChipColor: Color,
    val iconTint: Color,
    val textColor: Color
)