package com.frei.app.presentation.booking.seat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.model.flight.SeatInfo
import com.frei.app.data.model.flight.SeatMapGenerator
import com.frei.app.data.model.flight.SeatStatus
import com.frei.app.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SeatSelectionUiState {
    data object Loading : SeatSelectionUiState
    data class Ready(
        val rows: List<List<SeatInfo>>,
        val selectedSeat: SeatInfo?,
        val flightRoute: String,
        val flightMeta: String
    ) : SeatSelectionUiState
    data class Failed(val message: String) : SeatSelectionUiState
}

@HiltViewModel
class SeatSelectionViewModel @Inject constructor(
    private val flightRepository: FlightRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val flightId: String = savedStateHandle["flightId"] ?: ""
    val travelers: Int = (savedStateHandle["travelers"] as? String)?.toIntOrNull() ?: 1
    val guestName: String = savedStateHandle["name"] ?: ""
    val guestEmail: String = savedStateHandle["email"] ?: ""
    val guestPhone: String = savedStateHandle["phone"] ?: ""

    private val _uiState = MutableStateFlow<SeatSelectionUiState>(SeatSelectionUiState.Loading)
    val uiState: StateFlow<SeatSelectionUiState> = _uiState.asStateFlow()

    init { loadSeatMap() }

    private fun loadSeatMap() {
        viewModelScope.launch {
            flightRepository.getFlight(flightId)
                .onSuccess { flight ->
                    _uiState.value = SeatSelectionUiState.Ready(
                        rows = SeatMapGenerator.generate(flightId),
                        selectedSeat = null,
                        flightRoute = "${flight.fromAirport} → ${flight.toAirport}",
                        flightMeta = "${flight.departureTime} · ${flight.flightNumber}"
                    )
                }
                .onFailure {
                    _uiState.value = SeatSelectionUiState.Failed("Couldn't load seat map.")
                }
        }
    }

    fun selectSeat(seat: SeatInfo) {
        if (seat.status == SeatStatus.OCCUPIED) return
        _uiState.update { state ->
            if (state !is SeatSelectionUiState.Ready) return@update state
            val newRows = state.rows.map { row ->
                row.map { s ->
                    when {
                        s.seatNumber == seat.seatNumber -> s.copy(status = SeatStatus.SELECTED)
                        s.status == SeatStatus.SELECTED -> s.copy(status = SeatStatus.AVAILABLE)
                        else -> s
                    }
                }
            }
            state.copy(
                rows = newRows,
                selectedSeat = newRows.flatten().firstOrNull { it.status == SeatStatus.SELECTED }
            )
        }
    }
}