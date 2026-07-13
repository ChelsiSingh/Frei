package com.frei.app.presentation.booking.hotel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.model.hotel.Hotel
import com.frei.app.data.repository.HotelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed interface HotelListUiState {
    data object Idle : HotelListUiState
    data object Loading : HotelListUiState
    data class Success(val hotels: List<Hotel>) : HotelListUiState
    data class Error(val message: String) : HotelListUiState
}

@HiltViewModel
class HotelListViewModel @Inject constructor(
    private val repository: HotelRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<HotelListUiState>(HotelListUiState.Loading)
    val uiState: StateFlow<HotelListUiState> = _uiState.asStateFlow()

    val cityId: String = savedStateHandle["city_id"] ?: ""
    val cityName: String = savedStateHandle["city_name"] ?: ""
    private val minStars: Double? = (savedStateHandle["min_stars"] as? String)?.toDoubleOrNull()

    init {
        search()
    }

    fun search() {
        if (cityId.isBlank()) {
            _uiState.value = HotelListUiState.Error("Missing search details. Please search again.")
            return
        }
        viewModelScope.launch {
            _uiState.value = HotelListUiState.Loading
            repository.searchHotelsInCity(cityId, minStars)
                .onSuccess { response -> _uiState.value = HotelListUiState.Success(response.items) }
                .onFailure { exception -> _uiState.value = HotelListUiState.Error(exception.toUserMessage()) }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is IOException -> "Check your internet connection and try again."
        is HttpException -> "Couldn't complete that request. Please try again."
        else -> message ?: "Something went wrong. Please try again."
    }
}