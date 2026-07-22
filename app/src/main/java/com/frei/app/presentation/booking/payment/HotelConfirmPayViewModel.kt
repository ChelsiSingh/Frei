package com.frei.app.presentation.booking.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.model.Expense
import com.frei.app.data.model.ExpenseCategory
import com.frei.app.data.model.ExpenseSource
import com.frei.app.data.model.hotel.Hotel
import com.frei.app.data.repository.BookingRepository
import com.frei.app.data.repository.ExpenseRepository
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
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth,
    val paymentManager: RazorpayPaymentManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val hotelId: String = savedStateHandle["hotelId"] ?: ""
    val guests: Int = (savedStateHandle["guests"] as? String)?.toIntOrNull() ?: 1
    val guestPhone: String = savedStateHandle["phone"] ?: ""
    val tripIdArg: String? = savedStateHandle["tripId"]

    val nights: Int = (savedStateHandle["nights"] as? String)?.toIntOrNull() ?: 1

    private val _uiState = MutableStateFlow<HotelPaymentUiState>(HotelPaymentUiState.Loading)
    val uiState: StateFlow<HotelPaymentUiState> = _uiState.asStateFlow()

    private var lastReadyState: HotelPaymentUiState.Ready? = null

    init {
        viewModelScope.launch {
            hotelRepository.getHotel(hotelId)
                .onSuccess { hotel ->
                    val roomCost = hotel.pricePerNight.toDouble() * nights
                    val taxesAndCharges = roomCost * TAX_RATE
                    val ready = HotelPaymentUiState.Ready(
                        hotel = hotel,
                        roomCost = roomCost,
                        taxesAndCharges = taxesAndCharges,
                        totalPrice = roomCost + taxesAndCharges
                    )
                    lastReadyState = ready
                    _uiState.value = ready
                }
                .onFailure { _uiState.value = HotelPaymentUiState.Failed("Couldn't load booking details.") }
        }
        viewModelScope.launch {
            paymentManager.results.collect { result ->
                when (result) {
                    is RazorpayResult.Success -> verify(result)
                    is RazorpayResult.Failure -> {
                        if (result.code == com.razorpay.Checkout.PAYMENT_CANCELED) {
                            lastReadyState?.let { _uiState.value = it }
                        } else {
                            _uiState.value = HotelPaymentUiState.Failed(result.description)
                        }
                    }
                }
            }
        }
    }

    fun retry() {
        val ready = lastReadyState
        if (ready != null) {
            _uiState.value = ready
        } else {
            viewModelScope.launch {
                hotelRepository.getHotel(hotelId)
                    .onSuccess { hotel ->
                        val roomCost = hotel.pricePerNight.toDouble() * nights
                        val taxesAndCharges = roomCost * TAX_RATE
                        val ready = HotelPaymentUiState.Ready(hotel, roomCost, taxesAndCharges, roomCost + taxesAndCharges)
                        lastReadyState = ready
                        _uiState.value = ready
                    }
                    .onFailure { _uiState.value = HotelPaymentUiState.Failed("Couldn't load booking details.") }
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
        val readyState = lastReadyState
        val totalPrice = readyState?.totalPrice ?: 0.0
        val hotel = readyState?.hotel

        var savedTripId: String? = null

        val tripIdResult: Result<String> =
            tripIdArg?.let { Result.success(it) } ?: bookingRepository.getOrCreateTripId(uid)

        tripIdResult
            .mapCatching { tripId ->
                savedTripId = tripId
                bookingRepository.saveHotelBooking(
                    HotelBookingRecord(
                        tripId = tripId,
                        uid = uid,
                        hotelId = hotelId,
                        guests = guests,
                        totalPrice = totalPrice,
                        currency = hotel?.currency ?: "",
                        guestName = guestName,
                        guestEmail = guestEmail,
                        guestPhone = guestPhone,
                        nights = nights,
                        checkInDate = checkInDate,
                        checkOutDate = checkOutDate,
                        roomType = roomType,
                        hotelName = hotel?.name ?: "",
                        cityId = hotel?.cityId ?: "",
                        address = hotel?.address ?: "",
                        image = hotel?.image,
                        razorpayOrderId = result.orderId,
                        razorpayPaymentId = result.paymentId
                    )
                ).getOrThrow()
            }
            .onSuccess {
                _uiState.value = HotelPaymentUiState.Success
                recordExpense(uid, savedTripId, totalPrice, hotel)
            }
            .onFailure {
                _uiState.value = HotelPaymentUiState.Failed(
                    "Payment succeeded but saving the booking failed. Contact support with payment ID ${result.paymentId}."
                )
            }
    }

    private suspend fun recordExpense(uid: String, tripId: String?, amount: Double, hotel: Hotel?) {
        val tripName = tripId?.let { runCatching { expenseRepository.getTripName(it) }.getOrNull() }
        val title = hotel?.name?.takeIf { it.isNotBlank() } ?: "Hotel booking"
        runCatching {
            expenseRepository.addExpense(
                Expense(
                    userId = uid,
                    tripId = tripId,
                    tripName = tripName,
                    title = title,
                    category = ExpenseCategory.HOTEL,
                    amount = amount,
                    source = ExpenseSource.AUTO
                )
            )
        }
    }

    companion object {
        // Placeholder rate — swap for a real tax figure if your backend ever returns one.
        private const val TAX_RATE = 0.2
    }
}