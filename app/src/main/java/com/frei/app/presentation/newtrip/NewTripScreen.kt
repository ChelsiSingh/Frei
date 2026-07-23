package com.frei.app.presentation.newtrip

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.frei.app.presentation.auth.AuthGateBottomSheet
import com.frei.app.presentation.auth.AuthPromptDialog
import com.frei.app.presentation.auth.rememberAuthGateState
import com.frei.app.presentation.booking.flight.FreiInk
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Purple = Color(0xFF6C3CF0)

private val tripTypeOptions = listOf("Leisure", "Business", "Adventure", "Family")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTripScreen(

    viewModel: NewTripViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSaveSuccess: (String) -> Unit = {}
){
    var showStaySheet by remember { mutableStateOf(false) }
    var showDeparturePicker by remember { mutableStateOf(false) }
    var showReturnPicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val context = LocalContext.current

    val authGate = rememberAuthGateState { FirebaseAuth.getInstance().currentUser }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onCoverImageSelected(uri)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(text = "New Trip", style = MaterialTheme.typography.headlineSmall) },
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
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        authGate.requireAuth {
                            isSaving = true
                            viewModel.saveTripToFirestore(
                                onSuccess = { tripId ->
                                    isSaving = false
                                    Toast.makeText(
                                        context,
                                        "Trip saved successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    onSaveSuccess(tripId)
                                },
                                onFailure = {
                                    isSaving = false
                                    Toast.makeText(
                                        context,
                                        "Failed: ${it.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    enabled = viewModel.tripName.isNotBlank() && !isSaving
                ) {
                    Text(if (isSaving) "Saving..." else "Save Trip")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            CoverPhotoPicker(
                imageUri = viewModel.coverImageUri,
                onClick = { imagePickerLauncher.launch("image/*") }
            )

            TripInputField(
                label = "Trip Name",
                value = viewModel.tripName,
                leadingIcon = { Icon(imageVector = Icons.Default.Luggage, contentDescription = null) },
                onValueChange = viewModel::onTripNameChange
            )

            TripInputField(
                label = "Destination",
                value = viewModel.destination,
                leadingIcon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = null) },
                onValueChange = viewModel::onDestinationChange
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateInputField(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    label = "Departure",
                    value = viewModel.departureDate?.let { formatter.format(Date(it)) } ?: "",
                    onClick = { showDeparturePicker = true }
                )

                DateInputField(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    label = "Return",
                    value = viewModel.returnDate?.let { formatter.format(Date(it)) } ?: "",
                    onClick = { showReturnPicker = true }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TravelerCounter(
                    modifier = Modifier.weight(1f),
                    travelers = viewModel.travelers,
                    onIncrease = { viewModel.increaseTravelers() },
                    onDecrease = { if (viewModel.travelers > 1) viewModel.decreaseTravelers() }
                )

                TripInputField(
                    modifier = Modifier.weight(1f),
                    label = "Budget(₹)",
                    value = viewModel.budget,
                    leadingIcon = { Icon(imageVector = Icons.Default.Payments, contentDescription = null) },
                    onValueChange = viewModel::onBudgetChange
                )
            }

            Text(text = "Transportation", style = MaterialTheme.typography.titleMedium)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    TransportChip(
                        text = "Flight",
                        selected = viewModel.transport == "Flight",
                        onClick = { viewModel.updateTransport("Flight") }
                    )
                }
                item {
                    TransportChip(
                        text = "Bus",
                        selected = viewModel.transport == "Bus",
                        onClick = { viewModel.updateTransport("Bus") }
                    )
                }
                item {
                    TransportChip(
                        text = "Train",
                        selected = viewModel.transport == "Train",
                        onClick = { viewModel.updateTransport("Train") }
                    )
                }
            }

            TripInputField(
                label = "Stay",
                value = viewModel.stay,
                readOnly = true,
                leadingIcon = { Icon(imageVector = Icons.Default.Hotel, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showStaySheet = true }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Stay")
                    }
                }
            )

            TripTypeDropdown(
                selected = viewModel.tripType,
                options = tripTypeOptions,
                onSelect = viewModel::updateTripType
            )

            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (Optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    focusedLabelColor = Purple
                )
            )

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    if (showDeparturePicker) {
        TripDatePicker(
            title = "Select Departure Date",
            onDismiss = { showDeparturePicker = false },
            onDateSelected = { date ->
                viewModel.updateDepartureDate(date)
                showDeparturePicker = false
            }
        )
    }

    if (showReturnPicker) {
        TripDatePicker(
            title = "Select Return Date",
            onDismiss = { showReturnPicker = false },
            onDateSelected = { date ->
                viewModel.updateReturnDate(date)
                showReturnPicker = false
            }
        )
    }

    if (showStaySheet) {
        StayPickerBottomSheet(
            selected = viewModel.stay,
            onSelect = { option ->
                viewModel.updateStay(option)
                showStaySheet = false
            },
            onDismiss = { showStaySheet = false }
        )
    }

    if (authGate.showPrompt) {
        AuthPromptDialog(
            onLoginClick = authGate::onLoginClicked,
            onDismiss = authGate::dismiss
        )
    }

    if (authGate.showAuthSheet) {
        AuthGateBottomSheet(
            onDismiss = authGate::dismiss,
            onAuthSuccess = authGate::onAuthSuccess
        )
    }
}

@Composable
private fun CoverPhotoPicker(
    imageUri: android.net.Uri?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (imageUri == null) {
                    Modifier.dashedBorder(
                        color = Purple,
                        cornerRadiusDp = 16.dp
                    )
                } else Modifier
            )
            .background(if (imageUri == null) Purple.copy(alpha = 0.04f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Trip cover photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Purple
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Add Cover Photo",
                    color = Purple,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/** Simple dashed-border modifier used for the empty cover-photo placeholder. */
private fun Modifier.dashedBorder(color: Color, cornerRadiusDp: androidx.compose.ui.unit.Dp) = this.then(
    Modifier.border(
        width = 1.5.dp,
        color = color,
        shape = RoundedCornerShape(cornerRadiusDp)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripTypeDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Trip Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple,
                focusedLabelColor = Purple,
                focusedTrailingIconColor = Purple
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun DateInputField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = true,
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6C3CF0),
                focusedLabelColor = Color(0xFF6C3CF0),
                focusedLeadingIconColor = Color(0xFF6C3CF0),
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}