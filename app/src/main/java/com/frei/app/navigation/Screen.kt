package com.frei.app.navigation

sealed class Screen(val route: String) {

        data object Login : Screen("login")
        data object Register : Screen("register")
        data object Home : Screen("home?tripId={tripId}") {
                fun createRoute(tripId: String? = null): String =
                        if (tripId != null) "home?tripId=$tripId" else "home"
        }

        data object Trips : Screen("trips")
        data object NewTrip : Screen("new_trip")
        data object TripsDashboard : Screen("trips_dashboard")

        data object MyTrips : Screen("my_trips/{tripId}") {
                fun createRoute(tripId: String) = "my_trips/$tripId"
        }

        data object Expenses : Screen("expenses")
        data object Bookings : Screen("bookings")
        data object Profile : Screen("profile")

        data object FlightList : Screen(
                "flight_list/{origin}/{destination}/{depart_date}/{pax_count}/{is_round_trip}?return_date={return_date}&tripId={tripId}"
        ) {
                fun createRoute(
                        origin: String,
                        destination: String,
                        departDate: String,
                        paxCount: Int,
                        isRoundTrip: Boolean,
                        tripId: String? = null,
                        returnDate: String? = null
                ): String {
                        val base = "flight_list/$origin/$destination/$departDate/$paxCount/$isRoundTrip"
                        val params = buildList {
                                if (isRoundTrip && !returnDate.isNullOrBlank()) add("return_date=$returnDate")
                                if (!tripId.isNullOrBlank()) add("tripId=$tripId")
                        }
                        return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
                }
        }

        data object BookingDetails : Screen("booking_details/{flightId}?tripId={tripId}") {
                fun createRoute(flightId: String, tripId: String? = null): String {
                        val base = "booking_details/$flightId"
                        return if (tripId != null) "$base?tripId=$tripId" else base
                }
        }

        data object HotelList : Screen(
                "hotel_list/{city_id}/{city_name}?min_stars={min_stars}&tripId={tripId}"
        ) {
                fun createRoute(
                        cityId: String,
                        cityName: String,
                        minStars: Double? = null,
                        tripId: String? = null
                ): String {
                        val encodedName = java.net.URLEncoder.encode(cityName, "UTF-8")
                        val base = "hotel_list/$cityId/$encodedName"
                        val params = buildList {
                                if (minStars != null) add("min_stars=$minStars")
                                if (!tripId.isNullOrBlank()) add("tripId=$tripId")
                        }
                        return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
                }
        }

        data object HotelDetails : Screen("hotel_details/{hotelId}?tripId={tripId}") {
                fun createRoute(hotelId: String, tripId: String? = null): String {
                        val base = "hotel_details/$hotelId"
                        return if (tripId != null) "$base?tripId=$tripId" else base
                }
        }

        data object HotelGuestDetails : Screen("hotel_guest_details/{hotelId}?tripId={tripId}") {
                fun createRoute(hotelId: String, tripId: String? = null): String {
                        val base = "hotel_guest_details/$hotelId"
                        return if (tripId != null) "$base?tripId=$tripId" else base
                }
        }

        data object SeatSelection : Screen(
                "seat_selection/{flightId}?travelers={travelers}&name={name}&email={email}&phone={phone}&tripId={tripId}"
        ) {
                fun createRoute(flightId: String, travelers: Int, name: String, email: String, phone: String, tripId: String? = null): String {
                        val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
                        val encodedEmail = java.net.URLEncoder.encode(email, "UTF-8")
                        val tripParam = if (!tripId.isNullOrBlank()) "&tripId=$tripId" else ""
                        return "seat_selection/$flightId?travelers=$travelers&name=$encodedName&email=$encodedEmail&phone=$phone$tripParam"
                }
        }

        data object FlightConfirmPay : Screen(
                "flight_confirm_pay/{flightId}?travelers={travelers}&name={name}&email={email}&phone={phone}" +
                        "&seat={seat}&seatClass={seatClass}&seatPrice={seatPrice}&tripId={tripId}"
        ) {
                fun createRoute(
                        flightId: String, travelers: Int, name: String, email: String, phone: String,
                        seat: String, seatClass: String, seatPrice: Double, tripId: String? = null
                ): String {
                        val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
                        val encodedEmail = java.net.URLEncoder.encode(email, "UTF-8")
                        val tripParam = if (!tripId.isNullOrBlank()) "&tripId=$tripId" else ""
                        return "flight_confirm_pay/$flightId?travelers=$travelers&name=$encodedName&email=$encodedEmail" +
                                "&phone=$phone&seat=$seat&seatClass=$seatClass&seatPrice=$seatPrice$tripParam"
                }
        }

        data object HotelConfirmPay : Screen(
                "hotel_confirm_pay/{hotelId}?guests={guests}&name={name}&email={email}&phone={phone}" +
                        "&nights={nights}&checkIn={checkIn}&checkOut={checkOut}&roomType={roomType}&tripId={tripId}"
        ) {
                fun createRoute(
                        hotelId: String, guests: Int, name: String, email: String, phone: String,
                        nights: Int = 1, checkIn: String = "", checkOut: String = "", roomType: String = "Studio Suite",
                        tripId: String? = null
                ): String {
                        val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
                        val encodedEmail = java.net.URLEncoder.encode(email, "UTF-8")
                        val encodedCheckIn = java.net.URLEncoder.encode(checkIn, "UTF-8")
                        val encodedCheckOut = java.net.URLEncoder.encode(checkOut, "UTF-8")
                        val encodedRoomType = java.net.URLEncoder.encode(roomType, "UTF-8")
                        val tripParam = if (!tripId.isNullOrBlank()) "&tripId=$tripId" else ""
                        return "hotel_confirm_pay/$hotelId?guests=$guests&name=$encodedName&email=$encodedEmail&phone=$phone" +
                                "&nights=$nights&checkIn=$encodedCheckIn&checkOut=$encodedCheckOut&roomType=$encodedRoomType$tripParam"
                }
        }

        data object BoardingPass : Screen("boarding_pass/{flightId}") {
                fun createRoute(flightId: String) = "boarding_pass/$flightId"
        }

        data object FlightInvoice : Screen("flight_invoice/{flightId}") {
                fun createRoute(flightId: String) = "flight_invoice/$flightId"
        }

        data object HotelInvoice : Screen("hotel_invoice/{hotelBookingId}") {
                fun createRoute(hotelBookingId: String) = "hotel_invoice/$hotelBookingId"
        }

        data object HotelBookingDetails : Screen("hotel_booking_details/{hotelBookingId}") {
                fun createRoute(hotelBookingId: String) = "hotel_booking_details/$hotelBookingId"
        }
}