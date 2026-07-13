package com.frei.app.data.remote.api

import com.frei.app.data.model.flight.Airport
import com.frei.app.data.model.flight.AirportSearchResponse
import com.frei.app.data.model.flight.Flight
import com.frei.app.data.model.flight.FlightSearchResponse
import com.frei.app.data.remote.FreiApiPaths
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FlightApiService {

    @GET(FreiApiPaths.AIRPORTS)
    suspend fun searchAirports(
        @Query("q") query: String? = null,
        @Query("cityId") cityId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<AirportSearchResponse>

    @GET(FreiApiPaths.AIRPORT_BY_CODE)
    suspend fun getAirport(@Path("code") code: String): Response<Airport>

    @GET(FreiApiPaths.FLIGHTS)
    suspend fun searchFlights(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("date") date: String? = null,
        @Query("travelers") travelers: Int = 1,
        @Query("sort") sort: String? = "price",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<FlightSearchResponse>

    @GET(FreiApiPaths.FLIGHT_BY_ID)
    suspend fun getFlight(@Path("flightId") flightId: String): Response<Flight>
}