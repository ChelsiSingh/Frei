package com.frei.app.presentation.mytrips

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.frei.app.data.model.Trip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FreiPurple = Color(0xFF6C3CF0)
private val FreiInk = Color(0xFF1B1830)
private val FreiMuted = Color(0xFF8C89A3)
private val FreiFieldBg = Color(0xFFF3F2F8)

private enum class TripFilter { UPCOMING, PAST }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TripsDashboardScreen(
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onNewTripClick: () -> Unit
) {
    var tripsList by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var tripPendingDelete by remember { mutableStateOf<Trip?>(null) }
    var tripMenuTarget by remember { mutableStateOf<Trip?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(TripFilter.UPCOMING) }

    DisposableEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        var listener: ListenerRegistration? = null

        if (uid == null) {
            isLoading = false
        } else {
            listener = FirebaseFirestore.getInstance()
                .collection("trips")
                .whereEqualTo("userId", uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        tripsList = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Trip::class.java)?.copy(id = doc.id)
                        }
                    }
                    isLoading = false
                }
        }

        onDispose { listener?.remove() }
    }

    val now = remember { System.currentTimeMillis() }

    val filteredTrips = remember(tripsList, searchQuery, selectedFilter) {
        tripsList
            .filter { trip ->
                val isUpcoming = (trip.returnDate ?: trip.departureDate ?: 0L) >= now
                if (selectedFilter == TripFilter.UPCOMING) isUpcoming else !isUpcoming
            }
            .filter { trip ->
                searchQuery.isBlank() ||
                        trip.title.contains(searchQuery, ignoreCase = true) ||
                        trip.destination.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it.departureDate ?: Long.MAX_VALUE }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                TopAppBar(
                    title = {
                        Text(
                            "My Trips",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = FreiInk
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

                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )

                // ---------- Search bar ----------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search trips", color = FreiMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FreiMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FreiFieldBg,
                            unfocusedContainerColor = FreiFieldBg,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FreiFieldBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = FreiInk)
                    }
                }

                // ---------- Upcoming / Past toggle ----------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp)
                        .background(FreiFieldBg, RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    listOf(TripFilter.UPCOMING to "Upcoming", TripFilter.PAST to "Past").forEach { (filter, label) ->
                        val selected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(11.dp))
                                .background(if (selected) FreiPurple else Color.Transparent)
                                .combinedClickable(onClick = { selectedFilter = filter })
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color.White else FreiInk,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewTripClick,
                containerColor = FreiPurple,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Trip")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FreiPurple
                    )
                }
                filteredTrips.isEmpty() -> {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No trips match your search"
                        else if (selectedFilter == TripFilter.UPCOMING) "No upcoming trips yet!"
                        else "No past trips yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = FreiMuted,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredTrips, key = { it.id }) { trip ->
                            TripCard(
                                trip = trip,
                                isUpcoming = (trip.returnDate ?: trip.departureDate ?: 0L) >= now,
                                onClick = { onTripClick(trip.id) },
                                onLongPress = { tripMenuTarget = trip }
                            )
                        }
                    }
                }
            }

            // Long-press options menu
            tripMenuTarget?.let { trip ->
                TripOptionsSheet(
                    trip = trip,
                    onDismiss = { tripMenuTarget = null },
                    onDeleteClick = {
                        tripMenuTarget = null
                        tripPendingDelete = trip
                    }
                )
            }

            tripPendingDelete?.let { trip ->
                ConfirmDeleteDialog(
                    title = "Delete this trip?",
                    message = "\"${trip.title}\" and everything saved under it — packing list, bookings, expenses — will be removed. This can't be undone.",
                    onConfirm = {
                        FirebaseFirestore.getInstance()
                            .collection("trips")
                            .document(trip.id)
                            .delete()
                        tripPendingDelete = null
                    },
                    onDismiss = { tripPendingDelete = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TripCard(
    trip: Trip,
    isUpcoming: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateRange = remember(trip.departureDate, trip.returnDate) {
        val start = trip.departureDate?.let { dateFormatter.format(Date(it)) }
        val end = trip.returnDate?.let { dateFormatter.format(Date(it)) }
        when {
            start != null && end != null -> "$start – $end"
            start != null -> start
            else -> "Dates TBD"
        }
    }
    val displayId = remember(trip.id) { "TRP-${trip.id.takeLast(6).uppercase(Locale.ROOT)}" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
    ) {
        if (!trip.coverImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = trip.coverImageUrl,
                contentDescription = trip.destination,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(destinationGradient(trip.destination))
            )
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        startY = 40f
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isUpcoming) Color(0xFF22C55E) else FreiMuted)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isUpcoming) "Upcoming" else "Past",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = trip.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: $displayId",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp
            )
            Text(
                text = dateRange,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Travelers",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${trip.travelers}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripOptionsSheet(
    trip: Trip,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = trip.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = FreiInk,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = onDeleteClick)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFE05252)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Delete Trip", color = Color(0xFFE05252), fontSize = 15.sp)
            }
        }
    }
}

private fun destinationGradient(destination: String): Brush {
    val palettes = listOf(
        listOf(Color(0xFF11998E), Color(0xFF38EF7D)), // beach/green
        listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF)), // city/steel blue
        listOf(Color(0xFF614385), Color(0xFF516395)), // mountain/purple
        listOf(Color(0xFFEB5757), Color(0xFFF2994A)), // sunset/orange
        listOf(Color(0xFF1FA2FF), Color(0xFF12D8FA))  // ocean/cyan
    )
    val index = if (destination.isBlank()) 0 else destination.hashCode().mod(palettes.size)
    return Brush.linearGradient(palettes[index])
}