package com.frei.app.presentation.booking.flight

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.model.flight.Flight
import com.frei.app.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed interface FlightDetailUiState {
    data object Loading : FlightDetailUiState
    data class Success(val flight: Flight) : FlightDetailUiState
    data class Error(val message: String) : FlightDetailUiState
}

@HiltViewModel
class BookingDetailsViewModel @Inject constructor(
    private val repository: FlightRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<FlightDetailUiState>(FlightDetailUiState.Loading)
    val uiState: StateFlow<FlightDetailUiState> = _uiState.asStateFlow()

    val flightId: String = savedStateHandle["flightId"] ?: ""

    init {
        loadFlight()
    }

    private fun loadFlight() {
        if (flightId.isBlank()) {
            _uiState.value = FlightDetailUiState.Error("Missing flight reference.")
            return
        }
        viewModelScope.launch {
            _uiState.value = FlightDetailUiState.Loading
            repository.getFlight(flightId)
                .onSuccess { flight -> _uiState.value = FlightDetailUiState.Success(flight) }
                .onFailure { exception -> _uiState.value = FlightDetailUiState.Error(exception.toUserMessage()) }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is IOException -> "Check your internet connection and try again."
        is HttpException -> "Couldn't find that flight. Please go back and try again."
        else -> message ?: "Something went wrong. Please try again."
    }
}