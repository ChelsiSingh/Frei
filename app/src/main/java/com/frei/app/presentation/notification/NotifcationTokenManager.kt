package com.frei.app.presentation.notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

object NotificationTokenManager {
    fun registerCurrentToken(firestore: FirebaseFirestore, auth: FirebaseAuth) {
        val uid = auth.currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            firestore.collection("users").document(uid)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
        }
    }
}