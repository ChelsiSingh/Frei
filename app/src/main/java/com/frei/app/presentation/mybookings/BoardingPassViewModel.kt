package com.frei.app.presentation.mybookings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.repository.BookingRepository
import com.frei.app.presentation.mybookings.BoardingPassUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardingPassViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookingRepository: BookingRepository,
    private val auth: FirebaseAuth // assumes FirebaseAuth is already Hilt-provided elsewhere
) : ViewModel() {

    private val flightId: String = savedStateHandle.get<String>("flightId").orEmpty()

    private val _uiState = MutableStateFlow<BoardingPassUiState?>(null)
    val uiState: StateFlow<BoardingPassUiState?> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _error.value = "You need to be signed in to view this boarding pass."
            _isLoading.value = false
        } else {
            viewModelScope.launch {
                bookingRepository.getFlightBookings(uid)
                    .onSuccess { bookings ->
                        val match = bookings.firstOrNull { it.flightId == flightId }
                        if (match == null) {
                            _error.value = "We couldn't find this booking."
                        } else {
                            _uiState.value = match.toBoardingPassUiState()
                        }
                    }
                    .onFailure { _error.value = it.message ?: "Something went wrong loading your boarding pass." }
                _isLoading.value = false
            }
        }
    }
}