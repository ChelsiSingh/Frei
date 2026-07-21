package com.frei.app.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.frei.app.presentation.auth.LoginScreen
import com.frei.app.presentation.auth.RegisterScreen
import com.frei.app.presentation.booking.flight.BookingDetailsScreen
import com.frei.app.presentation.booking.flight.BookingDetailsViewModel
import com.frei.app.presentation.booking.flight.FlightListScreen
import com.frei.app.presentation.booking.flight.FlightListViewModel
import com.frei.app.presentation.booking.hotel.HotelDetailsScreen
import com.frei.app.presentation.booking.hotel.HotelDetailsViewModel
import com.frei.app.presentation.booking.hotel.HotelGuestDetailsScreen
import com.frei.app.presentation.booking.hotel.HotelListScreen
import com.frei.app.presentation.booking.hotel.HotelListViewModel
import com.frei.app.presentation.booking.payment.FlightConfirmPayScreen
import com.frei.app.presentation.booking.payment.FlightConfirmPayViewModel
import com.frei.app.presentation.booking.payment.HotelConfirmPayScreen
import com.frei.app.presentation.booking.payment.HotelConfirmPayViewModel
import com.frei.app.presentation.booking.seat.SeatSelectionScreen
import com.frei.app.presentation.booking.seat.SeatSelectionViewModel
import com.frei.app.presentation.expenses.ExpensesScreen
import com.frei.app.presentation.home.HomeScreen
import com.frei.app.presentation.mybookings.BoardingPassScreen
import com.frei.app.presentation.mybookings.BoardingPassViewModel
import com.frei.app.presentation.mybookings.BookingsScreen
import com.frei.app.presentation.mybookings.FlightInvoiceScreen
import com.frei.app.presentation.mybookings.FlightInvoiceViewModel
import com.frei.app.presentation.mybookings.HotelBookingDetailsScreen
import com.frei.app.presentation.mybookings.HotelBookingDetailsViewModel
import com.frei.app.presentation.mybookings.HotelInvoiceScreen
import com.frei.app.presentation.mybookings.HotelInvoiceViewModel
import com.frei.app.presentation.mytrips.MyTripScreen
import com.frei.app.presentation.mytrips.TripsDashboardScreen
import com.frei.app.presentation.newtrip.NewTripScreen
import com.frei.app.presentation.notification.NotificationsScreen
import com.frei.app.presentation.packing.PackingDashboardScreen
import com.frei.app.presentation.packing.PackingScreen
import com.frei.app.presentation.profile.ProfileScreen


