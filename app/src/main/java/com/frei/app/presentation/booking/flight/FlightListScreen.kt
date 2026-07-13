package com.frei.app.presentation.booking.flight

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.data.model.flight.Flight
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

val FreiInk = Color(0xFF1B1830)
val FreiPrimary = Color(0xFF6C3FCF)
val FreiTealAccent = Color(0xFF14B8A6)
val FreiSubtext = Color(0xFF8C89A3)
val FreiBackground = Color(0xFFFAFAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightListScreen(
    viewModel: FlightListViewModel,
    onBackClick: () -> Unit,
    onEditSearchClick: () -> Unit,
    onBookFlight: (Flight) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "${viewModel.origin} \u21C4 ${viewModel.destination}",
                            fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk
                        )
                        Text(
                            "${viewModel.departDate} \u00B7 ${viewModel.paxCount} " +
                                    (if (viewModel.paxCount > 1) "Travellers" else "Traveller") + " \u00B7 Economy",
                            fontSize = 12.sp, color = FreiSubtext
                        )
                    }
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
                    IconButton(
                        onClick = onEditSearchClick,
                        modifier = Modifier.padding(8.dp).size(38.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFECEAF3), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit search", tint = FreiPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FreiBackground)
            )
        },
        containerColor = FreiBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is FlightListUiState.Loading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = FreiPrimary)
                    Spacer(Modifier.height(12.dp))
                    Text("Getting all the available flights\u2026", color = FreiSubtext, fontSize = 13.sp)
                }

                is FlightListUiState.Success -> {
                    if (state.flights.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No flights found for this route and date.", color = FreiSubtext)
                        }
                    } else {
                        val cheapestId = state.flights.minByOrNull { it.price }?.id
                        LazyColumn(
                            contentPadding = PaddingValues(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Text("Onward", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
                                Spacer(Modifier.height(4.dp))
                            }
                            items(state.flights, key = { it.id }) { flight ->
                                FlightListCard(
                                    flight = flight,
                                    isCheapest = flight.id == cheapestId,
                                    onBookClick = { onBookFlight(flight) }
                                )
                            }
                        }
                    }
                }

                is FlightListUiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        TextButton(onClick = { viewModel.search() }) { Text("Retry") }
                    }
                }
            }
        }
    }
}

private fun formatIsoTime(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val displayFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val parsedDate = parser.parse(iso.substringBefore("."))
        if (parsedDate != null) displayFormat.format(parsedDate) else iso
    } catch (e: Exception) {
        iso
    }
}

@Composable
private fun FlightListCard(flight: Flight, isCheapest: Boolean, onBookClick: () -> Unit) {
    val priceLabel = if (flight.currency == "INR") "\u20B9${flight.price.toInt()}" else "${flight.currency} ${flight.price}"
    val stopsLabel = if (flight.stops == 0) "Non-stop" else "${flight.stops} stop(s)"
    val durationLabel = "${flight.durationMinutes / 60}h ${flight.durationMinutes % 60}m"

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF0EEF6), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(34.dp).background(Color(0xFFE7EFFB), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) { Text(flight.airlineCode, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2A4F9E)) }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(flight.airline, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
                    Text(flight.flightNumber, fontSize = 11.sp, color = FreiSubtext)
                }

                if (isCheapest) {
                    Box(
                        modifier = Modifier.background(FreiTealAccent.copy(alpha = 0.15f), RoundedCornerShape(99.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("CHEAPEST", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = FreiTealAccent)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(formatIsoTime(flight.departureTime), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
                    Text(flight.fromAirport, fontSize = 11.sp, color = FreiSubtext)
                }
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("$durationLabel \u00B7 $stopsLabel", fontSize = 10.sp, color = FreiSubtext)
                    HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color(0xFFE4E1EE))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatIsoTime(flight.arrivalTime), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
                    Text(flight.toAirport, fontSize = 11.sp, color = FreiSubtext)
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(priceLabel, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FreiPrimary, modifier = Modifier.weight(1f))
                Button(
                    onClick = onBookClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FreiPrimary)
                ) {
                    Text("Book", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}