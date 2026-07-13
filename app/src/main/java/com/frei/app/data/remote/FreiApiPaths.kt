package com.frei.app.data.remote

object FreiApiPaths {
    const val BASE_URL = "https://frei.onrender.com/"

    const val CITIES = "cities"
    const val CITY_BY_ID = "cities/{cityId}"
    const val CITY_HOTELS = "cities/{cityId}/hotels"
    const val CITY_ATTRACTIONS = "cities/{cityId}/attractions"

    const val AIRPORTS = "airports"
    const val AIRPORT_BY_CODE = "airports/{code}"

    const val FLIGHTS = "flights"
    const val FLIGHT_BY_ID = "flights/{flightId}"

    // TODO: wire up once Hotel/Attraction/Booking/NewBooking schemas are shared
    const val HOTELS = "hotels"
    const val HOTEL_BY_ID = "hotels/{hotelId}"

    const val BOOKINGS = "bookings"
    const val BOOKING_BY_ID = "bookings/{bookingId}"
}