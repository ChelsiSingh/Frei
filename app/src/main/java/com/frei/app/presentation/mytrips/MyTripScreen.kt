package com.frei.app.presentation.mytrips

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frei.app.data.model.Expense
import com.frei.app.data.model.ExpenseSource
import com.frei.app.data.model.Trip
import com.frei.app.data.repository.BookingRepositoryImpl
import com.frei.app.data.repository.FirestoreExpenseRepository
import com.frei.app.data.repository.FlightBookingRecord
import com.frei.app.data.repository.HotelBookingRecord
import com.frei.app.data.repository.PackingRepository
import com.frei.app.presentation.booking.flight.FreiInk
import com.frei.app.presentation.packing.CategoryCard
import com.frei.app.presentation.packing.InputDialog
import com.frei.app.presentation.packing.PackingCategory
import com.frei.app.presentation.packing.PackingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

private val FreiPurple = Color(0xFF6C3CF0)
private val FreiLightBg = Color(0xFFF7F6FB)

// ---------- Status + formatting helpers ----------

private fun deriveStatus(travelInstant: Instant?): BookingStatus {
    if (travelInstant == null) return BookingStatus.CONFIRMED
    val now = Instant.now()
    return when {
        travelInstant.isBefore(now) -> BookingStatus.COMPLETED
        travelInstant.isBefore(now.plus(7, ChronoUnit.DAYS)) -> BookingStatus.UPCOMING
        else -> BookingStatus.CONFIRMED
    }
}

private fun statusColor(status: BookingStatus): Color = when (status) {
    BookingStatus.CONFIRMED -> Color(0xFF6C3CF0)
    BookingStatus.UPCOMING -> Color(0xFF14B8A6)
    BookingStatus.COMPLETED -> Color(0xFF8C89A3)
}

