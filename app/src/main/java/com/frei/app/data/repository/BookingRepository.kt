package com.frei.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class FlightBookingRecord(
    val tripId: String = "",
    val uid: String = "",
    val flightId: String = "",
    val travelers: Int = 1,
    val totalPrice: Double = 0.0,
    val currency: String = "",
    val guestName: String = "",
    val guestEmail: String = "",
    val guestPhone: String = "",
    val seatNumber: String = "",
    val seatClass: String = "",
    // Denormalized flight details so the Bookings list doesn't need a join back to /flightDetails-search per row
    val airline: String = "",
    val airlineCode: String = "",
    val flightNumber: String = "",
    val fromAirport: String = "",
    val toAirport: String = "",
    val departureTime: String = "", // raw ISO string from Flight.departureTime, e.g. 2026-07-24T15:45:00.000Z
    val arrivalTime: String = "",
    val razorpayOrderId: String = "",
    val razorpayPaymentId: String = "",
    val bookedAt: Long = System.currentTimeMillis() // when the booking/payment was made
)

data class HotelBookingRecord(
    val tripId: String = "",
    val uid: String = "",
    val hotelId: String = "",
    val guests: Int = 1,
    val totalPrice: Double = 0.0,
    val currency: String = "",
    val guestName: String = "",
    val guestEmail: String = "",
    val guestPhone: String = "",
    val nights: Int = 1,
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val roomType: String = "",
    // Denormalized hotel details for the Bookings list
    val hotelName: String = "",
    val cityId: String = "",
    val address: String = "",
    val image: String? = null,
    val razorpayOrderId: String = "",
    val razorpayPaymentId: String = "",
    val bookedAt: Long = System.currentTimeMillis()
)

interface BookingRepository {
    suspend fun getOrCreateTripId(uid: String): Result<String>
    suspend fun saveFlightBooking(record: FlightBookingRecord): Result<Unit>
    suspend fun saveHotelBooking(record: HotelBookingRecord): Result<Unit>
    suspend fun getFlightBookings(uid: String, tripId: String? = null): Result<List<FlightBookingRecord>>
    suspend fun getHotelBookings(uid: String, tripId: String? = null): Result<List<HotelBookingRecord>>
}

class BookingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookingRepository {

    override suspend fun getOrCreateTripId(uid: String): Result<String> = runCatching {
        val userDocRef = firestore.collection("users").document(uid)
        val snapshot = userDocRef.get().await()
        val existing = snapshot.getString("currentTripId")
        if (existing != null) return@runCatching existing

        val newTripId = UUID.randomUUID().toString()
        userDocRef.set(mapOf("currentTripId" to newTripId), SetOptions.merge()).await()
        newTripId
    }

    override suspend fun saveFlightBooking(record: FlightBookingRecord): Result<Unit> = runCatching {
        firestore.collection("flightDetails").add(record).await()
        Unit
    }

    override suspend fun saveHotelBooking(record: HotelBookingRecord): Result<Unit> = runCatching {
        firestore.collection("hotelDetails").add(record).await()
        Unit
    }

    // tripId == null -> all bookings for this user across every trip (used by the
    // top-level all-trips BookingsScreen).
    // tripId != null -> scoped to a single trip (used by MyTripScreen's Bookings tab).
    // The scoped path requires a composite index on (uid, tripId, bookedAt) —
    // Firestore will prompt for it via a Logcat link on first run after this change.
    // The unscoped path only needs (uid, bookedAt), which should already exist.
    override suspend fun getFlightBookings(uid: String, tripId: String?): Result<List<FlightBookingRecord>> = runCatching {
        var query = firestore.collection("flightDetails")
            .whereEqualTo("uid", uid)
        if (tripId != null) {
            query = query.whereEqualTo("tripId", tripId)
        }
        query
            .orderBy("bookedAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(FlightBookingRecord::class.java)
    }

    override suspend fun getHotelBookings(uid: String, tripId: String?): Result<List<HotelBookingRecord>> = runCatching {
        var query = firestore.collection("hotelDetails")
            .whereEqualTo("uid", uid)
        if (tripId != null) {
            query = query.whereEqualTo("tripId", tripId)
        }
        query
            .orderBy("bookedAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(HotelBookingRecord::class.java)
    }
}