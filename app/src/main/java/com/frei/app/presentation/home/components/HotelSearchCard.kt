package com.frei.app.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.frei.app.data.model.hotel.City
import com.frei.app.presentation.booking.hotel.CitySuggestionUiState
import com.frei.app.presentation.booking.hotel.HotelViewModel
import com.frei.app.ui.theme.FreiPurple
import com.frei.app.ui.theme.FreiTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelSearchCard(
    viewModel: HotelViewModel,
    modifier: Modifier = Modifier,
    // minStars is the ONLY param that's real right now — see note below.
    onSearchClicked: (cityId: String, cityName: String, minStars: Double?) -> Unit = { _, _, _ -> }
) {
    val citySuggestionState by viewModel.citySuggestionState.collectAsState()
    val citySuggestions = (citySuggestionState as? CitySuggestionUiState.Success)?.cities ?: emptyList()

    var cityText by remember { mutableStateOf("") }
    var cityId by remember { mutableStateOf<String?>(null) }
    var showCityDropdown by remember { mutableStateOf(false) }

    var selectedMinStars by remember { mutableStateOf<Double?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangeState = rememberDateRangePickerState()
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val checkInString = dateRangeState.selectedStartDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Select"
    val checkOutString = dateRangeState.selectedEndDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Select"
    var guestCount by remember { mutableIntStateOf(2) }
    var showGuestDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, null, tint = FreiTeal, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("City", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Box {
                        if (cityText.isEmpty()) {
                            Text("Where are you staying?", style = TextStyle(color = Color.LightGray, fontSize = 15.sp))
                        }
                        BasicTextField(
                            value = cityText,
                            onValueChange = {
                                cityText = it
                                cityId = null
                                showCityDropdown = true
                                viewModel.onCityQueryChanged(it)
                            },
                            textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                        )
                        DropdownMenu(
                            expanded = showCityDropdown && citySuggestions.isNotEmpty(),
                            onDismissRequest = {
                                showCityDropdown = false
                                viewModel.clearCitySuggestions()
                            },
                            properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
                        ) {
                            citySuggestions.forEach { city: City ->
                                DropdownMenuItem(
                                    text = { Text(city.name) },
                                    onClick = {
                                        cityText = city.name
                                        cityId = city.id
                                        showCityDropdown = false
                                        viewModel.clearCitySuggestions()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoBoxHotel(
                    title = "CHECK-IN",
                    value = checkInString,
                    modifier = Modifier.weight(1f).clickable { showDatePicker = true }
                )
                InfoBoxHotel(
                    title = "CHECK-OUT",
                    value = checkOutString,
                    modifier = Modifier.weight(1f).clickable { showDatePicker = true }
                )
                Box(modifier = Modifier.weight(1f)) {
                    InfoBoxHotel(
                        title = "GUESTS",
                        value = "$guestCount ${if (guestCount > 1) "Guests" else "Guest"}",
                        modifier = Modifier.fillMaxWidth().clickable { showGuestDropdown = true }
                    )
                    DropdownMenu(expanded = showGuestDropdown, onDismissRequest = { showGuestDropdown = false }) {
                        (1..6).forEach { quantity ->
                            DropdownMenuItem(
                                text = { Text("$quantity ${if (quantity > 1) "Guests" else "Guest"}") },
                                onClick = { guestCount = quantity; showGuestDropdown = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Minimum star rating", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null, 3.0, 4.0, 5.0).forEach { stars ->
                    val label = if (stars == null) "Any" else "${stars.toInt()}+"
                    val selected = selectedMinStars == stars
                    Box(
                        modifier = Modifier
                            .background(if (selected) FreiPurple else Color(0xFFF4F1FC), RoundedCornerShape(99.dp))
                            .clickable { selectedMinStars = stars }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val canSearch = cityId != null

            Button(
                onClick = {
                    val id = cityId
                    if (id != null) onSearchClicked(id, cityText, selectedMinStars)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = FreiPurple),
                shape = RoundedCornerShape(14.dp),
                enabled = canSearch
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search Hotels")
            }

            if (!canSearch) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Pick a city from the list so we search the right hotels.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("Done") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DateRangePicker(state = dateRangeState, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun InfoBoxHotel(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(Color(0xFFF8F7FC), RoundedCornerShape(12.dp)).padding(10.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}