package com.frei.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Matches the schema AuthRepository.register() writes to users/{uid}
data class UserProfile(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val contact: String = "",
    val gender: String = "",
    val address: String = "",
    val email: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserProfile? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = ProfileUiState(isLoading = false, errorMessage = "Not signed in.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val snapshot = firestore.collection("users").document(uid).get().await()
                val user = snapshot.toObject(UserProfile::class.java) ?: UserProfile()
                _uiState.value = ProfileUiState(isLoading = false, user = user)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    errorMessage = "Couldn't load your profile. Pull down to try again."
                )
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        auth.signOut()
        onSignedOut()
    }
}