@Composable
private fun StatusBadge(status: BookingStatus) {
    Box(
        modifier = Modifier
            .background(statusColor(status).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            color = statusColor(status),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatIso(iso: String): String = runCatching {
    val instant = Instant.parse(iso)
    DateTimeFormatter.ofPattern("dd MMM, hh:mm a").withZone(ZoneId.systemDefault()).format(instant)
}.getOrDefault(iso)

private fun hotelDateInstant(dateStr: String): Instant? = runCatching {
    LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toInstant()
}.getOrNull()

// Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripScreen(
    tripId: String,
    onBackClick: () -> Unit,
    onNavigateHome: () -> Unit,
    onBookForTrip: (String) -> Unit
) {

    val context = LocalContext.current

    var tripDetails by remember { mutableStateOf<Trip?>(null) }
    var isLoadingTrip by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(TripTab.Trip) }

    var packingCategories by remember { mutableStateOf<List<PackingCategory>>(emptyList()) }
    var isLoadingPacking by remember { mutableStateOf(true) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var activeCategoryForNewItem by remember { mutableStateOf<PackingCategory?>(null) }

    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    val bookingRepository = remember { BookingRepositoryImpl(FirebaseFirestore.getInstance()) }
    val expenseRepository = remember { FirestoreExpenseRepository(FirebaseFirestore.getInstance()) }

    var selectedBookingMode by remember { mutableStateOf(BookingMode.FLIGHT) }
    var flightBookings by remember { mutableStateOf<List<FlightBookingRecord>>(emptyList()) }
    var hotelBookings by remember { mutableStateOf<List<HotelBookingRecord>>(emptyList()) }
    var isLoadingBookings by remember { mutableStateOf(true) }

    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var isLoadingExpenses by remember { mutableStateOf(true) }

    var showAddExpenseDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Load Core Trip Details
    LaunchedEffect(tripId) {
        FirebaseFirestore.getInstance()
            .collection("trips")
            .document(tripId)
            .get()
            .addOnSuccessListener { document ->
                tripDetails = document.toObject(Trip::class.java)
                isLoadingTrip = false
            }
            .addOnFailureListener {
                isLoadingTrip = false
            }
    }

    // Load Packing Collection items reactively when selecting the Packing Tab
    LaunchedEffect(selectedTab, tripId) {
        if (selectedTab == TripTab.Packing && currentUid.isNotEmpty()) {
            isLoadingPacking = true
            PackingRepository.fetchPackingListFromFirestore(
                userId = currentUid,
                tripId = tripId,
                onSuccess = { fetchedList ->
                    packingCategories = fetchedList
                    isLoadingPacking = false
                },
                onFailure = {
                    isLoadingPacking = false
                }
            )
        }
    }

    // Load Bookings when selecting the Bookings tab (or switching Flights/Hotels)
    LaunchedEffect(selectedTab, selectedBookingMode, tripId) {
        if (selectedTab == TripTab.Bookings && currentUid.isNotEmpty()) {
            isLoadingBookings = true
            when (selectedBookingMode) {
                BookingMode.FLIGHT -> bookingRepository.getFlightBookings(currentUid, tripId)
                    .onSuccess {
                        flightBookings = it
                        isLoadingBookings = false}
                    .onFailure {
                        Toast.makeText(context, "Couldn't load flights: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                BookingMode.HOTEL -> bookingRepository.getHotelBookings(currentUid, tripId)
                    .onSuccess {
                        hotelBookings = it
                        isLoadingBookings = false}
                    .onFailure {
                        Toast.makeText(context, "Couldn't load hotels: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    // Load Expenses when selecting the Expenses tab
    LaunchedEffect(selectedTab, tripId) {
        if (selectedTab == TripTab.Expenses && currentUid.isNotEmpty()) {
            isLoadingExpenses = true
            expenseRepository.observeExpensesForTrip(currentUid, tripId).collect {
                expenses = it
                isLoadingExpenses = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            TripTab.Packing -> "Packing List"
                            TripTab.Bookings -> "Bookings"
                            TripTab.Expenses -> "Expenses"
                            TripTab.Trip -> tripDetails?.title ?: "Trip Details"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(38.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFECEAF3), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreiInk)
                    }
                },
                actions = {
                    if (selectedTab == TripTab.Packing && !isLoadingPacking) {
                        IconButton(
                            onClick = {
                                PackingRepository.savePackingListToFirestore(
                                    userId = currentUid,
                                    tripId = tripId,
                                    categories = packingCategories,
                                    onSuccess = { Toast.makeText(context, "Packing list saved", Toast.LENGTH_SHORT).show() },
                                    onFailure = { /* Handle error states */ }
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save packing list",
                                tint = FreiPurple
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            when {
                selectedTab == TripTab.Packing && !isLoadingPacking -> {
                    ExtendedFloatingActionButton(
                        onClick = { showAddCategoryDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                        text = { Text("Add Category", fontWeight = FontWeight.Bold) },
                        containerColor = FreiPurple,
                        contentColor = Color.White
                    )
                }
                selectedTab == TripTab.Bookings -> {
                    FloatingActionButton(
                        onClick = { onBookForTrip(tripId) },
                        containerColor = FreiPurple,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Book flight or hotel")
                    }
                }
                selectedTab == TripTab.Expenses -> {
                    FloatingActionButton(
                        onClick = { showAddExpenseDialog = true },
                        containerColor = FreiPurple,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add expense")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                TripTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab.ordinal == index,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoadingTrip) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FreiPurple
                    )
                } else {
                    tripDetails?.let { trip ->
                        when (selectedTab) {
                            TripTab.Trip -> {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = " Destination: ${trip.destination}",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    HorizontalDivider(color = Color(0xFFE8E7EF))

                                    Text(
                                        text = "Number of people: ${trip.travelers}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Budget Allocated: ₹${trip.budget.ifBlank { "0" }}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Transport: ${trip.transport}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Stay Type: ${trip.stay}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            TripTab.Packing -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(FreiLightBg)
                                ) {
                                    if (isLoadingPacking) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.Center),
                                            color = FreiPurple
                                        )
                                    } else if (packingCategories.isEmpty()) {
                                        Text(
                                            text = "No categories yet. Tap + to start packing!",
                                            color = Color.Gray,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            items(packingCategories, key = { it.id }) { category ->
                                                CategoryCard(
                                                    category = category,
                                                    onAddItemClick = { activeCategoryForNewItem = category },
                                                    onItemToggle = { itemId ->
                                                        packingCategories = packingCategories.map { cat ->
                                                            if (cat.id == category.id) {
                                                                cat.copy(items = cat.items.map { item ->
                                                                    if (item.id == itemId) item.copy(isPacked = !item.isPacked) else item
                                                                })
                                                            } else cat
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            TripTab.Bookings -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Flights / Hotels segmented switch
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .background(FreiLightBg, RoundedCornerShape(12.dp))
                                            .padding(4.dp)
                                    ) {
                                        listOf(
                                            BookingMode.FLIGHT to "Flights",
                                            BookingMode.HOTEL to "Hotels"
                                        ).forEach { (mode, label) ->
                                            val selected = selectedBookingMode == mode
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedBookingMode = mode }
                                                    .background(
                                                        if (selected) Color.White else Color.Transparent,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (selected) FreiPurple else Color(0xFF8C89A3),
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }

                                    if (isLoadingBookings) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 40.dp),
                                            color = FreiPurple
                                        )
                                    } else {
                                        when (selectedBookingMode) {
                                            BookingMode.FLIGHT -> {
                                                if (flightBookings.isEmpty()) {
                                                    Text(
                                                        text = "No flights booked for this trip yet.",
                                                        color = Color.Gray,
                                                        modifier = Modifier.padding(24.dp)
                                                    )
                                                } else {
                                                    LazyColumn(
                                                        contentPadding = PaddingValues(16.dp),
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        items(flightBookings) { booking ->
                                                            val status = deriveStatus(
                                                                runCatching { Instant.parse(booking.departureTime) }.getOrNull()
                                                            )
                                                            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                                                Column(
                                                                    modifier = Modifier.padding(16.dp),
                                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                                    ) {
                                                                        Text(
                                                                            text = "${booking.fromAirport} → ${booking.toAirport}",
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                        StatusBadge(status)
                                                                    }
                                                                    Text(
                                                                        text = "${booking.airline} ${booking.flightNumber}",
                                                                        color = Color(0xFF8C89A3),
                                                                        style = MaterialTheme.typography.bodySmall
                                                                    )
                                                                    Text(
                                                                        text = "Departs: ${formatIso(booking.departureTime)}",
                                                                        style = MaterialTheme.typography.bodySmall
                                                                    )
                                                                    Text(
                                                                        text = "Arrives: ${formatIso(booking.arrivalTime)}",
                                                                        style = MaterialTheme.typography.bodySmall
                                                                    )
                                                                    Text(
                                                                        text = "₹${booking.totalPrice}",
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = FreiPurple
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            BookingMode.HOTEL -> {
                                                if (hotelBookings.isEmpty()) {
                                                    Text(
                                                        text = "No hotels booked for this trip yet.",
                                                        color = Color.Gray,
                                                        modifier = Modifier.padding(24.dp)
                                                    )
                                                } else {
                                                    LazyColumn(
                                                        contentPadding = PaddingValues(16.dp),
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        items(hotelBookings) { booking ->
                                                            val status = deriveStatus(hotelDateInstant(booking.checkOutDate))
                                                            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                                                Column(
                                                                    modifier = Modifier.padding(16.dp),
                                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                                    ) {
                                                                        Text(text = booking.hotelName, fontWeight = FontWeight.Bold)
                                                                        StatusBadge(status)
                                                                    }
                                                                    Text(
                                                                        text = booking.address,
                                                                        color = Color(0xFF8C89A3),
                                                                        style = MaterialTheme.typography.bodySmall
                                                                    )
                                                                    Text(
                                                                        text = "Check-in: ${booking.checkInDate}",
                                                                        style = MaterialTheme.typography.bodySmall
                                                                    )
                                                                    Text(
                                                                        text = "Check-out: ${booking.checkOutDate}",
                                                                        style = MaterialTheme.typography.bodySmall
                                                                    )
                                                                    Text(
                                                                        text = "₹${booking.totalPrice}",
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = FreiPurple
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            TripTab.Expenses -> {
                                if (isLoadingExpenses) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center),
                                        color = FreiPurple
                                    )
                                } else if (expenses.isEmpty()) {
                                    Text(
                                        text = "No expenses logged for this trip yet.",
                                        color = Color.Gray,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(expenses, key = { it.id }) { expense ->
                                            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text(text = expense.title, fontWeight = FontWeight.Bold)
                                                        Text(
                                                            text = expense.category.label,
                                                            color = Color(0xFF8C89A3),
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                    Text(
                                                        text = "₹${expense.amount}",
                                                        fontWeight = FontWeight.Bold,
                                                        color = FreiPurple
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } ?: Text(
                        text = "Failed to load trip info.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                if (showAddCategoryDialog) {
                    InputDialog(
                        title = "New Category",
                        placeholder = "e.g., Clothes, Toiletries",
                        onDismiss = { showAddCategoryDialog = false },
                        onConfirm = { name ->
                            if (name.isNotBlank()) {
                                val generatedId = UUID.randomUUID().toString()
                                packingCategories = packingCategories + PackingCategory(id = generatedId, name = name)
                            }
                            showAddCategoryDialog = false
                        }
                    )
                }

                if (showAddExpenseDialog) {
                    AddExpenseDialog(
                        onDismiss = { showAddExpenseDialog = false },
                        onConfirm = { title, amount, category ->
                            val expense = Expense(
                                userId = currentUid,
                                tripId = tripId,
                                tripName = tripDetails?.title,
                                title = title,
                                category = category,
                                amount = amount,
                                source = ExpenseSource.MANUAL
                            )
                            // launch on a coroutine scope tied to the composable
                            coroutineScope.launch {
                                runCatching { expenseRepository.addExpense(expense) }
                                    .onFailure { Toast.makeText(context, "Couldn't save expense", Toast.LENGTH_SHORT).show() }
                            }
                            showAddExpenseDialog = false
                        }
                    )
                }

                activeCategoryForNewItem?.let { category ->
                    InputDialog(
                        title = "Add to ${category.name}",
                        placeholder = "e.g., T-shirts, Toothbrush",
                        onDismiss = { activeCategoryForNewItem = null },
                        onConfirm = { itemName ->
                            if (itemName.isNotBlank()) {
                                val generatedItemId = UUID.randomUUID().toString()
                                packingCategories = packingCategories.map { cat ->
                                    if (cat.id == category.id) {
                                        cat.copy(items = cat.items + PackingItem(id = generatedItemId, name = itemName))
                                    } else cat
                                }
                            }
                            activeCategoryForNewItem = null
                        }
                    )
                }
            }
        }
    }
}