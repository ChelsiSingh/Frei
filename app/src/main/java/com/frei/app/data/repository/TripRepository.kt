package com.frei.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val uid: String? get() = auth.currentUser?.uid

    fun addTrip(
        title: String,
        destination: String,
        departureDate: Long?,
        returnDate: Long?,
        travelers: Int,
        budget: String,
        transport: String,
        stay: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUid = uid
            ?: return onFailure(Exception("User not authenticated"))

        val tripRef = db.collection("trips")
            .document()

        val tripData = hashMapOf(
            "id" to tripRef.id,
            "userId" to currentUid,
            "title" to title,
            "destination" to destination,
            "departureDate" to departureDate,
            "returnDate" to returnDate,
            "travelers" to travelers,
            "budget" to budget,
            "transport" to transport,
            "stay" to stay
        )

        tripRef.set(tripData)
            .addOnSuccessListener {
                onSuccess(tripRef.id)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }


    fun getMyTrips(
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUid = uid ?: return onFailure(Exception("Not logged in"))

        db.collection("trips")
            .whereEqualTo("userId", currentUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val trips = querySnapshot.documents.map { it.data ?: emptyMap() }
                onSuccess(trips)
            }
            .addOnFailureListener { onFailure(it) }
    }
}