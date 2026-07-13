package com.frei.app.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frei.app.presentation.home.model.QuickAction

@Composable
fun QuickActionCard(
    action: QuickAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .then(modifier)
            .height(110.dp)
            .background(
                action.background,
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Icon(
            imageVector = action.icon,
            contentDescription = action.title,
            tint = action.iconColor
        )

        Text(
            text = action.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = action.textColor
        )
    }
}