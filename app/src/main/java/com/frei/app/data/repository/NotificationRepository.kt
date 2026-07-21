package com.frei.app.data.repository

import com.frei.app.data.model.AppNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<AppNotification>>
}

@Singleton
class FirestoreNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    private companion object {
        const val NOTIFICATIONS_COLLECTION = "notifications"
    }

    override fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val listener = firestore.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(AppNotification::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }
}