package com.frei.app.presentation.booking.hotel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.model.hotel.City
import com.frei.app.data.repository.CityRepository
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

sealed interface CitySuggestionUiState {
    data object Idle : CitySuggestionUiState
    data object Loading : CitySuggestionUiState
    data class Success(val cities: List<City>) : CitySuggestionUiState
    data class Error(val message: String) : CitySuggestionUiState
}

@HiltViewModel
class HotelViewModel @Inject constructor(
    private val cityRepository: CityRepository
) : ViewModel() {

    private val _cityQuery = MutableStateFlow("")
    private val _citySuggestionState = MutableStateFlow<CitySuggestionUiState>(CitySuggestionUiState.Idle)
    val citySuggestionState: StateFlow<CitySuggestionUiState> = _citySuggestionState.asStateFlow()

    private var searchJob: Job? = null
    private var citySearchJob: Job? = null

    init {
        observeCityQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeCityQuery() {
        _cityQuery
            .debounce(400.milliseconds)
            .distinctUntilChanged()
            .onEach { query -> searchCities(query) }
            .launchIn(viewModelScope)
    }

    fun onCityQueryChanged(query: String) { _cityQuery.value = query }

    fun clearCitySuggestions() {
        citySearchJob?.cancel()
        _citySuggestionState.value = CitySuggestionUiState.Idle
    }

    private fun searchCities(query: String) {
        if (query.isBlank()) { _citySuggestionState.value = CitySuggestionUiState.Idle; return }
        citySearchJob?.cancel()
        citySearchJob = viewModelScope.launch {
            _citySuggestionState.value = CitySuggestionUiState.Loading
            cityRepository.searchCities(query)
                .onSuccess { response -> _citySuggestionState.value = CitySuggestionUiState.Success(response.items) }
                .onFailure { exception -> _citySuggestionState.value = CitySuggestionUiState.Error(exception.toUserMessage()) }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is IOException -> "Check your internet connection and try again."
        is HttpException -> "Couldn't complete that request. Please try again."
        else -> message ?: "Something went wrong. Please try again."
    }
}