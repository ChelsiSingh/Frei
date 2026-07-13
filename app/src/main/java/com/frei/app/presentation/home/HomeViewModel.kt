package com.frei.app.presentation.home

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
        stay: String
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
                onSuccess = {
                    _isLoading.value = false
                    viewModelScope.launch {
                        _saveSuccessEvent.emit(Unit)
                    }
                },
                onFailure = { e -> // FIXED: Explicitly typed closure parameter
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Failed to save trip"
                    e.printStackTrace()
                }
            )
        }
    }
}