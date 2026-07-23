package com.frei.app.presentation.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: TripRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun saveTrip(
        title: String,
        destination: String,
        departureDate: Long?,
        returnDate: Long?,
        travelers: Int,
        budget: String,
        transport: String,
        stay: String,
        tripType: String = "Leisure",
        notes: String = "",
        coverImageUri: Uri? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repo.addTrip(
                title = title,
                destination = destination,
                departureDate = departureDate,
                returnDate = returnDate,
                travelers = travelers,
                budget = budget,
                transport = transport,
                stay = stay,
                tripType = tripType,
                notes = notes,
                coverImageUri = coverImageUri,
                onSuccess = {
                    _isLoading.value = false
                    viewModelScope.launch {
                        _saveSuccessEvent.emit(Unit)
                    }
                },
                onFailure = { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Failed to save trip"
                    e.printStackTrace()
                }
            )
        }
    }
}