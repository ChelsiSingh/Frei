package com.frei.app.presentation.mybookings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.frei.app.data.repository.FlightBookingRecord
import com.frei.app.data.repository.HotelBookingRecord
import com.frei.app.presentation.booking.flight.FreiInk
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val FreiPurple = Color(0xFF6C3FCF)
private val FreiBg = Color(0xFFFAFAFC)
private val FreiCardBorder = Color(0xFFF0EEF6)
private val FreiMuted = Color(0xFF8C89A3)
private val FreiChipBorder = Color(0xFFE4E1EE)
private val FreiSubtleBg = Color(0xFFF7F6FB)

enum class BookingStatus { UPCOMING, COMPLETED, CANCELLED }

private enum class BookingFilter(val label: String) {
    ALL("All"), UPCOMING("Upcoming"), COMPLETED("Completed"), CANCELLED("Cancelled")
}

private enum class BookingsTab { FLIGHTS, HOTELS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    onBackClick: () -> Unit,
    onBoardingPassClick: (FlightBookingRecord) -> Unit,
    onFlightInvoiceClick: (FlightBookingRecord) -> Unit,
    onViewHotelBookingClick: (HotelBookingRecord) -> Unit,
    onHotelInvoiceClick: (HotelBookingRecord) -> Unit,
    onSearchFlightsClick: () -> Unit = {},
    onSearchHotelsClick: () -> Unit = {},
    viewModel: BookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf(BookingFilter.ALL) }

    val totalConfirmed = uiState.flightBookings.size + uiState.hotelBookings.size

    Scaffold(
        containerColor = FreiBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Bookings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "$totalConfirmed confirmed ${if (totalConfirmed == 1) "trip" else "trips"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FreiMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(38.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, FreiCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreiInk)
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, FreiCardBorder, RoundedCornerShape(12.dp)),
                        onClick = { /* search not wired yet */ }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search bookings", tint = FreiInk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SegmentedTabRow(
                selectedTab = if (pagerState.currentPage == 0) BookingsTab.FLIGHTS else BookingsTab.HOTELS,
                onTabSelected = { tab ->
                    scope.launch {
                        pagerState.animateScrollToPage(if (tab == BookingsTab.FLIGHTS) 0 else 1)
                    }
                }
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                repeat(2) { index ->
                    val active = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.5.dp)
                            .height(4.dp)
                            .width(if (active) 16.dp else 4.dp)
                            .background(if (active) FreiPurple else Color(0xFFD9D5E8), CircleShape)
                    )
                }
            }

            FilterChipsRow(
                selected = selectedFilter,
                onSelected = { selectedFilter = it },
                modifier = Modifier.padding(top = 10.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
                                color = FreiPurple
                            )
                        }
                        uiState.errorMessage != null -> {
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                color = FreiMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center).padding(32.dp)
                            )
                        }
                        page == 0 -> {
                            FlightsTabContent(
                                bookings = uiState.flightBookings,
                                filter = selectedFilter,
                                onBoardingPassClick = onBoardingPassClick,
                                onInvoiceClick = onFlightInvoiceClick,
                                onSearchFlightsClick = onSearchFlightsClick
                            )
                        }
                        else -> {
                            HotelsTabContent(
                                bookings = uiState.hotelBookings,
                                filter = selectedFilter,
                                onViewBookingClick = onViewHotelBookingClick,
                                onInvoiceClick = onHotelInvoiceClick,
                                onSearchHotelsClick = onSearchHotelsClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedTabRow(
    selectedTab: BookingsTab,
    onTabSelected: (BookingsTab) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 18.dp)
            .padding(top = 10.dp)
            .fillMaxWidth()
            .background(Color(0xFFF0EEF6), RoundedCornerShape(13.dp))
            .padding(4.dp)
    ) {
        SegmentedTabItem(
            label = "Flights",
            selected = selectedTab == BookingsTab.FLIGHTS,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(BookingsTab.FLIGHTS) }
        )
        SegmentedTabItem(
            label = "Hotels",
            selected = selectedTab == BookingsTab.HOTELS,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(BookingsTab.HOTELS) }
        )
    }
}

@Composable
private fun SegmentedTabItem(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .background(if (selected) Color.White else Color.Transparent, RoundedCornerShape(9.dp))
            .clickableNoRipple(onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) FreiInk else FreiMuted
        )
    }
}

@Composable
private fun FilterChipsRow(
    selected: BookingFilter,
    onSelected: (BookingFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        items(BookingFilter.entries) { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) FreiInk else Color.White,
                        RoundedCornerShape(99.dp)
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = FreiChipBorder,
                        shape = RoundedCornerShape(99.dp)
                    )
                    .clickableNoRipple { onSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color(0xFF6E6B82)
                )
            }
        }
    }
}

// ---------- FLIGHTS TAB ----------

