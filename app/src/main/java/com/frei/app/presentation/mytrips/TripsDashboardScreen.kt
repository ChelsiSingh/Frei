package com.frei.app.presentation.mytrips

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.frei.app.data.model.Trip
import com.frei.app.presentation.booking.flight.FreiInk
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsDashboardScreen(
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onNewTripClick: () -> Unit
) {
    var tripsList by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        var listener: ListenerRegistration? = null

        if (uid == null) {
            isLoading = false
        }
        else {
            listener = FirebaseFirestore.getInstance()
                .collection("trips")
                .whereEqualTo("userId", uid)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        tripsList = snapshot.documents.mapNotNull { doc ->
                            val trip = doc.toObject(Trip::class.java)
                            trip?.copy(id = doc.id)
                        }
                    }
                    isLoading = false
                }
        }

        onDispose {
            listener?.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Trips✈️", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(8.dp).size(38.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFECEAF3), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreiInk)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewTripClick,
                containerColor = Color(0xFF6C3CF0),
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF6C3CF0)
                )
            } else if (tripsList.isEmpty()) {
                Text(
                    text = "No trips planned yet!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tripsList, key = { it.id }) { trip ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTripClick(trip.id) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F6FB))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = trip.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF1B1830)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 ${trip.destination}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF8C89A3)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}