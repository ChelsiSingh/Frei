package com.frei.app.presentation.booking.flight

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.data.model.flight.Flight
import com.frei.app.ui.theme.Orange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AdditionalTraveler(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Mr.",
    val firstName: String = "",
    val lastName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    viewModel: BookingDetailsViewModel,
    onBackClick: () -> Unit,
    onSelectSeatClick: (flightId: String, travelers: Int, name: String, email: String, phone: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flight Details", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk) },
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FreiBackground)
            )
        },
        containerColor = FreiBackground
    ) { innerPadding ->
        when (val state = uiState) {
            is FlightDetailUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = FreiPrimary) }

            is FlightDetailUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center
            ) { Text(state.message, color = Color.Red, fontWeight = FontWeight.Bold) }

            is FlightDetailUiState.Success -> {
                BookingDetailsContent(
                    flight = state.flight,
                    innerPadding = innerPadding,
                    onSelectSeatClick = onSelectSeatClick
                )
            }
        }
    }
}

@Composable
private fun BookingDetailsContent(
    flight: Flight,
    innerPadding: PaddingValues,
    onSelectSeatClick: (flightId: String, travelers: Int, name: String, email: String, phone: String) -> Unit  // was 4 params
) {

    var title by remember { mutableStateOf("Ms.") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }

    val travelers = 1

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var additionalTravelers by remember { mutableStateOf(listOf<AdditionalTraveler>()) }
    val totalTravelers = 1 + additionalTravelers.size
    val totalPrice = flight.price * totalTravelers

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dob = formatter.format(Date(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val emailRegex = remember { Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") }
    val isPrimaryValid = firstName.isNotBlank() && lastName.isNotBlank() &&
            email.matches(emailRegex) && mobile.length == 10
    val allTravelersValid = additionalTravelers.all { it.firstName.isNotBlank() && it.lastName.isNotBlank() }
    val isFormValid = isPrimaryValid && allTravelersValid

    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 18.dp)
    ) {
        Column(modifier = Modifier.weight(1f).verticalScrollWorkaround()) {
            Spacer(Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(Color(0xFF7C4DDB), Color(0xFF5A2EB8))), RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(30.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text(flight.airlineCode, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("${flight.airline} \u00B7 Economy", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text(
                                "${flight.flightNumber} \u00B7 ${if (flight.stops == 0) "Non-stop" else "${flight.stops} stop(s)"}",
                                fontSize = 11.sp, color = Color(0xFFE4D9FA)
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(flight.departureTime.take(16).substringAfter("T"), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text(flight.fromAirport, fontSize = 11.sp, color = Color(0xFFE4D9FA))
                        }
                        Spacer(Modifier.weight(1f))
                        Text("${flight.durationMinutes / 60}h ${flight.durationMinutes % 60}m", fontSize = 11.sp, color = Color(0xFFE4D9FA))
                        Spacer(Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(flight.arrivalTime.take(16).substringAfter("T"), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text(flight.toAirport, fontSize = 11.sp, color = Color(0xFFE4D9FA))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AmenityChip(icon = Icons.Default.Work, label = "7kg cabin", modifier = Modifier.weight(1f))
                AmenityChip(icon = Icons.Default.Luggage, label = "15kg check-in", modifier = Modifier.weight(1f))
                AmenityChip(icon = Icons.Default.LocalDining, label = "Meal incl.", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))
            Text("Traveler Details", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
            Spacer(Modifier.height(8.dp))
            Text("Please ensure that your name matches your govt. ID such as Aadhaar, Passport or Driver's License", fontSize = 11.sp, color = Orange)
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TravelerField(
                    label = "TITLE",
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.width(72.dp),
                )
                TravelerField(
                    label = "FIRST NAME",
                    value = firstName,
                    onValueChange = { firstName = it },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TravelerField(
                    label = "LAST NAME",
                    value = lastName,
                    onValueChange = { lastName = it },
                    modifier = Modifier.weight(1f),
                )
                TravelerField(
                    label = "DOB",
                    value = dob,
                    onValueChange = {},
                    modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                    readOnly = true,
                    trailingIcon = Icons.Default.CalendarMonth,
                )
            }

            Spacer(Modifier.height(8.dp))
            TravelerField(
                label = "EMAIL",
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                isError = email.isNotEmpty() && !email.matches(emailRegex),
                errorText = "Enter a valid email address"
            )

            Spacer(Modifier.height(8.dp))
            TravelerField(
                label = "MOBILE NUMBER",
                value = mobile,
                onValueChange = { input -> mobile = input.filter { it.isDigit() }.take(10) },
                modifier = Modifier.fillMaxWidth(),
                isError = mobile.isNotEmpty() && mobile.length < 10,
                errorText = "Enter a valid 10-digit mobile number"
            )

            additionalTravelers.forEachIndexed { index, traveler ->
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Traveler ${index + 2}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { additionalTravelers = additionalTravelers.filterNot { it.id == traveler.id } },
                        modifier = Modifier.size(26.dp)
                    ) { Icon(Icons.Default.Close, contentDescription = "Remove traveler", tint = FreiSubtext, modifier = Modifier.size(16.dp)) }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TitleDropdownField(
                        selected = traveler.title,
                        onSelect = { newTitle -> additionalTravelers = additionalTravelers.map { if (it.id == traveler.id) it.copy(title = newTitle) else it } },
                        modifier = Modifier.width(90.dp)
                    )
                    TravelerField(
                        label = "FIRST NAME",
                        value = traveler.firstName,
                        onValueChange = { newVal -> additionalTravelers = additionalTravelers.map { if (it.id == traveler.id) it.copy(firstName = newVal) else it } },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                TravelerField(
                    label = "LAST NAME",
                    value = traveler.lastName,
                    onValueChange = { newVal -> additionalTravelers = additionalTravelers.map { if (it.id == traveler.id) it.copy(lastName = newVal) else it } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { additionalTravelers = additionalTravelers + AdditionalTraveler() }) {
                Text("+ Add another traveler", color = FreiPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Total for $travelers", fontSize = 11.sp, color = FreiSubtext)
                Text("\u20B9${totalPrice.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
            }
            Button(
                onClick = {
                    onSelectSeatClick(flight.id, totalTravelers, "$title $firstName $lastName", email, mobile)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreiPrimary),
                enabled = isFormValid
            ) {
                Text("Select Seat", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun TitleDropdownField(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F2FC), RoundedCornerShape(13.dp))
                .clickable { expanded = true }.padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("TITLE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FreiSubtext)
                Text(selected, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FreiInk)
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = FreiSubtext, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("Mr.", "Mrs.", "Ms.").forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@Composable
private fun AmenityChip(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFF0EEF6), RoundedCornerShape(14.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = FreiPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FreiInk)
    }
}

@Composable
private fun TravelerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    trailingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorText: String? = null
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFFF5F2FC), RoundedCornerShape(13.dp))
                .then(if (isError) Modifier.border(1.5.dp, Color(0xFFD9534F), RoundedCornerShape(13.dp)) else Modifier)
                .padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FreiSubtext)
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    readOnly = readOnly,
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FreiInk),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (trailingIcon != null) {
                Icon(trailingIcon, contentDescription = null, tint = FreiSubtext, modifier = Modifier.size(20.dp))
            }
        }
        if (isError && errorText != null) {
            Spacer(Modifier.height(3.dp))
            Text(errorText, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD9534F))
        }
    }
}

private fun Modifier.verticalScrollWorkaround(): Modifier = this