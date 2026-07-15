package com.frei.app.presentation.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.frei.app.ui.theme.FreiBorder
import com.frei.app.ui.theme.FreiInk
import com.frei.app.ui.theme.FreiInkSoft

@Composable
fun RecommendedHotelsSection(
    onHotelClick: (hotelId: String) -> Unit = {},
    viewModel: RecommendedHotelsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Recommended Hotels", style = MaterialTheme.typography.titleMedium, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("See all", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        when (val state = uiState) {
            is RecommendedHotelsUiState.Loading -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(3) { HotelSkeletonCard() }
                }
            }
            is RecommendedHotelsUiState.Success -> {
                if (state.hotels.isEmpty()) {
                    HotelEmptyState()
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(state.hotels.size) { index ->
                            val hotel = state.hotels[index]
                            HotelCard(
                                hotel = hotel,
                                toneIndex = index,
                                onClick = { onHotelClick(hotel.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HotelSkeletonCard() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress"
    )
    val brush = Brush.linearGradient(
        colors = listOf(FreiBorder, Color(0xFFF6F3FE), FreiBorder),
        start = Offset(progress * 400f - 200f, 0f),
        end = Offset(progress * 400f + 200f, 0f)
    )
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .width(208.dp)
            .height(216.dp)
            .background(brush, RoundedCornerShape(22.dp))
    )
}

@Composable
private fun HotelEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Hotel, null, tint = FreiInkSoft, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(6.dp))
        Text("No recommendations yet — check back soon.", fontSize = 12.5.sp, color = FreiInkSoft, fontWeight = FontWeight.SemiBold)
    }
}