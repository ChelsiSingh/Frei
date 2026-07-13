package com.frei.app.data.remote

import com.frei.app.data.model.flight.Airport
import com.frei.app.data.remote.api.FlightApiService
import com.frei.app.data.model.flight.AirportSearchResponse
import com.frei.app.data.model.flight.Flight
import com.frei.app.data.model.flight.FlightSearchResponse
import retrofit2.Response

class FlightRemoteDataSource(
    private val apiService: FlightApiService
) {
    // FIX: named argument must match the Kotlin parameter name (`query`),
    // not the HTTP query-string key (`"q"`) declared on the @Query annotation.
    suspend fun searchAirports(query: String): Response<AirportSearchResponse> =
        apiService.searchAirports(query = query)

    suspend fun getAirport(code: String): Response<Airport> =
        apiService.getAirport(code)

    suspend fun searchFlights(
        from: String,
        to: String,
        date: String?,
        travelers: Int
    ): Response<FlightSearchResponse> =
        apiService.searchFlights(from = from, to = to, date = date, travelers = travelers)

    suspend fun getFlight(flightId: String): Response<Flight> =
        apiService.getFlight(flightId)
}