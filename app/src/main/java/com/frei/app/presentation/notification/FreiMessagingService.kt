package com.frei.app.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.frei.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FreiMessagingService : FirebaseMessagingService() {

    @Inject lateinit var firestore: FirebaseFirestore
    @Inject lateinit var auth: FirebaseAuth

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "frei_reminders"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Trip Reminders", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}