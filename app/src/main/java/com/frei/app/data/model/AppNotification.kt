package com.frei.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tripId: String? = null,
    val type: NotificationType = NotificationType.GENERAL,
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val read: Boolean = false
)

enum class NotificationType { GENERAL, TRIP_START, FLIGHT_REMINDER, HOTEL_CHECKIN, HOTEL_CHECKOUT }