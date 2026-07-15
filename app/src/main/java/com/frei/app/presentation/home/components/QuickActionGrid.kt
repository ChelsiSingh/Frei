package com.frei.app.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.frei.app.ui.theme.FreiGradPurple
import com.frei.app.ui.theme.FreiInk
import com.frei.app.ui.theme.FreiTeal
import com.frei.app.ui.theme.FreiTealSoft

@Composable
fun QuickActionGrid(
    onNewTripClick: () -> Unit,
    onExpensesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val newtrip = QuickAction(
        title = "New Trip",
        icon = Icons.Default.AddCircleOutline,
        cardBackground = FreiGradPurple,
        cardOutlineColor = Color(0xFF6C3FCF),
        iconChipColor = Color.White.copy(alpha = 0.18f),
        iconTint = Color.White,
        textColor = Color.White
    )

    val expenses = QuickAction(
        title = "Expenses",
        icon = Icons.Default.ReceiptLong,
        cardBackground = SolidColor(Color.White),
        cardOutlineColor = FreiTeal,
        iconChipColor = FreiTealSoft,
        iconTint = FreiTeal,
        textColor = FreiInk
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(action = newtrip, modifier = Modifier.weight(1f), onClick = onNewTripClick)
        QuickActionCard(action = expenses, modifier = Modifier.weight(1f), onClick = onExpensesClick)
    }
}