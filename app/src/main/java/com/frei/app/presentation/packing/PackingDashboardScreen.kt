package com.frei.app.presentation.packing

import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import com.frei.app.ui.theme.FreiInk
import com.frei.app.ui.theme.FreiLightBg
import com.frei.app.ui.theme.FreiPurple
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class TripItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val destination: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingDashboardScreen(
    onBackClick: () -> Unit,
    onTripSelected: (String, String) -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    var trips by remember { mutableStateOf<List<TripItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddTripDialog by remember { mutableStateOf(false) }

    // ✅ Read directly from the root collection with the userId constraint filter
    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            db.collection("trips")
                .whereEqualTo("userId", currentUid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val fetchedTrips = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val title = doc.getString("title") ?: doc.getString("title") ?: ""
                        val destination = doc.getString("destination")
                        if (title.isNotEmpty()) TripItem(id, title, destination) else null
                    }
                    trips = fetchedTrips
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to load trips.", Toast.LENGTH_SHORT).show()
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Trips", color = FreiInk, fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(8.dp).size(38.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFECEAF3), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = com.frei.app.presentation.booking.flight.FreiInk)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTripDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Trip") },
                text = { Text("New Trip", fontWeight = FontWeight.Bold) },
                containerColor = FreiPurple,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(FreiLightBg)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = FreiPurple
                )
            } else if (trips.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No trips added yet. Tap + to add one!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trips, key = { it.id }) { trip ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTripSelected(trip.id, trip.title) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = trip.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FreiInk
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = trip.destination ?: "No destination set",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            if (showAddTripDialog) {
                var tripTitle by remember { mutableStateOf("") }
                var tripDest by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showAddTripDialog = false },
                    title = { Text("Create New Trip", fontWeight = FontWeight.Bold, color = FreiInk) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = tripTitle,
                                onValueChange = { tripTitle = it },
                                label = { Text("Trip Title (e.g., Goa 2026)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = tripDest,
                                onValueChange = { tripDest = it },
                                label = { Text("Destination") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (tripTitle.isNotBlank() && currentUid.isNotEmpty()) {
                                    val tripRef = db.collection("trips").document()

                                    // ✅ Use the correct root fields schema structure
                                    val tripData = hashMapOf(
                                        "id" to tripRef.id,
                                        "userId" to currentUid,
                                        "title" to tripTitle,
                                        "destination" to tripDest,
                                        "travelers" to 1,
                                        "budget" to "",
                                        "transport" to "Flight",
                                        "stay" to "Hotel"
                                    )

                                    tripRef.set(tripData)
                                        .addOnSuccessListener {
                                            trips = trips + TripItem(tripRef.id, tripTitle, tripDest)
                                            Toast.makeText(context, "Trip initialized successfully!", Toast.LENGTH_SHORT).show()
                                            showAddTripDialog = false
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to create trip.", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    showAddTripDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FreiPurple)
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddTripDialog = false }) {
                            Text("Cancel", color = FreiPurple)
                        }
                    }
                )
            }
        }
    }
}