@Composable
fun FreiNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Home.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            HomeScreen(
                navController = navController,
                tripId = tripId,
                onNewTripClick = {
                    navController.navigate(Screen.NewTrip.route)
                },
                onPackingClick = { navController.navigate("packing_dashboard") { launchSingleTop = true } },
                onTripsClick = { navController.navigate(Screen.TripsDashboard.route) },
                onExpensesClick = { navController.navigate(Screen.Expenses.route) },
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.NewTrip.route) {
            NewTripScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveSuccess = { _ ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.NewTrip.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TripsDashboard.route) {
            TripsDashboardScreen(
                onBackClick = { navController.popBackStack() },
                onTripClick = { passedTripId ->
                    navController.navigate(Screen.MyTrips.createRoute(passedTripId))
                },
                onNewTripClick = {
                    navController.navigate(Screen.NewTrip.route)
                }
            )
        }

        composable(
            route = Screen.MyTrips.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val idParam = backStackEntry.arguments?.getString("tripId").orEmpty()
            MyTripScreen(
                tripId = idParam,
                onBackClick = { navController.popBackStack() },
                onNavigateHome = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onBookForTrip = { forTripId ->
                    navController.navigate(Screen.Home.createRoute(forTripId)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("packing_dashboard") {
            PackingDashboardScreen(
                onBackClick = { navController.popBackStack() },
                onTripSelected = { tripId, _ ->
                    navController.navigate("packing/$tripId/trip")
                }
            )
        }

        composable(
            route = "packing/{tripId}/{tripName}",
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType },
                navArgument("tripName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId").orEmpty()

            PackingScreen(
                tripId = tripId,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            val ctx = LocalContext.current
            ProfileScreen(
                onSettingsClick = { Toast.makeText(ctx, "Settings — coming soon", Toast.LENGTH_SHORT).show()  },
                onPreviousTripsClick = { navController.navigate(Screen.TripsDashboard.route) },
                onCustomerSupportClick = { Toast.makeText(ctx, "Customer Support — coming soon", Toast.LENGTH_SHORT).show() },
                onAboutAppClick = { Toast.makeText(ctx, "About App — coming soon", Toast.LENGTH_SHORT).show() },
                onEditProfileClick = { Toast.makeText(ctx, "Edit Profile — coming soon", Toast.LENGTH_SHORT).show() },
                onBackClick = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Expenses.route) {
            val ctx = LocalContext.current
            ExpensesScreen(
                onBackClick = { navController.popBackStack() },
                onFilterClick = { Toast.makeText(ctx, "Filter — coming soon", Toast.LENGTH_SHORT).show() }
            )
        }

        composable(
            route = Screen.FlightList.route,
            arguments = listOf(
                navArgument("origin") { type = NavType.StringType },
                navArgument("destination") { type = NavType.StringType },
                navArgument("depart_date") { type = NavType.StringType },
                navArgument("pax_count") { type = NavType.IntType },
                navArgument("is_round_trip") { type = NavType.BoolType },
                navArgument("return_date") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("tripId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            val viewModel: FlightListViewModel = hiltViewModel()
            FlightListScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onEditSearchClick = { navController.popBackStack() },
                onBookFlight = { flight ->
                    navController.navigate(Screen.BookingDetails.createRoute(flight.id, tripId))
                }
            )
        }

        composable(
            route = Screen.BookingDetails.route,
            arguments = listOf(
                navArgument("flightId") { type = NavType.StringType },
                navArgument("tripId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            val viewModel: BookingDetailsViewModel = hiltViewModel()
            BookingDetailsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onSelectSeatClick = { flightId, travelers, name, email, mobile ->
                    navController.navigate(
                        Screen.SeatSelection.createRoute(flightId, travelers, name, email, mobile, tripId)
                    )
                }
            )
        }

        composable(
            route = Screen.SeatSelection.route,
            arguments = listOf(
                navArgument("flightId") { type = NavType.StringType },
                navArgument("travelers") { type = NavType.StringType; defaultValue = "1" },
                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                navArgument("email") { type = NavType.StringType; defaultValue = "" },
                navArgument("phone") { type = NavType.StringType; defaultValue = "" },
                navArgument("tripId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val flightId = backStackEntry.arguments?.getString("flightId").orEmpty()
            val travelers = backStackEntry.arguments?.getString("travelers")?.toIntOrNull() ?: 1
            val name = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("name").orEmpty(), "UTF-8")
            val email = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("email").orEmpty(), "UTF-8")
            val phone = backStackEntry.arguments?.getString("phone").orEmpty()
            val tripId = backStackEntry.arguments?.getString("tripId")

            val viewModel: SeatSelectionViewModel = hiltViewModel()
            SeatSelectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { seat ->
                    navController.navigate(
                        Screen.FlightConfirmPay.createRoute(
                            flightId = flightId, travelers = travelers, name = name, email = email, phone = phone,
                            seat = seat.seatNumber, seatClass = seat.seatClass.name, seatPrice = seat.extraPrice,
                            tripId = tripId
                        )
                    )
                }
            )
        }

        composable(
            route = Screen.HotelList.route,
            arguments = listOf(
                navArgument("city_id") { type = NavType.StringType },
                navArgument("city_name") { type = NavType.StringType },
                navArgument("min_stars") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("tripId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            val viewModel: HotelListViewModel = hiltViewModel()
            HotelListScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onHotelClick = { hotel ->
                    navController.navigate(Screen.HotelDetails.createRoute(hotel.id, tripId))
                }
            )
        }

        composable(
            route = Screen.HotelDetails.route,
            arguments = listOf(
                navArgument("hotelId") { type = NavType.StringType },
                navArgument("tripId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            val viewModel: HotelDetailsViewModel = hiltViewModel()
            HotelDetailsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onReserveClick = { hotelId ->
                    navController.navigate(Screen.HotelGuestDetails.createRoute(hotelId, tripId))
                }
            )
        }

        composable(
            route = Screen.HotelGuestDetails.route,
            arguments = listOf(
                navArgument("hotelId") { type = NavType.StringType },
                navArgument("tripId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            val viewModel: HotelDetailsViewModel = hiltViewModel()
            HotelGuestDetailsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onContinueClick = { hotelId, guests, name, email, phone, nights, checkIn, checkOut ->
                    navController.navigate(
                        Screen.HotelConfirmPay.createRoute(
                            hotelId = hotelId, guests = guests, name = name, email = email, phone = phone,
                            nights = nights, checkIn = checkIn, checkOut = checkOut, tripId = tripId
                        )
                    )
                }
            )
        }

        composable(
            route = Screen.FlightConfirmPay.route,
            arguments = listOf(
                navArgument("flightId") { type = NavType.StringType },
                navArgument("travelers") { type = NavType.StringType; defaultValue = "1" },
                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                navArgument("email") { type = NavType.StringType; defaultValue = "" },
                navArgument("phone") { type = NavType.StringType; defaultValue = "" },
                navArgument("seat") { type = NavType.StringType; defaultValue = "" },
                navArgument("seatClass") { type = NavType.StringType; defaultValue = "" },
                navArgument("seatPrice") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("tripId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            val viewModel: FlightConfirmPayViewModel = hiltViewModel()
            FlightConfirmPayScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onDone = { navController.popBackStack(Screen.Home.route, inclusive = false) }
            )
        }

        composable(
            route = Screen.HotelConfirmPay.route,
            arguments = listOf(
                navArgument("hotelId") { type = NavType.StringType },
                navArgument("guests") { type = NavType.StringType; defaultValue = "1" },
                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                navArgument("email") { type = NavType.StringType; defaultValue = "" },
                navArgument("phone") { type = NavType.StringType; defaultValue = "" },
                navArgument("nights") { type = NavType.StringType; defaultValue = "1" },
                navArgument("checkIn") { type = NavType.StringType; defaultValue = "" },
                navArgument("checkOut") { type = NavType.StringType; defaultValue = "" },
                navArgument("roomType") { type = NavType.StringType; defaultValue = "Studio Suite" },
                navArgument("tripId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            val viewModel: HotelConfirmPayViewModel = hiltViewModel()
            HotelConfirmPayScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onDone = { navController.popBackStack(Screen.Home.route, inclusive = false) }
            )
        }

        composable(Screen.Bookings.route) {
            BookingsScreen(
                onBackClick = { navController.popBackStack() },
                onBoardingPassClick = { booking ->
                    navController.navigate(Screen.BoardingPass.createRoute(booking.flightId))
                },
                onFlightInvoiceClick = { booking ->
                    navController.navigate(Screen.FlightInvoice.createRoute(booking.flightId))
                },
                onViewHotelBookingClick = { booking ->
                    navController.navigate(Screen.HotelBookingDetails.createRoute(booking.hotelId))
                },
                onHotelInvoiceClick = { booking ->
                    navController.navigate(Screen.HotelInvoice.createRoute(booking.hotelId))
                },
                onSearchFlightsClick = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        composable(
            route = Screen.BoardingPass.route,
            arguments = listOf(navArgument("flightId") { type = NavType.StringType })
        ) {
            val viewModel: BoardingPassViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            uiState?.let { state ->
                BoardingPassScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onShareClick = { /* TODO: share intent */ },
                    onAddToWalletClick = { /* TODO: wallet integration */ },
                    onDownloadClick = { /* TODO: PDF export */ }
                )
            }
            // TODO: show a loading state while uiState == null (viewModel.isLoading)
        }

        composable(
            route = Screen.FlightInvoice.route,
            arguments = listOf(navArgument("flightId") { type = NavType.StringType })
        ) {
            val viewModel: FlightInvoiceViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            uiState?.let { state ->
                FlightInvoiceScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onShareClick = { /* TODO: share intent */ },
                    onEmailClick = { /* TODO: email intent */ },
                    onDownloadClick = { /* TODO: PDF export */ }
                )
            }
        }

        composable(
            route = Screen.HotelInvoice.route,
            arguments = listOf(navArgument("hotelBookingId") { type = NavType.StringType })
        ) {
            val viewModel: HotelInvoiceViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            uiState?.let { state ->
                HotelInvoiceScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onShareClick = { /* TODO: share intent */ },
                    onEmailClick = { /* TODO: email intent */ },
                    onDownloadClick = { /* TODO: PDF export */ }
                )
            }
        }

        composable(
            route = Screen.HotelBookingDetails.route,
            arguments = listOf(navArgument("hotelBookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val hotelBookingId = backStackEntry.arguments?.getString("hotelBookingId").orEmpty()
            val viewModel: HotelBookingDetailsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            uiState?.let { state ->
                HotelBookingDetailsScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onFavoriteClick = { /* TODO: favorite/save toggle */ },
                    onDirectionsClick = { /* TODO: maps intent */ },
                    onViewInvoiceClick = {
                        navController.navigate(Screen.HotelInvoice.createRoute(hotelBookingId))
                    },
                    onCallContactClick = { /* TODO: dial intent */ }
                )
            }
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}