package com.frei.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
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
    val guestName: String = "",
    val guestEmail: String = "",
    val guestPhone: String = "",
    val seatNumber: String = "",
    val seatClass: String = "",
    val razorpayOrderId: String = "",
    val razorpayPaymentId: String = "",
    val bookedAt: Long = System.currentTimeMillis()
)

data class HotelBookingRecord(
    val tripId: String = "",
    val uid: String = "",
    val hotelId: String = "",
    val guests: Int = 1,
    val totalPrice: Double = 0.0,
    val guestName: String = "",
    val guestEmail: String = "",
    val guestPhone: String = "",
    val nights: Int = 1,
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val roomType: String = "",
    val razorpayOrderId: String = "",
    val razorpayPaymentId: String = "",
    val bookedAt: Long = System.currentTimeMillis()
)

interface BookingRepository {
    suspend fun getOrCreateTripId(uid: String): Result<String>
    suspend fun saveFlightBooking(record: FlightBookingRecord): Result<Unit>
    suspend fun saveHotelBooking(record: HotelBookingRecord): Result<Unit>
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
}