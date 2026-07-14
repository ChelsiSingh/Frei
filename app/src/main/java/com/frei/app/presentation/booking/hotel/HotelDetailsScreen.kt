package com.frei.app.presentation.booking.hotel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.data.model.hotel.Hotel
import com.frei.app.presentation.booking.flight.FreiBackground
import com.frei.app.presentation.booking.flight.FreiInk
import com.frei.app.presentation.booking.flight.FreiPrimary
import com.frei.app.presentation.booking.flight.FreiSubtext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailsScreen(
    viewModel: HotelDetailsViewModel,
    onBackClick: () -> Unit,
    onReserveClick: (hotelId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hotel Details", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(8.dp).size(38.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFECEAF3), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreiInk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FreiBackground)
            )
        },
        containerColor = FreiBackground
    ) { innerPadding ->
        when (val state = uiState) {
            is HotelDetailUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = FreiPrimary) }

            is HotelDetailUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center
            ) { Text(state.message, color = Color.Red, fontWeight = FontWeight.Bold) }

            is HotelDetailUiState.Success -> HotelDetailsContent(
                hotel = state.hotel,
                innerPadding = innerPadding,
                onReserveClick = { onReserveClick(state.hotel.id) }
            )
        }
    }
}

@Composable
private fun HotelDetailsContent(hotel: Hotel, innerPadding: PaddingValues, onReserveClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 18.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Spacer(Modifier.height(4.dp))

            // Gallery: decorative gradient placeholder — Hotel DTO has a single
            // `image: String?`, not a gallery array, so no real photo grid/count.
            Box(
                modifier = Modifier.fillMaxWidth().height(168.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFFD9CBF2), Color(0xFFB79AE0))), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(46.dp))
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Text(hotel.name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk, modifier = Modifier.weight(1f))
                Row {
                    repeat(hotel.starRating.coerceIn(0, 5)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF5A623), modifier = Modifier.size(13.dp))
                    }
                }
            }
            hotel.address?.let {
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = FreiSubtext, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(it, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = FreiSubtext)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFFF0EEF6), RoundedCornerShape(14.dp)).padding(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(34.dp).background(Color(0xFF1EA672), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("${hotel.userRating}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
                Spacer(Modifier.width(10.dp))
                Text("Guest Rating", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFB6B3C6))
            }

            if (hotel.amenities.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(hotel.amenities) { amenity ->
                        AmenityPill(icon = amenityIcon(amenity), label = amenity)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))


            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF0EEF6), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier.background(Color(0xFFEAF6EF), RoundedCornerShape(99.dp)).padding(horizontal = 9.dp, vertical = 3.dp)
                    ) { Text("Recommended Deal", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1EA672)) }
                    Spacer(Modifier.height(7.dp))
                    Text("Studio Suite", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
                    Spacer(Modifier.height(8.dp))
                    val priceLabel = if (hotel.currency == "INR") "\u20B9${hotel.pricePerNight.toInt()}" else "${hotel.currency} ${hotel.pricePerNight}"
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(priceLabel, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
                        Spacer(Modifier.width(6.dp))
                        Text("/ night", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FreiSubtext)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Per night", fontSize = 11.sp, color = FreiSubtext)
                val priceLabel = if (hotel.currency == "INR") "\u20B9${hotel.pricePerNight.toInt()}" else "${hotel.currency} ${hotel.pricePerNight}"
                Text(priceLabel, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
            }
            Button(
                onClick = onReserveClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreiPrimary)
            ) {
                Text("Reserve Room", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun AmenityPill(icon: ImageVector, label: String) {
    Column(
        modifier = Modifier.width(64.dp).background(Color(0xFFF5F2FC), RoundedCornerShape(12.dp)).padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = FreiPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FreiInk, maxLines = 1)
    }
}

private fun amenityIcon(amenity: String): ImageVector = when {
    amenity.contains("pool", ignoreCase = true) -> Icons.Default.Pool
    amenity.contains("wifi", ignoreCase = true) -> Icons.Default.Wifi
    amenity.contains("gym", ignoreCase = true) || amenity.contains("fitness", ignoreCase = true) -> Icons.Default.FitnessCenter
    amenity.contains("restaurant", ignoreCase = true) || amenity.contains("dining", ignoreCase = true) -> Icons.Default.Restaurant
    amenity.contains("parking", ignoreCase = true) -> Icons.Default.LocalParking
    amenity.contains("spa", ignoreCase = true) -> Icons.Default.Spa
    amenity.contains("ac", ignoreCase = true) || amenity.contains("air conditioning", ignoreCase = true) -> Icons.Default.AcUnit
    else -> Icons.Default.CheckCircle
}