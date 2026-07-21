package com.frei.app.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.model.AppNotification
import com.frei.app.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    repository: NotificationRepository,
    auth: FirebaseAuth
) : ViewModel() {
    val notifications: StateFlow<List<AppNotification>> =
        repository.observeNotifications(auth.currentUser?.uid.orEmpty())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}