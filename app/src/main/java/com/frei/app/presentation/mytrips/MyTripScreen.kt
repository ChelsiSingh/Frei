package com.frei.app.presentation.mytrips

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.data.model.Expense
import com.frei.app.data.model.ExpenseSource
import com.frei.app.data.model.Trip
import com.frei.app.data.repository.BookingRepositoryImpl
import com.frei.app.data.repository.FirestoreExpenseRepository
import com.frei.app.data.repository.FlightBookingRecord
import com.frei.app.data.repository.HotelBookingRecord
import com.frei.app.data.repository.PackingRepository
import com.frei.app.presentation.booking.flight.FreiInk
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
private val FreiPurpleLight = Color(0xFFF0EAFB)
private val FreiTeal = Color(0xFF14B8A6)
private val FreiTealLight = Color(0xFFE5F9F6)
private val FreiLine = Color(0xFFECE8F5)
private val FreiInkDim = Color(0xFF6F6C79)
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

// ---------- Trip tab building blocks ----------

@Composable
private fun TripSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = FreiInkDim,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 22.dp, bottom = 10.dp)
    )
}

@Composable
private fun TripDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    badge: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = FreiInkDim, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = FreiInk, fontWeight = FontWeight.Bold)
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(FreiTealLight)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(text = badge, color = FreiTeal, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PackingCategorySection(
    category: PackingCategory,
    onToggleItem: (String) -> Unit,
    onAddItemClick: () -> Unit,
    onDeleteCategory: () -> Unit,
    onDeleteItem: (PackingItem) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val packedCount = category.items.count { it.isPacked }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, FreiLine),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, top = 16.dp, end = 8.dp, bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FreiInk
                    )
                    if (category.items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$packedCount of ${category.items.size} packed",
                            style = MaterialTheme.typography.labelSmall,
                            color = FreiInkDim
                        )
                    }
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Category options",
                            tint = FreiInkDim
                        )
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Delete category", color = Color(0xFFE0453C), fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE0453C))
                            },
                            onClick = {
                                menuExpanded = false
                                onDeleteCategory()
                            }
                        )
                    }
                }
            }

            if (category.items.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No items added yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FreiInkDim,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = FreiLine)
                category.items.forEach { item ->
                    PackingItemRow(
                        item = item,
                        onToggle = { onToggleItem(item.id) },
                        onDelete = { onDeleteItem(item) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAddItemClick() }
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = FreiPurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add item", color = FreiPurple, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun PackingItemRow(item: PackingItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .padding(vertical = 9.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (item.isPacked) Icons.Default.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (item.isPacked) FreiTeal else Color(0xFFCFCBDE),
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (item.isPacked) FreiInkDim else FreiInk,
            textDecoration = if (item.isPacked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove ${item.name}",
                tint = Color(0xFFCFCBDE),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


@Composable
private fun TripDetailsTab(trip: Trip) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FreiLightBg),
        contentPadding = PaddingValues(20.dp)
    ) {
        item {
            // Hero card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(colors = listOf(FreiPurple, Color(0xFF8B5FE0))))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DESTINATION",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = trip.destination,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Trip details at a glance",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            TripSectionLabel("Trip Info")

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, FreiLine),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    TripDetailRow(
                        icon = Icons.Default.Group,
                        iconTint = FreiPurple,
                        iconBg = FreiPurpleLight,
                        label = "Number of people",
                        value = if (trip.travelers == 1) "1 traveler" else "${trip.travelers} travelers"
                    )
                    HorizontalDivider(color = FreiLine)
                    TripDetailRow(
                        icon = Icons.Default.Flight,
                        iconTint = FreiTeal,
                        iconBg = FreiTealLight,
                        label = "Transport",
                        value = trip.transport
                    )
                    HorizontalDivider(color = FreiLine)
                    TripDetailRow(
                        icon = Icons.Default.Hotel,
                        iconTint = FreiPurple,
                        iconBg = FreiPurpleLight,
                        label = "Stay type",
                        value = trip.stay
                    )
                }
            }

            TripSectionLabel("Budget")

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, FreiLine),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FreiTealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Savings, contentDescription = null, tint = FreiTeal, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = "ALLOCATED", style = MaterialTheme.typography.labelSmall, color = FreiInkDim, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${trip.budget.ifBlank { "0" }}",
                            style = MaterialTheme.typography.titleLarge,
                            color = FreiInk,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

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
    var categoryPendingDelete by remember { mutableStateOf<PackingCategory?>(null) }

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
    var expensePendingDelete by remember { mutableStateOf<Expense?>(null) }

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
                            TripTab.Trip -> TripDetailsTab(trip = trip)

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
                                                PackingCategorySection(
                                                    category = category,
                                                    onToggleItem = { itemId ->
                                                        packingCategories = packingCategories.map { cat ->
                                                            if (cat.id == category.id) {
                                                                cat.copy(items = cat.items.map { item ->
                                                                    if (item.id == itemId) item.copy(isPacked = !item.isPacked) else item
                                                                })
                                                            } else cat
                                                        }
                                                    },
                                                    onAddItemClick = { activeCategoryForNewItem = category },
                                                    onDeleteCategory = { categoryPendingDelete = category },
                                                    onDeleteItem = { item ->
                                                        packingCategories = packingCategories.map { cat ->
                                                            if (cat.id == category.id) {
                                                                cat.copy(items = cat.items.filterNot { it.id == item.id })
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
                                                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
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
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "₹${expense.amount}",
                                                            fontWeight = FontWeight.Bold,
                                                            color = FreiPurple
                                                        )
                                                        IconButton(onClick = { expensePendingDelete = expense }) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete ${expense.title}",
                                                                tint = Color(0xFF8C89A3)
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

                categoryPendingDelete?.let { category ->
                    ConfirmDeleteDialog(
                        title = "Delete this category?",
                        message = "\"${category.name}\" and all ${category.items.size} item(s) in it will be removed from your packing list.",
                        onConfirm = {
                            val updatedCategories = packingCategories.filterNot { it.id == category.id }
                            packingCategories = updatedCategories
                            PackingRepository.savePackingListToFirestore(
                                userId = currentUid,
                                tripId = tripId,
                                categories = updatedCategories,
                                onSuccess = {},
                                onFailure = {
                                    Toast.makeText(context, "Couldn't delete category", Toast.LENGTH_SHORT).show()
                                }
                            )
                            categoryPendingDelete = null
                        },
                        onDismiss = { categoryPendingDelete = null }
                    )
                }

                expensePendingDelete?.let { expense ->
                    ConfirmDeleteDialog(
                        title = "Delete this expense?",
                        message = "\"${expense.title}\" (₹${expense.amount}) will be removed from this trip's expenses.",
                        onConfirm = {
                            coroutineScope.launch {
                                runCatching { expenseRepository.deleteExpense(expense) }
                                    .onFailure {
                                        Toast.makeText(context, "Couldn't delete expense", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            expensePendingDelete = null
                        },
                        onDismiss = { expensePendingDelete = null }
                    )
                }
            }
        }
    }
}