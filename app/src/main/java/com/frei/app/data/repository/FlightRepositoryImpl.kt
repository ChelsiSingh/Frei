package com.frei.app.data.repository

import com.frei.app.data.model.flight.AirportSearchResponse
import com.frei.app.data.model.flight.Flight
import com.frei.app.data.model.flight.FlightSearchResponse
import com.frei.app.data.remote.FlightRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val api: FlightRemoteDataSource
) : FlightRepository {

    private fun <T> Response<T>.getOrThrow(): T {
        if (isSuccessful) return body() ?: throw Exception("Response body was empty")
        throw Exception(errorBody()?.string() ?: "Network call failed with code ${code()}")
    }

    override suspend fun saveBookedFlight(tripId: String, flight: Flight): Result<Unit> = runCatching {
        firestore.collection("flightbooking").document(tripId).set(flight).await()
        Unit
    }

    override suspend fun getBookedFlightForTrip(tripId: String): Result<Flight?> = runCatching {
        val document = firestore.collection("flightbooking").document(tripId).get().await()
        if (document.exists()) document.toObject(Flight::class.java) else null
    }

    override suspend fun searchAirports(query: String): Result<AirportSearchResponse> = runCatching {
        api.searchAirports(query).getOrThrow()
    }

    override suspend fun searchFlights(from: String, to: String, date: String?, travelers: Int): Result<FlightSearchResponse> = runCatching {
        api.searchFlights(from, to, date, travelers).getOrThrow()
    }

    override suspend fun getFlight(flightId: String): Result<Flight> = runCatching {
        api.getFlight(flightId).getOrThrow()
    }
}