package com.frei.app.presentation.mytrips

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frei.app.data.model.Trip
import com.frei.app.data.repository.PackingRepository
import com.frei.app.presentation.booking.flight.FreiInk
import com.frei.app.presentation.packing.CategoryCard
import com.frei.app.presentation.packing.InputDialog
import com.frei.app.presentation.packing.PackingCategory
import com.frei.app.presentation.packing.PackingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

private val FreiPurple = Color(0xFF6C3CF0)
private val FreiLightBg = Color(0xFFF7F6FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripScreen(
    tripId: String,
    onBackClick: () -> Unit
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == TripTab.Packing) "Packing List" else (tripDetails?.title ?: "Trip Details"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(8.dp).size(38.dp)
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
            if (selectedTab == TripTab.Packing && !isLoadingPacking) {
                ExtendedFloatingActionButton(
                    onClick = { showAddCategoryDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("Add Category", fontWeight = FontWeight.Bold) },
                    containerColor = FreiPurple,
                    contentColor = Color.White
                )
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
                            TripTab.Flights -> {
                                Text(
                                    text = "✈️ Flight Schedules Coming Soon!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            TripTab.Hotels -> {
                                Text(
                                    text = "🏨 Hotel Reservations Coming Soon!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.Center)
                                )
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