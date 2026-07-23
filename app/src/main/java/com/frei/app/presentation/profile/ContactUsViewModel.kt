package com.frei.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.model.ContactMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ContactUsUiState(
    val name: String = "",
    val email: String = "",
    val subject: String = "",
    val message: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val messageError: String? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ContactUsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactUsUiState())
    val uiState: StateFlow<ContactUsUiState> = _uiState.asStateFlow()

    init {
        auth.currentUser?.email?.let { email ->
            _uiState.value = _uiState.value.copy(email = email)
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null)
    }

    fun onSubjectChange(value: String) {
        _uiState.value = _uiState.value.copy(subject = value)
    }

    fun onMessageChange(value: String) {
        _uiState.value = _uiState.value.copy(message = value, messageError = null)
    }

    fun submit() {
        val state = _uiState.value

        val nameError = if (state.name.isBlank()) "Please enter your name" else null
        val emailError = when {
            state.email.isBlank() -> "Please enter your email"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Enter a valid email"
            else -> null
        }
        val messageError = if (state.message.isBlank()) "Please enter a message" else null

        if (nameError != null || emailError != null || messageError != null) {
            _uiState.value = state.copy(
                nameError = nameError,
                emailError = emailError,
                messageError = messageError
            )
            return
        }

        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val contactMessage = ContactMessage(
                    userId = auth.currentUser?.uid,
                    name = state.name.trim(),
                    email = state.email.trim(),
                    subject = state.subject.trim(),
                    message = state.message.trim()
                )
                firestore.collection("contact_messages")
                    .add(contactMessage)
                    .await()

                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    isSubmitted = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = e.localizedMessage ?: "Something went wrong. Please try again."
                )
            }
        }
    }

    fun resetSubmission() {
        _uiState.value = ContactUsUiState(email = auth.currentUser?.email ?: "")
    }
}