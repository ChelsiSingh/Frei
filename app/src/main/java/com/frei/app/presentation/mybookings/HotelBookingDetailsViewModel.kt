package com.frei.app.presentation.mybookings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.repository.BookingRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HotelBookingDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookingRepository: BookingRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val hotelBookingId: String = savedStateHandle.get<String>("hotelBookingId").orEmpty()

    private val _uiState = MutableStateFlow<HotelBookingDetailsUiState?>(null)
    val uiState: StateFlow<HotelBookingDetailsUiState?> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _error.value = "You need to be signed in to view this booking."
            _isLoading.value = false
        } else {
            viewModelScope.launch {
                bookingRepository.getHotelBookings(uid)
                    .onSuccess { bookings ->
                        val match = bookings.firstOrNull { it.hotelId == hotelBookingId }
                        if (match == null) {
                            _error.value = "We couldn't find this booking."
                        } else {
                            _uiState.value = match.toHotelBookingDetailsUiState()
                        }
                    }
                    .onFailure { _error.value = it.message ?: "Something went wrong loading this booking." }
                _isLoading.value = false
            }
        }
    }
}