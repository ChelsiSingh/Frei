package com.frei.app.presentation.booking.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.model.Expense
import com.frei.app.data.model.ExpenseCategory
import com.frei.app.data.model.ExpenseSource
import com.frei.app.data.model.flight.Flight
import com.frei.app.data.repository.BookingRepository
import com.frei.app.data.repository.ExpenseRepository
import com.frei.app.data.repository.FlightBookingRecord
import com.frei.app.data.repository.FlightRepository
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

sealed interface PaymentUiState {
    data object Loading : PaymentUiState
    data class Ready(
        val flight: Flight,
        val baseFare: Double,
        val taxesAndFees: Double,
        val seatPrice: Double,
        val totalPrice: Double
    ) : PaymentUiState
    data object Processing : PaymentUiState
    data object Success : PaymentUiState
    data class Failed(val message: String) : PaymentUiState
}

@HiltViewModel
class FlightConfirmPayViewModel @Inject constructor(
    private val flightRepository: FlightRepository,
    private val paymentRepository: PaymentRepository,
    private val bookingRepository: BookingRepository,
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth,
    val paymentManager: RazorpayPaymentManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val flightId: String = savedStateHandle["flightId"] ?: ""
    val travelers: Int = (savedStateHandle["travelers"] as? String)?.toIntOrNull() ?: 1
    val guestPhone: String = savedStateHandle["phone"] ?: ""
    val tripIdArg: String? = savedStateHandle["tripId"]
    val seatNumber: String = savedStateHandle["seat"] ?: ""
    val seatClass: String = savedStateHandle["seatClass"] ?: ""
    val seatPrice: Double = (savedStateHandle["seatPrice"] as? String)?.toDoubleOrNull() ?: 0.0

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Loading)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private var lastReadyState: PaymentUiState.Ready? = null

    init {
        loadFlight()
        viewModelScope.launch {
            paymentManager.results.collect { result ->
                when (result) {
                    is RazorpayResult.Success -> verify(result)
                    is RazorpayResult.Failure -> _uiState.value =
                        PaymentUiState.Failed(result.description)
                }
            }
        }
    }

    private fun String.urlDecoded(): String =
        try { java.net.URLDecoder.decode(this, "UTF-8") } catch (e: Exception) { this }

    val guestName: String = (savedStateHandle["name"] ?: "").urlDecoded()
    val guestEmail: String = (savedStateHandle["email"] ?: "").urlDecoded()

    private fun loadFlight() {
        viewModelScope.launch {
            flightRepository.getFlight(flightId)
                .onSuccess { flight ->
                    val baseFare = flight.price * travelers
                    val taxesAndFees = TAX_RATE
                    val total = baseFare + taxesAndFees + seatPrice
                    val ready = PaymentUiState.Ready(flight, baseFare, taxesAndFees, seatPrice, total)
                    lastReadyState = ready
                    _uiState.value = ready
                }
                .onFailure {
                    _uiState.value = PaymentUiState.Failed("Couldn't load booking details.")
                }
        }
    }

    companion object {
        private const val TAX_RATE = 0.2
    }

    fun createOrder(onOrderReady: (orderId: String, keyId: String, amountPaise: Int) -> Unit) {
        val state = _uiState.value as? PaymentUiState.Ready ?: return
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Processing
            paymentRepository.createOrder(state.totalPrice, receipt = "flight_$flightId")
                .onSuccess { order -> onOrderReady(order.orderId, order.keyId, order.amount) }
                .onFailure {
                    _uiState.value = PaymentUiState.Failed(it.message ?: "Could not start payment.")
                }
        }
    }

    private fun verify(result: RazorpayResult.Success) {
        viewModelScope.launch {
            paymentRepository.verifyPayment(result.orderId, result.paymentId, result.signature)
                .onSuccess { verified ->
                    if (!verified) {
                        _uiState.value =
                            PaymentUiState.Failed("Payment couldn't be verified. If money was deducted, it will be refunded.")
                        return@onSuccess
                    }
                    saveBooking(result)
                }
                .onFailure {
                    _uiState.value = PaymentUiState.Failed(it.message ?: "Verification failed.")
                }
        }
    }

    private suspend fun saveBooking(result: RazorpayResult.Success) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = PaymentUiState.Failed(
                "Payment succeeded but you're signed out, so the booking couldn't be saved. " +
                        "Contact support with payment ID ${result.paymentId}."
            )
            return
        }
        val readyState = lastReadyState
        val totalPrice = readyState?.totalPrice ?: 0.0
        val flight = readyState?.flight

        var savedTripId: String? = null

        val tripIdResult: Result<String> =
            tripIdArg?.let { Result.success(it) } ?: bookingRepository.getOrCreateTripId(uid)

        tripIdResult
            .mapCatching { tripId ->
                savedTripId = tripId
                bookingRepository.saveFlightBooking(
                    FlightBookingRecord(
                        tripId = tripId,
                        uid = uid,
                        flightId = flightId,
                        travelers = travelers,
                        totalPrice = totalPrice,
                        currency = flight?.currency ?: "",
                        guestName = guestName,
                        guestEmail = guestEmail,
                        guestPhone = guestPhone,
                        seatNumber = seatNumber,
                        seatClass = seatClass,
                        airline = flight?.airline ?: "",
                        airlineCode = flight?.airlineCode ?: "",
                        flightNumber = flight?.flightNumber ?: "",
                        fromAirport = flight?.fromAirport ?: "",
                        toAirport = flight?.toAirport ?: "",
                        departureTime = flight?.departureTime ?: "",
                        arrivalTime = flight?.arrivalTime ?: "",
                        razorpayOrderId = result.orderId,
                        razorpayPaymentId = result.paymentId
                    )
                ).getOrThrow()
            }
            .onSuccess {
                _uiState.value = PaymentUiState.Success
                recordExpense(uid, savedTripId, totalPrice, flight)
            }
            .onFailure {
                _uiState.value = PaymentUiState.Failed(
                    "Payment succeeded but saving the booking failed. Contact support with payment ID ${result.paymentId}."
                )
            }
    }

    private suspend fun recordExpense(uid: String, tripId: String?, amount: Double, flight: Flight?) {
        val tripName = tripId?.let { runCatching { expenseRepository.getTripName(it) }.getOrNull() }
        val title = if (flight != null && flight.fromAirport.isNotBlank() && flight.toAirport.isNotBlank()) {
            "Flight ${flight.fromAirport} \u2192 ${flight.toAirport}"
        } else {
            "Flight booking"
        }
        runCatching {
            expenseRepository.addExpense(
                Expense(
                    userId = uid,
                    tripId = tripId,
                    tripName = tripName,
                    title = title,
                    category = ExpenseCategory.FLIGHT,
                    amount = amount,
                    source = ExpenseSource.AUTO
                )
            )
        }
    }
}