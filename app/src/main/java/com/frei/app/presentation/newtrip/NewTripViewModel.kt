package com.frei.app.presentation.newtrip

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.frei.app.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewTripViewModel @Inject constructor(
    private val tripRepository: TripRepository // ✅ Injected Repository
) : ViewModel() {


    var tripName by mutableStateOf("")
        private set

    var destination by mutableStateOf("")
        private set

    var departureDate by mutableStateOf<Long?>(null)
        private set

    var returnDate by mutableStateOf<Long?>(null)
        private set

    var travelers by mutableIntStateOf(1)
        private set

    var budget by mutableStateOf("")
        private set

    var transport by mutableStateOf("Flight")
        private set

    var stay by mutableStateOf("Hotel")
        private set


    fun onTripNameChange(newValue: String) {
        tripName = newValue
    }

    fun onDestinationChange(newValue: String) {
        destination = newValue
    }

    fun updateDepartureDate(timestamp: Long?) {
        departureDate = timestamp
    }

    fun updateReturnDate(timestamp: Long?) {
        returnDate = timestamp
    }

    fun increaseTravelers() {
        travelers += 1
    }

    fun decreaseTravelers() {
        if (travelers > 1) {
            travelers -= 1
        }
    }

    fun onBudgetChange(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            budget = newValue
        }
    }

    fun updateTransport(selectedTransport: String) {
        transport = selectedTransport
    }

    fun updateStay(selectedStay: String) {
        stay = selectedStay
    }


    fun saveTripToFirestore(
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        tripRepository.addTrip(
            title = tripName,
            destination = destination,
            departureDate = departureDate,
            returnDate = returnDate,
            travelers = travelers,
            budget = budget,
            transport = transport,
            stay = stay,
            onSuccess = { generatedTripId ->
                onSuccess(generatedTripId)
            },
            onFailure = { exception ->
                onFailure(exception)
            }
        )
    }
}