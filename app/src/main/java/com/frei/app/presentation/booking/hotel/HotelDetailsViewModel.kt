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

sealed interface HotelDetailUiState {
    data object Loading : HotelDetailUiState
    data class Success(val hotel: Hotel) : HotelDetailUiState
    data class Error(val message: String) : HotelDetailUiState
}

@HiltViewModel
class HotelDetailsViewModel @Inject constructor(
    private val repository: HotelRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<HotelDetailUiState>(HotelDetailUiState.Loading)
    val uiState: StateFlow<HotelDetailUiState> = _uiState.asStateFlow()

    val hotelId: String = savedStateHandle["hotelId"] ?: ""

    init { loadHotel() }

    private fun loadHotel() {
        if (hotelId.isBlank()) {
            _uiState.value = HotelDetailUiState.Error("Missing hotel reference.")
            return
        }
        viewModelScope.launch {
            _uiState.value = HotelDetailUiState.Loading
            repository.getHotel(hotelId)
                .onSuccess { hotel -> _uiState.value = HotelDetailUiState.Success(hotel) }
                .onFailure { exception -> _uiState.value = HotelDetailUiState.Error(exception.toUserMessage()) }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is IOException -> "Check your internet connection and try again."
        is HttpException -> "Couldn't find that hotel. Please go back and try again."
        else -> message ?: "Something went wrong. Please try again."
    }
}