package com.frei.app.data.model

data class Trip(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val destination: String = "",
    val departureDate: Long? = null,
    val returnDate: Long? = null,
    val travelers: Int = 1,
    val budget: String = "",
    val transport: String = "Flight",
    val stay: String = "Hotel"
)