@Composable
private fun FlightsTabContent(
    bookings: List<FlightBookingRecord>,
    filter: BookingFilter,
    onBoardingPassClick: (FlightBookingRecord) -> Unit,
    onInvoiceClick: (FlightBookingRecord) -> Unit,
    onSearchFlightsClick: () -> Unit
) {
    val filtered = remember(bookings, filter) {
        bookings.filter { matchesFilter(flightStatus(it), filter) }
            .sortedByDescending { it.bookedAt }
    }

    if (filtered.isEmpty()) {
        EmptyBookingsState(
            title = if (bookings.isEmpty()) "No flight bookings yet" else "No ${filter.label.lowercase()} flights",
            subtitle = "Your confirmed flights will show up here right after checkout — ready with boarding pass access.",
            ctaLabel = "Search Flights",
            onCtaClick = onSearchFlightsClick
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filtered, key = { it.razorpayPaymentId.ifBlank { it.hashCode().toString() } }) { record ->
            FlightBookingCard(
                record = record,
                status = flightStatus(record),
                onBoardingPassClick = { onBoardingPassClick(record) },
                onInvoiceClick = { onInvoiceClick(record) }
            )
        }
    }
}

@Composable
private fun FlightBookingCard(
    record: FlightBookingRecord,
    status: BookingStatus,
    onBoardingPassClick: () -> Unit,
    onInvoiceClick: () -> Unit
) {
    val departure = parseIsoDateTime(record.departureTime)
    val arrival = parseIsoDateTime(record.arrivalTime)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, FreiCardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFE7EFFB), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = record.airlineCode.ifBlank { "??" },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2A4F9E)
                )
            }
            Spacer(modifier = Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.airline.ifBlank { "Flight" }} · ${record.seatClass.ifBlank { "Economy" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = FreiInk
                )
                Text(
                    text = "${record.flightNumber} · ${departure?.let { formatDateShort(it.toLocalDate()) } ?: "--"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FreiMuted
                )
            }
            StatusBadge(status)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 13.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = departure?.let { formatTime(it) } ?: "--:--",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = FreiInk
                )
                Text(record.fromAirport, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = FreiMuted)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
            ) {
                Text(
                    text = flightDurationLabel(departure, arrival),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFB6B3C6)
                )
                HorizontalDivider(modifier = Modifier.padding(top = 3.dp), color = Color(0xFFE4E1EE))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = arrival?.let { formatTime(it) } ?: "--:--",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = FreiInk
                )
                Text(record.toAirport, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = FreiMuted)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = FreiCardBorder)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "BOOKING ID · SEAT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9A97AE)
                )
                Text(
                    text = "${pseudoBookingRef(record.razorpayOrderId)} · ${record.seatNumber.ifBlank { "--" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = FreiInk
                )
            }
            if (status == BookingStatus.UPCOMING) {
                PrimaryPillButton(
                    label = "Boarding Pass",
                    onClick = onBoardingPassClick
                )
            } else {
                TextLinkButton(label = "Invoice", onClick = onInvoiceClick)
            }
        }
    }
}

// ---------- HOTELS TAB ----------

@Composable
private fun HotelsTabContent(
    bookings: List<HotelBookingRecord>,
    filter: BookingFilter,
    onViewBookingClick: (HotelBookingRecord) -> Unit,
    onInvoiceClick: (HotelBookingRecord) -> Unit,
    onSearchHotelsClick: () -> Unit
) {
    val filtered = remember(bookings, filter) {
        bookings.filter { matchesFilter(hotelStatus(it), filter) }
            .sortedByDescending { it.bookedAt }
    }

    if (filtered.isEmpty()) {
        EmptyBookingsState(
            title = if (bookings.isEmpty()) "No hotel bookings yet" else "No ${filter.label.lowercase()} hotels",
            subtitle = "Your confirmed stays will show up here right after checkout — ready with full booking details.",
            ctaLabel = "Search Hotels",
            onCtaClick = onSearchHotelsClick
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filtered, key = { it.razorpayPaymentId.ifBlank { it.hashCode().toString() } }) { record ->
            HotelBookingCard(
                record = record,
                status = hotelStatus(record),
                onViewBookingClick = { onViewBookingClick(record) },
                onInvoiceClick = { onInvoiceClick(record) }
            )
        }
    }
}

@Composable
private fun HotelBookingCard(
    record: HotelBookingRecord,
    status: BookingStatus,
    onViewBookingClick: () -> Unit,
    onInvoiceClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, FreiCardBorder, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.linearGradient(listOf(FreiPurple, Color(0xFF5A2EB8))),
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\uD83C\uDFE8", style = MaterialTheme.typography.titleLarge) // 🏨
            }
            Spacer(modifier = Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = record.hotelName.ifBlank { "Hotel" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = FreiInk,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(status)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = FreiMuted, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = record.address.ifBlank { "Location unavailable" },
                        style = MaterialTheme.typography.labelSmall,
                        color = FreiMuted
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateBox(label = "CHECK-IN", value = record.checkInDate, modifier = Modifier.weight(1f))
            DateBox(label = "CHECK-OUT", value = record.checkOutDate, modifier = Modifier.weight(1f))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 11.dp), color = FreiCardBorder)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "${record.guests} GUEST${if (record.guests == 1) "" else "S"} · ${record.nights} NIGHT${if (record.nights == 1) "" else "S"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9A97AE)
                )
                Text(
                    text = "₹${"%,.0f".format(record.totalPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = FreiPurple
                )
            }
            if (status == BookingStatus.UPCOMING) {
                PrimaryPillButton(label = "View Booking", onClick = onViewBookingClick)
            } else {
                TextLinkButton(label = "Invoice", onClick = onInvoiceClick)
            }
        }
    }
}

