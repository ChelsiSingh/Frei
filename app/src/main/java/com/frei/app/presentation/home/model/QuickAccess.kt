package com.frei.app.presentation.home.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val background: Color,
    val iconColor: Color,
    val textColor: Color
)