package com.frei.app.data.model.flight

import com.google.gson.annotations.SerializedName

data class AirportSearchResponse(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("items") val items: List<Airport> = emptyList()
)

data class Airport(
    @SerializedName("code") val code: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("cityId") val cityId: String = "",
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0
)

data class FlightSearchResponse(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("items") val items: List<Flight> = emptyList()
)

data class Flight(
    @SerializedName("id") val id: String = "",
    @SerializedName("airline") val airline: String = "",
    @SerializedName("airlineCode") val airlineCode: String = "",
    @SerializedName("flightNumber") val flightNumber: String = "",
    @SerializedName("fromAirport") val fromAirport: String = "",
    @SerializedName("toAirport") val toAirport: String = "",
    @SerializedName("departureTime") val departureTime: String = "",
    @SerializedName("arrivalTime") val arrivalTime: String = "",
    @SerializedName("durationMinutes") val durationMinutes: Int = 0,
    @SerializedName("stops") val stops: Int = 0,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("currency") val currency: String = "",
    @SerializedName("cabinClass") val cabinClass: String = "",
    @SerializedName("seatsAvailable") val seatsAvailable: Int = 0
)