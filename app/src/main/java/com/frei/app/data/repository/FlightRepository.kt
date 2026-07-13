package com.frei.app.data.repository

import com.frei.app.data.model.flight.AirportSearchResponse
import com.frei.app.data.model.flight.Flight
import com.frei.app.data.model.flight.FlightSearchResponse

interface FlightRepository {
    suspend fun saveBookedFlight(tripId: String, flight: Flight): Result<Unit>
    suspend fun getBookedFlightForTrip(tripId: String): Result<Flight?>
    suspend fun searchAirports(query: String): Result<AirportSearchResponse>
    suspend fun searchFlights(from: String, to: String, date: String?, travelers: Int): Result<FlightSearchResponse>
    suspend fun getFlight(flightId: String): Result<Flight> // new — BookingDetailsScreen needs this
}