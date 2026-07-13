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

sealed interface FlightListUiState {
    data object Loading : FlightListUiState
    data class Success(val flights: List<Flight>) : FlightListUiState
    data class Error(val message: String) : FlightListUiState
}

@HiltViewModel
class FlightListViewModel @Inject constructor(
    private val repository: FlightRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<FlightListUiState>(FlightListUiState.Loading)
    val uiState: StateFlow<FlightListUiState> = _uiState.asStateFlow()

    val origin: String = savedStateHandle["origin"] ?: ""
    val destination: String = savedStateHandle["destination"] ?: ""
    val departDate: String = savedStateHandle["depart_date"] ?: ""
    val paxCount: Int = savedStateHandle["pax_count"] ?: 1

    init {
        search()
    }

    fun search() {
        if (origin.isBlank() || destination.isBlank()) {
            _uiState.value = FlightListUiState.Error("Missing search details. Please search again.")
            return
        }
        viewModelScope.launch {
            _uiState.value = FlightListUiState.Loading
            repository.searchFlights(origin, destination, departDate.ifBlank { null }, paxCount)
                .onSuccess { response -> _uiState.value = FlightListUiState.Success(response.items) }
                .onFailure { exception -> _uiState.value = FlightListUiState.Error(exception.toUserMessage()) }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is IOException -> "Check your internet connection and try again."
        is HttpException -> "Couldn't complete that request. Please try again."
        else -> message ?: "Something went wrong. Please try again."
    }
}