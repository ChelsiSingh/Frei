package com.frei.app.presentation.mytrips

enum class TripTab(val title: String) {
    Trip("Trip"),
    Packing("Packing"),
    Bookings("Bookings"),
    Expenses("Expenses")
}

enum class BookingMode { FLIGHT, HOTEL }

enum class BookingStatus(val label: String) {
    CONFIRMED("Confirmed"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed")
}