package com.frei.app.presentation.mybookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.repository.BookingRepository
import com.frei.app.data.repository.FlightBookingRecord
import com.frei.app.data.repository.HotelBookingRecord
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingsUiState(
    val isLoading: Boolean = true,
    val flightBookings: List<FlightBookingRecord> = emptyList(),
    val hotelBookings: List<HotelBookingRecord> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingsUiState())
    val uiState: StateFlow<BookingsUiState> = _uiState.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = BookingsUiState(
                isLoading = false,
                errorMessage = "Sign in to see your bookings."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val flightsResult = bookingRepository.getFlightBookings(uid)
            val hotelsResult = bookingRepository.getHotelBookings(uid)

            val bothFailed = flightsResult.isFailure && hotelsResult.isFailure

            _uiState.value = BookingsUiState(
                isLoading = false,
                flightBookings = flightsResult.getOrElse { emptyList() },
                hotelBookings = hotelsResult.getOrElse { emptyList() },
                errorMessage = if (bothFailed) {
                    "Couldn't load your bookings. Pull down to try again."
                } else null
            )
        }
    }
}