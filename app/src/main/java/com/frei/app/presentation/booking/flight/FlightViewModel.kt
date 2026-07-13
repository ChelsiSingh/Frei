package com.frei.app.presentation.booking.flight

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.model.flight.Airport
import com.frei.app.data.model.flight.Flight
import com.frei.app.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

sealed interface FlightSearchUiState {
    data object Idle : FlightSearchUiState
    data object Loading : FlightSearchUiState
    data class Success(val flights: List<Flight>) : FlightSearchUiState
    data class Error(val message: String) : FlightSearchUiState
}

sealed interface AirportUiState {
    data object Idle : AirportUiState
    data object Loading : AirportUiState
    data class Success(val airports: List<Airport>) : AirportUiState
    data class Error(val message: String) : AirportUiState
}

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val repository: FlightRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _searchState = MutableStateFlow<FlightSearchUiState>(FlightSearchUiState.Idle)
    val searchState: StateFlow<FlightSearchUiState> = _searchState.asStateFlow()

    private val _isRoundTrip = MutableStateFlow(false)
    val isRoundTrip: StateFlow<Boolean> = _isRoundTrip.asStateFlow()

    private val _originQuery = MutableStateFlow("")
    private val _destinationQuery = MutableStateFlow("")

    private val _originState = MutableStateFlow<AirportUiState>(AirportUiState.Idle)
    val originState: StateFlow<AirportUiState> = _originState.asStateFlow()

    private val _destinationState = MutableStateFlow<AirportUiState>(AirportUiState.Idle)
    val destinationState: StateFlow<AirportUiState> = _destinationState.asStateFlow()

    val initialOrigin: String = savedStateHandle["origin"] ?: "BOM"
    val initialDestination: String = savedStateHandle["destination"] ?: "DPS"
    val initialDepartureDate: String = savedStateHandle["depart_date"] ?: "2026-07-24"
    val initialIsRoundTrip: Boolean = savedStateHandle["is_round_trip"] ?: false
    val initialPaxCount: Int = savedStateHandle["pax_count"] ?: 1

    private var searchJob: Job? = null
    private var originJob: Job? = null
    private var destinationJob: Job? = null

    init {
        _isRoundTrip.value = initialIsRoundTrip
        observeOriginQuery()
        observeDestinationQuery()
        searchFlights(
            from = initialOrigin,
            to = initialDestination,
            date = initialDepartureDate,
            travelers = initialPaxCount
        )
    }

    fun setRoundTrip(value: Boolean) { _isRoundTrip.value = value }

    fun clearOriginSuggestions() {
        originJob?.cancel()
        _originState.value = AirportUiState.Idle
    }

    fun clearDestinationSuggestions() {
        destinationJob?.cancel()
        _destinationState.value = AirportUiState.Idle
    }

    @OptIn(FlowPreview::class)
    private fun observeOriginQuery() {
        _originQuery.debounce(400.milliseconds).distinctUntilChanged()
            .onEach { query -> searchOriginAirports(query) }.launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class)
    private fun observeDestinationQuery() {
        _destinationQuery.debounce(400.milliseconds).distinctUntilChanged()
            .onEach { query -> searchDestinationAirports(query) }.launchIn(viewModelScope)
    }

    fun onOriginQueryChanged(query: String) { _originQuery.value = query }
    fun onDestinationQueryChanged(query: String) { _destinationQuery.value = query }

    fun searchFlights(from: String, to: String, date: String?, travelers: Int) {
        if (from.isBlank() || to.isBlank()) {
            _searchState.value = FlightSearchUiState.Error("Please choose an origin and destination.")
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.value = FlightSearchUiState.Loading
            repository.searchFlights(from, to, date, travelers)
                .onSuccess { response -> _searchState.value = FlightSearchUiState.Success(response.items) }
                .onFailure { exception -> _searchState.value = FlightSearchUiState.Error(exception.toUserMessage()) }
        }
    }

    private fun searchOriginAirports(query: String) {
        if (query.isBlank()) { _originState.value = AirportUiState.Idle; return }
        originJob?.cancel()
        originJob = viewModelScope.launch {
            _originState.value = AirportUiState.Loading
            repository.searchAirports(query)
                .onSuccess { response -> _originState.value = AirportUiState.Success(response.items) }
                .onFailure { exception -> _originState.value = AirportUiState.Error(exception.toUserMessage()) }
        }
    }

    private fun searchDestinationAirports(query: String) {
        if (query.isBlank()) { _destinationState.value = AirportUiState.Idle; return }
        destinationJob?.cancel()
        destinationJob = viewModelScope.launch {
            _destinationState.value = AirportUiState.Loading
            repository.searchAirports(query)
                .onSuccess { response -> _destinationState.value = AirportUiState.Success(response.items) }
                .onFailure { exception -> _destinationState.value = AirportUiState.Error(exception.toUserMessage()) }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is IOException -> "Check your internet connection and try again."
        is HttpException -> "Couldn't complete that request. Please try again."
        else -> message ?: "Something went wrong. Please try again."
    }
}