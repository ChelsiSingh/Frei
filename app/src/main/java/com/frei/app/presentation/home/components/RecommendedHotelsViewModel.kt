package com.frei.app.presentation.home.components

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.repository.HotelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RecommendedHotelsUiState {
    data object Loading : RecommendedHotelsUiState
    data class Success(val hotels: List<RecommendedHotel>) : RecommendedHotelsUiState
}

@HiltViewModel
class RecommendedHotelsViewModel @Inject constructor(
    private val repository: HotelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecommendedHotelsUiState>(RecommendedHotelsUiState.Loading)
    val uiState: StateFlow<RecommendedHotelsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load(cityId: String? = null) {
        viewModelScope.launch {
            _uiState.value = RecommendedHotelsUiState.Loading
            repository.getRecommendedHotels(cityId)
                .onSuccess { response ->
                    Log.d("RecommendedHotels", "Got ${response.items.size} hotels")
                    _uiState.value = RecommendedHotelsUiState.Success(response.items.map { it.toRecommendedHotel() })
                }
                .onFailure {
                    Log.e("RecommendedHotels", "FAILED", it)
                    _uiState.value = RecommendedHotelsUiState.Success(emptyList())
                }
        }
    }
}

private fun com.frei.app.data.model.hotel.Hotel.toRecommendedHotel() = RecommendedHotel(
    id = id,
    name = name,
    city = cityId, // TODO: swap for a real city name lookup if you have city data cached
    address = address,
    rating = userRating.toString(),
    price = "₹${"%,d".format(pricePerNight)}",
    imageUrl = image,
    host = "",
    description = "",
    amenities = amenities
)