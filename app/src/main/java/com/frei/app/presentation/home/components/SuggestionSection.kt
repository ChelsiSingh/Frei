package com.frei.app.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frei.app.R
import com.frei.app.presentation.home.model.Destination
import com.frei.app.ui.theme.FreiPurple

@Composable
fun SuggestionSection() {

    val destinations = listOf(

        Destination(
            "Varanasi",
            "India",
            "₹20000k",
            R.drawable.varanasi
        ),

        Destination(
            "Manali",
            "India",
            "₹25000k",
            R.drawable.manali
        ),

        Destination(
            "Kerala",
            "India",
            "₹30000",
            R.drawable.kerala
        )
    )

    Column {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "Suggestions",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                "See all",
                color = FreiPurple,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(destinations) {

                DestinationCard(it)
            }
        }
    }
}