package com.frei.app.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.frei.app.data.model.flight.Airport
import com.frei.app.presentation.booking.flight.AirportUiState
import com.frei.app.presentation.booking.flight.FlightViewModel
import com.frei.app.ui.theme.FreiPurple
import com.frei.app.ui.theme.FreiTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchCard(
    viewModel: FlightViewModel,
    modifier: Modifier = Modifier,
    onSearchClicked: (
        originId: String,
        destinationId: String,
        isRoundTrip: Boolean,
        departDate: Long?,
        returnDate: Long?,
        paxCount: Int
    ) -> Unit = { _, _, _, _, _, _ -> }
) {
    val isRoundTrip by viewModel.isRoundTrip.collectAsState()
    val originState by viewModel.originState.collectAsState()
    val destinationState by viewModel.destinationState.collectAsState()

    var currentLocationText by remember { mutableStateOf(viewModel.initialOrigin) }
    var destinationText by remember { mutableStateOf(viewModel.initialDestination) }

    // Actual IDs (airport codes) sent to the search API
    var originId by remember { mutableStateOf<String?>(if (viewModel.initialOrigin.isNotEmpty()) viewModel.initialOrigin else null) }
    var destinationId by remember { mutableStateOf<String?>(if (viewModel.initialDestination.isNotEmpty()) viewModel.initialDestination else null) }

    var paxCount by remember { mutableIntStateOf(viewModel.initialPaxCount) }

    var showCurrentLocationDropdown by remember { mutableStateOf(false) }
    var showDestinationDropdown by remember { mutableStateOf(false) }

    val originSuggestions = (originState as? AirportUiState.Success)?.airports ?: emptyList()
    val destinationSuggestions = (destinationState as? AirportUiState.Success)?.airports ?: emptyList()

    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangeState = rememberDateRangePickerState()

    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val departDateString = dateRangeState.selectedStartDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Select"
    val returnDateString = dateRangeState.selectedEndDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Select"

    var showPaxDropdown by remember { mutableStateOf(false) }

    fun Airport.displayLabel(): String = "$name ($code)"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            TripTypeSliderToggle(
                isRoundTrip = isRoundTrip,
                onToggleChange = { viewModel.setRoundTrip(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = FreiPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text("From", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Box {
                                if (currentLocationText.isEmpty()) {
                                    Text("Current Location", style = TextStyle(color = Color.LightGray, fontSize = 15.sp))
                                }
                                BasicTextField(
                                    value = currentLocationText,
                                    onValueChange = {
                                        currentLocationText = it
                                        originId = null
                                        showCurrentLocationDropdown = true
                                        showDestinationDropdown = false
                                        viewModel.onOriginQueryChanged(it)
                                    },
                                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                                )

                                DropdownMenu(
                                    expanded = showCurrentLocationDropdown && originSuggestions.isNotEmpty(),
                                    onDismissRequest = {
                                        showCurrentLocationDropdown = false
                                        viewModel.clearOriginSuggestions()
                                    },
                                    properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
                                ) {
                                    originSuggestions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item.displayLabel()) },
                                            onClick = {
                                                currentLocationText = item.name
                                                originId = item.code
                                                showCurrentLocationDropdown = false
                                                viewModel.clearOriginSuggestions()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, tint = FreiTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text("To", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Box {
                                if (destinationText.isEmpty()) {
                                    Text("Where to?", style = TextStyle(color = Color.LightGray, fontSize = 15.sp))
                                }
                                BasicTextField(
                                    value = destinationText,
                                    onValueChange = {
                                        destinationText = it
                                        destinationId = null
                                        showDestinationDropdown = true
                                        showCurrentLocationDropdown = false
                                        viewModel.onDestinationQueryChanged(it)
                                    },
                                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                                )

                                DropdownMenu(
                                    expanded = showDestinationDropdown && destinationSuggestions.isNotEmpty(),
                                    onDismissRequest = {
                                        showDestinationDropdown = false
                                        viewModel.clearDestinationSuggestions()
                                    },
                                    properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
                                ) {
                                    destinationSuggestions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item.displayLabel()) },
                                            onClick = {
                                                destinationText = item.name
                                                destinationId = item.code
                                                showDestinationDropdown = false
                                                viewModel.clearDestinationSuggestions()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF4F1FC)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clickable {
                            val tmpText = currentLocationText
                            val tmpId = originId
                            currentLocationText = destinationText
                            originId = destinationId
                            destinationText = tmpText
                            destinationId = tmpId
                        }
                    ) {
                        Icon(Icons.Default.SwapVert, null, tint = FreiPurple)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoBox(
                    title = "DEPARTURE",
                    value = departDateString,
                    modifier = Modifier.weight(1f).clickable { showDatePicker = true }
                )

                AnimatedVisibility(
                    visible = isRoundTrip,
                    modifier = Modifier.weight(1f),
                    enter = fadeIn(animationSpec = tween(250)) + slideInHorizontally(animationSpec = tween(250)),
                    exit = fadeOut(animationSpec = tween(250)) + slideOutHorizontally(animationSpec = tween(250))
                ) {
                    InfoBox(
                        title = "RETURN",
                        value = returnDateString,
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    InfoBox(
                        title = "PAX",
                        value = "$paxCount ${if (paxCount > 1) "Adults" else "Adult"}",
                        modifier = Modifier.fillMaxWidth().clickable { showPaxDropdown = true }
                    )
                    DropdownMenu(expanded = showPaxDropdown, onDismissRequest = { showPaxDropdown = false }) {
                        (1..5).forEach { quantity ->
                            DropdownMenuItem(
                                text = { Text("$quantity ${if (quantity > 1) "Adults" else "Adult"}") },
                                onClick = { paxCount = quantity; showPaxDropdown = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val canSearch = originId != null && destinationId != null

            Button(
                onClick = {
                    val from = originId
                    val to = destinationId
                    if (from != null && to != null) {
                        onSearchClicked(
                            from,
                            to,
                            isRoundTrip,
                            dateRangeState.selectedStartDateMillis,
                            dateRangeState.selectedEndDateMillis,
                            paxCount
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = FreiPurple),
                shape = RoundedCornerShape(14.dp),
                enabled = canSearch
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search Flights")
            }

            if (!canSearch) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Pick a location from the list so we search the right airport.",
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
            DateRangePicker(
                state = dateRangeState,
                modifier = Modifier.weight(1f),
                title = { Text("Select travel windows", modifier = Modifier.padding(16.dp)) },
                headline = { Text("Choose Dates", modifier = Modifier.padding(horizontal = 16.dp)) }
            )
        }
    }
}

@Composable
private fun TripTypeSliderToggle(
    isRoundTrip: Boolean,
    onToggleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(44.dp)
            .background(Color(0xFFF4F1FC), RoundedCornerShape(12.dp)).padding(4.dp)
    ) {
        val tabWidth = maxWidth / 2
        val indicatorOffset by animateDpAsState(
            targetValue = if (isRoundTrip) tabWidth else 0.dp,
            animationSpec = tween(durationMillis = 250),
            label = "IndicatorOffset"
        )
        Box(
            modifier = Modifier.offset(x = indicatorOffset).width(tabWidth).fillMaxHeight()
                .padding(horizontal = 2.dp).background(Color.White, RoundedCornerShape(10.dp))
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val oneWayTextColor by animateColorAsState(if (!isRoundTrip) FreiPurple else Color.Gray, label = "OneWayColor")
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(10.dp))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onToggleChange(false) },
                contentAlignment = Alignment.Center
            ) {
                Text("One Way", color = oneWayTextColor, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!isRoundTrip) FontWeight.Bold else FontWeight.Normal)
            }
            val roundTripTextColor by animateColorAsState(if (isRoundTrip) FreiPurple else Color.Gray, label = "RoundTripColor")
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(10.dp))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onToggleChange(true) },
                contentAlignment = Alignment.Center
            ) {
                Text("Round Trip", color = roundTripTextColor, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isRoundTrip) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun InfoBox(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(Color(0xFFF8F7FC), RoundedCornerShape(12.dp)).padding(10.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
