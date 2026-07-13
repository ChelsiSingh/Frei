package com.frei.app.navigation

sealed class Screen(val route: String) {

        data object Login : Screen("login")
        data object Register : Screen("register")
        data object Home : Screen("home")
        data object Trips : Screen("trips")
        data object NewTrip : Screen("new_trip")
        data object TripsDashboard : Screen("trips_dashboard")

        data object MyTrips : Screen("my_trips/{tripId}") {
                fun createRoute(tripId: String) = "my_trips/$tripId"
        }

        data object Expenses : Screen("expenses")
        data object Profile : Screen("profile")

        data object FlightList : Screen(
                "flight_list/{origin}/{destination}/{depart_date}/{pax_count}/{is_round_trip}?return_date={return_date}"
        ) {
                fun createRoute(
                        origin: String,
                        destination: String,
                        departDate: String,
                        paxCount: Int,
                        isRoundTrip: Boolean,
                        returnDate: String? = null
                ): String {
                        val base = "flight_list/$origin/$destination/$departDate/$paxCount/$isRoundTrip"
                        return if (isRoundTrip && !returnDate.isNullOrBlank()) "$base?return_date=$returnDate" else base
                }
        }

        data object BookingDetails : Screen("booking_details/{flightId}") {
                fun createRoute(flightId: String) = "booking_details/$flightId"
        }


        data object HotelList : Screen(
                "hotel_list/{city_id}/{city_name}?min_stars={min_stars}"
        ) {
                fun createRoute(
                        cityId: String,
                        cityName: String,
                        minStars: Double? = null
                ): String {
                        val encodedName = java.net.URLEncoder.encode(cityName, "UTF-8")
                        val base = "hotel_list/$cityId/$encodedName"
                        return if (minStars != null) "$base?min_stars=$minStars" else base
                }
        }

        data object HotelDetails : Screen("hotel_details/{hotelId}") {
                fun createRoute(hotelId: String) = "hotel_details/$hotelId"
        }

        data object HotelGuestDetails : Screen("hotel_guest_details/{hotelId}") {
                fun createRoute(hotelId: String) = "hotel_guest_details/$hotelId"
        }

        data object SeatSelection : Screen(
                "seat_selection/{flightId}?travelers={travelers}&name={name}&email={email}&phone={phone}"
        ) {
                fun createRoute(flightId: String, travelers: Int, name: String, email: String, phone: String): String {
                        val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
                        val encodedEmail = java.net.URLEncoder.encode(email, "UTF-8")
                        return "seat_selection/$flightId?travelers=$travelers&name=$encodedName&email=$encodedEmail&phone=$phone"
                }
        }

        data object FlightConfirmPay : Screen(
                "flight_confirm_pay/{flightId}?travelers={travelers}&name={name}&email={email}&phone={phone}" +
                        "&seat={seat}&seatClass={seatClass}&seatPrice={seatPrice}"
        ) {
                fun createRoute(
                        flightId: String, travelers: Int, name: String, email: String, phone: String,
                        seat: String, seatClass: String, seatPrice: Double
                ): String {
                        val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
                        val encodedEmail = java.net.URLEncoder.encode(email, "UTF-8")
                        return "flight_confirm_pay/$flightId?travelers=$travelers&name=$encodedName&email=$encodedEmail" +
                                "&phone=$phone&seat=$seat&seatClass=$seatClass&seatPrice=$seatPrice"
                }
        }

        data object HotelConfirmPay : Screen(
                "hotel_confirm_pay/{hotelId}?guests={guests}&name={name}&email={email}&phone={phone}" +
                        "&nights={nights}&checkIn={checkIn}&checkOut={checkOut}&roomType={roomType}"
        ) {
                fun createRoute(
                        hotelId: String, guests: Int, name: String, email: String, phone: String,
                        nights: Int = 1, checkIn: String = "", checkOut: String = "", roomType: String = "Studio Suite"
                ): String {
                        val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
                        val encodedEmail = java.net.URLEncoder.encode(email, "UTF-8")
                        val encodedCheckIn = java.net.URLEncoder.encode(checkIn, "UTF-8")
                        val encodedCheckOut = java.net.URLEncoder.encode(checkOut, "UTF-8")
                        val encodedRoomType = java.net.URLEncoder.encode(roomType, "UTF-8")
                        return "hotel_confirm_pay/$hotelId?guests=$guests&name=$encodedName&email=$encodedEmail&phone=$phone" +
                                "&nights=$nights&checkIn=$encodedCheckIn&checkOut=$encodedCheckOut&roomType=$encodedRoomType"
                }
        }
}