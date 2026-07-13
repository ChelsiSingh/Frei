package com.frei.app.presentation.newtrip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val Purple = Color(0xFF6C3CF0)

@Composable
fun TransportChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    val icon: ImageVector = when (text) {
        "Flight" -> Icons.Default.Flight
        "Bus" -> Icons.Default.DirectionsBus
        "Train" -> Icons.Default.Train
        else -> Icons.Default.Flight
    }

    FilterChip(
        selected = selected,
        onClick = onClick,

        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = text
                )

                Text(text)

            }
        },

        modifier = Modifier
            .height(48.dp)
            .padding(end = 8.dp),

        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Purple else MaterialTheme.colorScheme.outline
        ),

        colors = FilterChipDefaults.filterChipColors(

            selectedContainerColor = Purple,

            selectedLabelColor = Color.White,

            selectedLeadingIconColor = Color.White,

            containerColor = Color.White,

            labelColor = MaterialTheme.colorScheme.onSurface,

            iconColor = MaterialTheme.colorScheme.onSurface

        )
    )
}