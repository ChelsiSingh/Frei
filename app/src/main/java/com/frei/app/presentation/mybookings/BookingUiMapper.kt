package com.frei.app.presentation.mybookings

import com.frei.app.data.repository.FlightBookingRecord
import com.frei.app.data.repository.HotelBookingRecord
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

// Shared formatting helpers

private fun parseIsoInstant(raw: String): Instant? = runCatching { Instant.parse(raw) }.getOrNull()

private fun formatDate(raw: String): String =
    parseIsoInstant(raw)?.let {
        DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH).withZone(ZoneId.systemDefault()).format(it)
    } ?: "—"

private fun formatTime(raw: String): String =
    parseIsoInstant(raw)?.let {
        DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault()).format(it)
    } ?: "—"

private fun formatDateTime(raw: String): String {
    val date = formatDate(raw)
    val time = formatTime(raw)
    return if (date == "—" || time == "—") "—" else "$date, $time"
}

private fun estimateBoardingTime(departureRaw: String): String =
    parseIsoInstant(departureRaw)?.let {
        DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
            .format(it.minusSeconds(45 * 60))
    } ?: "—"

private fun formatFlightDuration(departureRaw: String, arrivalRaw: String): String {
    val dep = parseIsoInstant(departureRaw) ?: return "—"
    val arr = parseIsoInstant(arrivalRaw) ?: return "—"
    val mins = Duration.between(dep, arr).toMinutes().coerceAtLeast(0)
    return "${mins / 60}h ${mins % 60}m"
}

private fun formatMoney(amount: Double, currencyCode: String): String = runCatching {
    NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = Currency.getInstance(currencyCode.ifBlank { "INR" })
    }.format(amount)
}.getOrElse { "₹%.2f".format(amount) }

private fun shortRef(paymentId: String, fallback: String): String =
    paymentId.ifBlank { fallback }.takeLast(6).uppercase().ifBlank { "N/A" }

fun generateInvoiceNumber(prefix: String, uniqueSeed: String): String =
    "FRI-$prefix-${uniqueSeed.ifBlank { "000000" }.takeLast(6).uppercase()}"

// Flight

fun FlightBookingRecord.toBoardingPassUiState(): BoardingPassUiState = BoardingPassUiState(
    airline = airline.ifBlank { "—" },
    flightNumber = flightNumber.ifBlank { "—" },
    travelClass = seatClass.ifBlank { "Economy" },
    fromCode = fromAirport.ifBlank { "—" },
    fromCity = fromAirport.ifBlank { "—" }, // TODO: denormalize real city name at booking time
    toCode = toAirport.ifBlank { "—" },
    toCity = toAirport.ifBlank { "—" },     // TODO: denormalize real city name at booking time
    durationLabel = formatFlightDuration(departureTime, arrivalTime),
    passengerName = guestName.ifBlank { "—" },
    date = formatDate(departureTime),
    boardingTime = estimateBoardingTime(departureTime), // estimate, not authoritative
    departureTime = formatTime(departureTime),
    gate = "—",             // TODO: not known until check-in
    terminal = "—",         // TODO: not known until check-in
    seat = seatNumber.ifBlank { "Not selected" },
    baggageAllowance = "—", // TODO: depends on fare class; not currently stored
    boardingGroup = "—",    // TODO: not known until check-in
    pnr = shortRef(razorpayPaymentId, flightId)
)

fun FlightBookingRecord.toFlightInvoiceUiState(): FlightInvoiceUiState = FlightInvoiceUiState(
    invoiceNo = generateInvoiceNumber("FL", razorpayPaymentId.ifBlank { flightId }),
    issuedOn = formatDate(Instant.ofEpochMilli(bookedAt).toString()),
    billedTo = guestName.ifBlank { guestEmail },
    bookingId = flightId,
    fromCode = fromAirport.ifBlank { "—" },
    fromCity = fromAirport.ifBlank { "—" },
    toCode = toAirport.ifBlank { "—" },
    toCity = toAirport.ifBlank { "—" },
    airline = airline.ifBlank { "—" },
    flightNumber = flightNumber.ifBlank { "—" },
    departureDateTime = formatDateTime(departureTime),
    baseFare = formatMoney(totalPrice, currency),
    taxesAndFees = formatMoney(0.0, currency),
    seatFee = formatMoney(0.0, currency),
    convenienceFee = formatMoney(0.0, currency),
    discount = null,
    total = formatMoney(totalPrice, currency),
    paymentMethod = "Razorpay",
    transactionId = razorpayPaymentId.ifBlank { "—" }
)

// Hotel
fun HotelBookingRecord.toHotelInvoiceUiState(): HotelInvoiceUiState = HotelInvoiceUiState(
    invoiceNo = generateInvoiceNumber("HT", razorpayPaymentId.ifBlank { hotelId }),
    issuedOn = formatDate(Instant.ofEpochMilli(bookedAt).toString()),
    billedTo = guestName.ifBlank { guestEmail },
    bookingId = hotelId,
    hotelName = hotelName.ifBlank { "—" },
    roomType = roomType.ifBlank { "—" },
    guestCount = guests,
    checkIn = checkInDate.ifBlank { "—" },
    checkOut = checkOutDate.ifBlank { "—" },
    nights = nights,
    roomChargePerNight = formatMoney(if (nights > 0) totalPrice / nights else totalPrice, currency),
    roomChargesTotal = formatMoney(totalPrice, currency),
    taxes = formatMoney(0.0, currency),
    convenienceFee = formatMoney(0.0, currency),
    discount = null,
    total = formatMoney(totalPrice, currency),
    paymentMethod = "Razorpay",
    transactionId = razorpayPaymentId.ifBlank { "—" }
)

fun HotelBookingRecord.toHotelBookingDetailsUiState(): HotelBookingDetailsUiState = HotelBookingDetailsUiState(
    hotelName = hotelName.ifBlank { "—" },
    address = address.ifBlank { "—" },
    heroImageUrl = image.orEmpty(),
    rating = 0f,             // TODO: pull from hotel listing API if needed
    reviewCount = 0,         // TODO: pull from hotel listing API if needed
    statusLabel = "Confirmed",
    checkInDate = checkInDate.ifBlank { "—" },
    checkInTime = "2:00 PM",  // TODO: property-specific; not stored on the booking
    checkOutDate = checkOutDate.ifBlank { "—" },
    checkOutTime = "11:00 AM",
    roomType = roomType.ifBlank { "—" },
    roomImageUrl = image.orEmpty(),
    guestCount = guests,
    bedType = "—",
    nights = nights,
    amenities = defaultHotelAmenities(),
    contactName = "Front Desk",
    contactRole = "Property Contact",
    roomCharges = formatMoney(totalPrice, currency),
    taxesAndFees = formatMoney(0.0, currency),
    discount = null,
    total = formatMoney(totalPrice, currency),
    cancellationNote = "Check property policy"
)