@Composable
private fun DateBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(FreiSubtleBg, RoundedCornerShape(11.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9A97AE))
        Text(
            text = formatFlexibleDateDisplay(value),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = FreiInk
        )
    }
}

// ---------- SHARED PIECES ----------

@Composable
private fun StatusBadge(status: BookingStatus) {
    val (bg, fg, label) = when (status) {
        BookingStatus.UPCOMING -> Triple(Color(0xFFF5F2FC), FreiPurple, "UPCOMING")
        BookingStatus.COMPLETED -> Triple(Color(0xFFF0EEF6), Color(0xFF6E6B82), "COMPLETED")
        BookingStatus.CANCELLED -> Triple(Color(0xFFFBEAEA), Color(0xFFC0392B), "CANCELLED")
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(99.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = fg)
    }
}

@Composable
private fun PrimaryPillButton(label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(FreiPurple, RoundedCornerShape(11.dp))
            .clickableNoRipple(onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun TextLinkButton(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = FreiPurple,
        modifier = Modifier.clickableNoRipple(onClick)
    )
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))

@Composable
private fun EmptyBookingsState(
    title: String,
    subtitle: String,
    ctaLabel: String,
    onCtaClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Color(0xFFF5F2FC), RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\u2708\uFE0F", style = MaterialTheme.typography.headlineMedium) // ✈️
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = FreiInk,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = FreiMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier
                .background(
                    Brush.linearGradient(listOf(Color(0xFF7C4DDB), Color(0xFF5A2EB8))),
                    RoundedCornerShape(13.dp)
                )
                .clickableNoRipple(onCtaClick)
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            Text(ctaLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
    }
}

// ---------- STATUS / FORMATTING HELPERS ----------

private fun matchesFilter(status: BookingStatus, filter: BookingFilter): Boolean = when (filter) {
    BookingFilter.ALL -> true
    BookingFilter.UPCOMING -> status == BookingStatus.UPCOMING
    BookingFilter.COMPLETED -> status == BookingStatus.COMPLETED
    BookingFilter.CANCELLED -> status == BookingStatus.CANCELLED
}

private fun flightStatus(record: FlightBookingRecord): BookingStatus {
    val departure = parseIsoDateTime(record.departureTime) ?: return BookingStatus.UPCOMING
    return if (departure.isAfter(LocalDateTime.now())) BookingStatus.UPCOMING else BookingStatus.COMPLETED
}

private fun hotelStatus(record: HotelBookingRecord): BookingStatus {
    val checkOut = parseFlexibleDate(record.checkOutDate) ?: return BookingStatus.UPCOMING
    return if (checkOut.isAfter(LocalDate.now())) BookingStatus.UPCOMING else BookingStatus.COMPLETED
}

private fun parseIsoDateTime(raw: String): LocalDateTime? {
    if (raw.isBlank()) return null
    return try {
        OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    } catch (e: DateTimeParseException) {
        try {
            Instant.parse(raw).atZone(ZoneId.systemDefault()).toLocalDateTime()
        } catch (e2: DateTimeParseException) {
            null
        }
    }
}

private fun parseFlexibleDate(raw: String): LocalDate? {
    if (raw.isBlank()) return null
    return try {
        LocalDate.parse(raw)
    } catch (e: DateTimeParseException) {
        parseIsoDateTime(raw)?.toLocalDate()
    }
}

private val dateShortFormatter = DateTimeFormatter.ofPattern("d MMM ''yy")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun formatDateShort(date: LocalDate): String = date.format(dateShortFormatter)

private fun formatTime(dateTime: LocalDateTime): String = dateTime.format(timeFormatter)

private fun formatFlexibleDateDisplay(raw: String): String =
    parseFlexibleDate(raw)?.let { formatDateShort(it) } ?: raw.ifBlank { "--" }

private fun flightDurationLabel(departure: LocalDateTime?, arrival: LocalDateTime?): String {
    if (departure == null || arrival == null) return "Non-stop"
    val minutes = Duration.between(departure, arrival).toMinutes().coerceAtLeast(0)
    val hours = minutes / 60
    val mins = minutes % 60
    return "${hours}h ${mins}m · Non-stop"
}

private fun pseudoBookingRef(orderId: String): String =
    orderId.filter { it.isLetterOrDigit() }.takeLast(6).uppercase().ifBlank { "--" }