package com.frei.app.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseUser

class AuthGateState internal constructor(
    private val currentUser: () -> FirebaseUser?
) {
    var showPrompt by mutableStateOf(false)
        private set

    var showAuthSheet by mutableStateOf(false)
        private set

    private var pendingAction: (() -> Unit)? = null

    fun requireAuth(action: () -> Unit) {
        if (currentUser() != null) {
            action()
        } else {
            pendingAction = action
            showPrompt = true
        }
    }

    fun onLoginClicked() {
        showPrompt = false
        showAuthSheet = true
    }

    fun onAuthSuccess() {
        showPrompt = false
        showAuthSheet = false
        val action = pendingAction
        pendingAction = null
        action?.invoke()
    }

    fun dismiss() {
        showPrompt = false
        showAuthSheet = false
        pendingAction = null
    }
}

@Composable
fun rememberAuthGateState(currentUser: () -> FirebaseUser?): AuthGateState {
    return remember { AuthGateState(currentUser) }
}