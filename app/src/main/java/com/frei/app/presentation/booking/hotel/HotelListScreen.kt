package com.frei.app.presentation.booking.hotel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.data.model.hotel.Hotel
import com.frei.app.presentation.booking.flight.FreiBackground
import com.frei.app.presentation.booking.flight.FreiInk
import com.frei.app.presentation.booking.flight.FreiPrimary
import com.frei.app.presentation.booking.flight.FreiSubtext
import com.frei.app.presentation.booking.hotel.HotelListUiState
import com.frei.app.presentation.booking.hotel.HotelListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelListScreen(
    viewModel: HotelListViewModel,
    onBackClick: () -> Unit,
    onHotelClick: (Hotel) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cityName = viewModel.cityName

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hotels in $cityName", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk) },
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is HotelListUiState.Idle -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Search for a city to see hotels", color = FreiSubtext)
                }
                is HotelListUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FreiPrimary)
                }
                is HotelListUiState.Success -> {
                    if (state.hotels.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hotels found in this city.", color = FreiSubtext)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(state.hotels, key = { it.id }) { hotel ->
                                HotelCard(hotel = hotel, onClick = { onHotelClick(hotel) })
                            }
                        }
                    }
                }
                is HotelListUiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HotelCard(hotel: Hotel, onClick: () -> Unit) {
    val priceLabel = if (hotel.currency == "INR") "\u20B9${hotel.pricePerNight.toInt()}" else "${hotel.currency} ${hotel.pricePerNight}"

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF0EEF6), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(hotel.name, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
                    hotel.address?.let {
                        Text(it, fontSize = 11.sp, color = FreiSubtext, maxLines = 1)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF5A623), modifier = Modifier.size(14.dp))
                    Text(" ${hotel.userRating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FreiInk)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "\u2B50".repeat(hotel.starRating.coerceIn(0, 5)) + (if (hotel.starRating == 0) "Unrated" else ""),
                fontSize = 11.sp
            )

            if (hotel.amenities.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(hotel.amenities) { amenity ->
                        Box(
                            modifier = Modifier.background(Color(0xFFF5F2FC), RoundedCornerShape(99.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(amenity, fontSize = 10.sp, color = FreiPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(priceLabel, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = FreiPrimary)
                    Text("per night", fontSize = 10.sp, color = FreiSubtext)
                }
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FreiPrimary)
                ) {
                    Text("View", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}