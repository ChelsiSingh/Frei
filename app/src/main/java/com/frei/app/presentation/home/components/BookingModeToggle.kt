package com.frei.app.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.ui.theme.FreiGradPurple
import com.frei.app.ui.theme.FreiInk
import com.frei.app.ui.theme.FreiInkFaint


enum class BookingMode { FLIGHTS, HOTELS }

@Composable
fun BookingModeToggle(
    selectedMode: BookingMode,
    onModeChange: (BookingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Row(modifier = modifier.fillMaxWidth()) {
            ModeTab(
                label = "Flights",
                icon = Icons.Default.Flight,
                selected = selectedMode == BookingMode.FLIGHTS,
                onClick = { onModeChange(BookingMode.FLIGHTS) },
                modifier = Modifier.weight(1f)
            )
            ModeTab(
                label = "Hotels",
                icon = Icons.Default.Hotel,
                selected = selectedMode == BookingMode.HOTELS,
                onClick = { onModeChange(BookingMode.HOTELS) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(1.dp))

        // Track + sliding indicator
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).padding(horizontal = 12.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    .background(Color(0xFFEEEAF8), RoundedCornerShape(4.dp))
            )
            val indicatorOffset by animateDpAsState(
                targetValue = if (selectedMode == BookingMode.HOTELS) 205.dp else 0.dp, // adjust to your measured tab width
                animationSpec = tween(320),
                label = "TabIndicatorOffset"
            )
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(150.dp)
                    .fillMaxHeight()
                    .background(FreiGradPurple, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun ModeTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null
) {
    val textColor by animateColorAsState(if (selected) FreiInk else FreiInkFaint, label = "TabTextColor")
    val iconColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else FreiInkFaint,
        label = "TabIconColor"
    )

    Box(
        modifier = modifier
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(top = 10.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.height(19.dp))
            Spacer(Modifier.width(7.dp))
            Text(label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (badgeText != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-6).dp)
                    .rotate(3f)
            ) {
                Text(
                    badgeText,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}