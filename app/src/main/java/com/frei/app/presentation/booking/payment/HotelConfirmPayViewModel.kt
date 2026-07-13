package com.frei.app.presentation.booking.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.model.hotel.Hotel
import com.frei.app.data.repository.BookingRepository
import com.frei.app.data.repository.HotelBookingRecord
import com.frei.app.data.repository.HotelRepository
import com.frei.app.data.repository.PaymentRepository
import com.frei.app.payment.RazorpayPaymentManager
import com.frei.app.payment.RazorpayResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HotelPaymentUiState {
    data object Loading : HotelPaymentUiState
    data class Ready(
        val hotel: Hotel,
        val roomCost: Double,
        val taxesAndCharges: Double,
        val totalPrice: Double
    ) : HotelPaymentUiState
    data object Processing : HotelPaymentUiState
    data object Success : HotelPaymentUiState
    data class Failed(val message: String) : HotelPaymentUiState
}

@HiltViewModel
class HotelConfirmPayViewModel @Inject constructor(
    private val hotelRepository: HotelRepository,
    private val paymentRepository: PaymentRepository,
    private val bookingRepository: BookingRepository,
    private val auth: FirebaseAuth,
    val paymentManager: RazorpayPaymentManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val hotelId: String = savedStateHandle["hotelId"] ?: ""
    val guests: Int = (savedStateHandle["guests"] as? String)?.toIntOrNull() ?: 1
    val guestPhone: String = savedStateHandle["phone"] ?: ""

    val nights: Int = (savedStateHandle["nights"] as? String)?.toIntOrNull() ?: 1

    private val _uiState = MutableStateFlow<HotelPaymentUiState>(HotelPaymentUiState.Loading)
    val uiState: StateFlow<HotelPaymentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            hotelRepository.getHotel(hotelId)
                .onSuccess { hotel ->
                    val roomCost = hotel.pricePerNight.toDouble() * nights
                    val taxesAndCharges = roomCost * TAX_RATE
                    _uiState.value = HotelPaymentUiState.Ready(
                        hotel = hotel,
                        roomCost = roomCost,
                        taxesAndCharges = taxesAndCharges,
                        totalPrice = roomCost + taxesAndCharges
                    )
                }
                .onFailure { _uiState.value = HotelPaymentUiState.Failed("Couldn't load booking details.") }
        }
        viewModelScope.launch {
            paymentManager.results.collect { result ->
                when (result) {
                    is RazorpayResult.Success -> verify(result)
                    is RazorpayResult.Failure -> _uiState.value = HotelPaymentUiState.Failed(result.description)
                }
            }
        }
    }

    fun createOrder(onOrderReady: (orderId: String, keyId: String, amountPaise: Int) -> Unit) {
        val state = _uiState.value as? HotelPaymentUiState.Ready ?: return
        viewModelScope.launch {
            _uiState.value = HotelPaymentUiState.Processing
            paymentRepository.createOrder(state.totalPrice, receipt = "hotel_$hotelId")
                .onSuccess { order -> onOrderReady(order.orderId, order.keyId, order.amount) }
                .onFailure { _uiState.value = HotelPaymentUiState.Failed(it.message ?: "Could not start payment.") }
        }
    }

    private fun verify(result: RazorpayResult.Success) {
        viewModelScope.launch {
            paymentRepository.verifyPayment(result.orderId, result.paymentId, result.signature)
                .onSuccess { verified ->
                    if (!verified) {
                        _uiState.value = HotelPaymentUiState.Failed("Payment couldn't be verified. If money was deducted, it will be refunded.")
                        return@onSuccess
                    }
                    saveBooking(result)
                }
                .onFailure { _uiState.value = HotelPaymentUiState.Failed(it.message ?: "Verification failed.") }
        }
    }

    private fun String.urlDecoded(): String =
        try { java.net.URLDecoder.decode(this, "UTF-8") } catch (e: Exception) { this }

    val guestName: String = (savedStateHandle["name"] ?: "").urlDecoded()
    val guestEmail: String = (savedStateHandle["email"] ?: "").urlDecoded()
    val roomType: String = (savedStateHandle["roomType"] ?: "Studio Suite").urlDecoded()
    val checkInDate: String = (savedStateHandle["checkIn"] ?: "").urlDecoded()
    val checkOutDate: String = (savedStateHandle["checkOut"] ?: "").urlDecoded()

    private suspend fun saveBooking(result: RazorpayResult.Success) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = HotelPaymentUiState.Failed(
                "Payment succeeded but you're signed out, so the booking couldn't be saved. " +
                        "Contact support with payment ID ${result.paymentId}."
            )
            return
        }
        val totalPrice = (_uiState.value as? HotelPaymentUiState.Ready)?.totalPrice ?: 0.0

        bookingRepository.getOrCreateTripId(uid)
            .mapCatching { tripId ->
                bookingRepository.saveHotelBooking(
                    HotelBookingRecord(
                        tripId = tripId,
                        uid = uid,
                        hotelId = hotelId,
                        guests = guests,
                        totalPrice = totalPrice,
                        guestName = guestName,
                        guestEmail = guestEmail,
                        guestPhone = guestPhone,
                        nights = nights,
                        checkInDate = checkInDate,
                        checkOutDate = checkOutDate,
                        roomType = roomType,
                        razorpayOrderId = result.orderId,
                        razorpayPaymentId = result.paymentId
                    )
                ).getOrThrow()
            }
            .onSuccess { _uiState.value = HotelPaymentUiState.Success }
            .onFailure {
                _uiState.value = HotelPaymentUiState.Failed(
                    "Payment succeeded but saving the booking failed. Contact support with payment ID ${result.paymentId}."
                )
            }
    }

    companion object {
        // Placeholder rate — swap for a real tax figure if your backend ever returns one.
        private const val TAX_RATE = 0.18
    }
}