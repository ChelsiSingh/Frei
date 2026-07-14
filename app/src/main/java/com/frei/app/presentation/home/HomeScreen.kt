package com.frei.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.frei.app.navigation.Screen
import com.frei.app.presentation.booking.flight.FlightViewModel
import com.frei.app.presentation.booking.hotel.HotelViewModel
import com.frei.app.presentation.home.components.FreiBottomBar
import com.frei.app.presentation.home.components.HomeTopBar
import com.frei.app.presentation.home.components.QuickActionGrid
import com.frei.app.presentation.home.components.SuggestionSection
import com.frei.app.presentation.home.components.TripSearchSection
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun HomeScreen(
    navController: NavController,
    onAddTripClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPackingClick: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    flightViewModel: FlightViewModel = hiltViewModel(),
    hotelViewModel: HotelViewModel = hiltViewModel()
) {
    Scaffold(
        bottomBar = {
            FreiBottomBar(
                selectedIndex = 0,
                onItemSelected = { index ->
                    when (index) {
                        0 -> { /* Already home */ }
                        1 -> navController.navigate(Screen.Bookings.route)
                        2 -> onTripsClick()
                        3 -> navController.navigate(Screen.Profile.route)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { HomeTopBar() }
            item {
                val formatter = SimpleDateFormat("yyyy-MM-dd", LocalLocale.current.platformLocale)

                TripSearchSection(
                    flightViewModel = flightViewModel,
                    hotelViewModel = hotelViewModel,
                    onFlightSearchClicked = { origin, destination, isRoundTrip, departMillis, returnMillis, pax ->
                        val departDate = departMillis?.let { formatter.format(Date(it)) } ?: ""
                        val returnDate = returnMillis?.let { formatter.format(Date(it)) }

                        navController.navigate(
                            Screen.FlightList.createRoute(
                                origin = origin,
                                destination = destination,
                                departDate = departDate,
                                returnDate = returnDate,
                                paxCount = pax,
                                isRoundTrip = isRoundTrip
                            )
                        )
                    },
                    onHotelSearchClicked = { cityId, cityName, minStars ->
                        navController.navigate(
                            Screen.HotelList.createRoute(
                                cityId = cityId,
                                cityName = cityName,
                                minStars = minStars
                            )
                        )
                    }
                )
            }
            item { SuggestionSection() }
            item {
                QuickActionGrid(
                    onAddTripClick = onAddTripClick,
                    onExpensesClick = { navController.navigate(Screen.Expenses.route) }
                )
            }
        }
    }
}