package com.frei.app.presentation.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.frei.app.presentation.booking.flight.FlightViewModel
import com.frei.app.presentation.booking.hotel.HotelViewModel

@Composable
fun TripSearchSection(
    flightViewModel: FlightViewModel,
    hotelViewModel: HotelViewModel,
    modifier: Modifier = Modifier,
    onFlightSearchClicked: (
        originId: String, destinationId: String, isRoundTrip: Boolean,
        departDate: Long?, returnDate: Long?, paxCount: Int
    ) -> Unit = { _, _, _, _, _, _ -> },
    onHotelSearchClicked: (cityId: String, cityName: String, minStars: Double?) -> Unit = { _, _, _ -> }
) {
    var selectedMode by remember { mutableStateOf(BookingMode.FLIGHTS) }

    Column(modifier = modifier) {
        BookingModeToggle(selectedMode = selectedMode, onModeChange = { selectedMode = it })
        Spacer(modifier = Modifier.height(12.dp))
        AnimatedContent(
            targetState = selectedMode,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "SearchCardSwitch"
        ) { mode ->
            when (mode) {
                BookingMode.FLIGHTS -> FlightSearchCard(viewModel = flightViewModel, onSearchClicked = onFlightSearchClicked)
                BookingMode.HOTELS -> HotelSearchCard(viewModel = hotelViewModel, onSearchClicked = onHotelSearchClicked)
            }
        }
    }
}