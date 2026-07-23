package com.frei.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ContactMessage(
    @DocumentId
    val id: String = "",
    val userId: String? = null,
    val name: String = "",
    val email: String = "",
    val subject: String = "",
    val message: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val status: String = "new"
)