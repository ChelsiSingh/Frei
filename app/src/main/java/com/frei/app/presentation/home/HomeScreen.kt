package com.frei.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.frei.app.navigation.Screen
import com.frei.app.presentation.booking.flight.FlightViewModel
import com.frei.app.presentation.booking.hotel.HotelViewModel
import com.frei.app.presentation.home.components.FreiBottomBar
import com.frei.app.presentation.home.components.HomeTopBar
import com.frei.app.presentation.home.components.QuickActionGrid
import com.frei.app.presentation.home.components.RecommendedHotelsSection
import com.frei.app.presentation.home.components.TripSearchSection
import com.frei.app.presentation.notification.NotificationTokenManager
import com.frei.app.presentation.notification.RequestNotificationPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    onTripsClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onNewTripClick: () -> Unit,
    onPackingClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    tripId: String? = null,
    flightViewModel: FlightViewModel = hiltViewModel(),
    hotelViewModel: HotelViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    RequestNotificationPermission()

    LaunchedEffect(Unit) {
        NotificationTokenManager.registerCurrentToken(firestore, auth)
    }

    Scaffold(
        bottomBar = {
            FreiBottomBar(
                selectedIndex = 0,
                onItemSelected = { index ->
                    when (index) {
                        0 -> { /* Already home */ }
                        1 -> navController.navigate(Screen.Bookings.route)
                        2 -> navController.navigate(Screen.TripsDashboard.route)
                        3 -> navController.navigate(Screen.Profile.route)
                        4 -> navController.navigate(Screen.NewTrip.route)
                        5 -> navController.navigate("packing_dashboard") { launchSingleTop = true }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 14.dp, top = 40.dp, end = 14.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                HomeTopBar(
                    hasUnread = false,
                    onNotificationsClick = onNotificationClick,
                    onProfileClick = onProfileClick
                )
            }
            item {
                val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

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
                                isRoundTrip = isRoundTrip,
                                tripId = tripId
                            )
                        )
                    },
                    onHotelSearchClicked = { cityId, cityName, minStars ->
                        navController.navigate(
                            Screen.HotelList.createRoute(
                                cityId = cityId,
                                cityName = cityName,
                                minStars = minStars,
                                tripId = tripId
                            )
                        )
                    }
                )
            }
            item {
                QuickActionGrid(
                    onNewTripClick = onNewTripClick,
                    onExpensesClick = onExpensesClick
                )
            }
            item {
                RecommendedHotelsSection(
                    onHotelClick = { hotelId ->
                        navController.navigate(Screen.HotelDetails.createRoute(hotelId, tripId))
                    }
                )
            }
        }
    }
}