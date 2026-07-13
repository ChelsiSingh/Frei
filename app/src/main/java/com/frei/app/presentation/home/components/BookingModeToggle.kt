package com.frei.app.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frei.app.ui.theme.FreiPurple

enum class BookingMode { FLIGHTS, HOTELS }

@Composable
fun BookingModeToggle(
    selectedMode: BookingMode,
    onModeChange: (BookingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(48.dp)
            .background(Color(0xFFF4F1FC), RoundedCornerShape(14.dp)).padding(4.dp)
    ) {
        val tabWidth = maxWidth / 2
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedMode == BookingMode.HOTELS) tabWidth else 0.dp,
            animationSpec = tween(250),
            label = "ModeIndicatorOffset"
        )
        Box(
            modifier = Modifier.offset(x = indicatorOffset).width(tabWidth).fillMaxHeight()
                .padding(horizontal = 2.dp).background(Color.White, RoundedCornerShape(11.dp))
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ModeTab(
                label = "✈️  Flights",
                selected = selectedMode == BookingMode.FLIGHTS,
                onClick = { onModeChange(BookingMode.FLIGHTS) },
                modifier = Modifier.weight(1f)
            )
            ModeTab(
                label = "🏨  Hotels",
                selected = selectedMode == BookingMode.HOTELS,
                onClick = { onModeChange(BookingMode.HOTELS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val textColor by animateColorAsState(if (selected) FreiPurple else Color.Gray, label = "ModeTabColor")
    Box(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(11.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = textColor, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}