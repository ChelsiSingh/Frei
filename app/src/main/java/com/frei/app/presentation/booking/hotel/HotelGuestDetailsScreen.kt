package com.frei.app.presentation.booking.hotel

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.data.model.hotel.Hotel
import com.frei.app.presentation.booking.flight.FreiBackground
import com.frei.app.presentation.booking.flight.FreiInk
import com.frei.app.presentation.booking.flight.FreiPrimary
import com.frei.app.presentation.booking.flight.FreiSubtext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AdditionalGuest(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Mr.",
    val firstName: String = "",
    val lastName: String = ""
)

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM ''yy", Locale.getDefault()).format(Date(millis))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelGuestDetailsScreen(
    viewModel: HotelDetailsViewModel,
    onBackClick: () -> Unit,
    onContinueClick: (
        hotelId: String, guests: Int, name: String, email: String, phone: String,
        nights: Int, checkIn: String, checkOut: String
    ) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Your Details", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(8.dp).size(38.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreiInk) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FreiBackground)
            )
        },
        containerColor = FreiBackground
    ) { innerPadding ->
        when (val state = uiState) {
            is HotelDetailUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = FreiPrimary) }

            is HotelDetailUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center
            ) { Text(state.message, color = Color.Red, fontWeight = FontWeight.Bold) }

            is HotelDetailUiState.Success -> GuestDetailsContent(
                hotel = state.hotel,
                innerPadding = innerPadding,
                onContinueClick = onContinueClick
            )
        }
    }
}

@Composable
private fun GuestDetailsContent(
    hotel: Hotel,
    innerPadding: PaddingValues,
    onContinueClick: (
        hotelId: String, guests: Int, name: String, email: String, phone: String,
        nights: Int, checkIn: String, checkOut: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("Mr.") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var checkInMillis by remember { mutableStateOf<Long?>(null) }
    var checkOutMillis by remember { mutableStateOf<Long?>(null) }

    var additionalGuests by remember { mutableStateOf(listOf<AdditionalGuest>()) }
    val emailRegex = remember { Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") }

    val totalGuests = 1 + additionalGuests.size
    val priceLabel = if (hotel.currency == "INR") "\u20B9${hotel.pricePerNight}" else "${hotel.currency} ${hotel.pricePerNight}"

    val nights = remember(checkInMillis, checkOutMillis) {
        val inM = checkInMillis; val outM = checkOutMillis
        if (inM != null && outM != null && outM > inM) ((outM - inM) / 86_400_000L).toInt() else 1
    }
    val areDatesValid = checkInMillis != null && checkOutMillis != null && checkOutMillis!! > checkInMillis!!

    val isPrimaryValid = firstName.isNotBlank() && lastName.isNotBlank() &&
            email.matches(emailRegex) && mobile.length == 10
    val areAdditionalGuestsValid = additionalGuests.all { it.firstName.isNotBlank() && it.lastName.isNotBlank() }
    val isFormValid = isPrimaryValid && areAdditionalGuestsValid && areDatesValid

    Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 18.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateChip(
                    label = "CHECK-IN",
                    dateMillis = checkInMillis,
                    modifier = Modifier.weight(1f)
                ) { picked ->
                    checkInMillis = picked
                    if (checkOutMillis != null && checkOutMillis!! <= picked) checkOutMillis = null
                }
                DateChip(
                    label = "CHECK-OUT",
                    dateMillis = checkOutMillis,
                    minMillis = checkInMillis,
                    modifier = Modifier.weight(1f)
                ) { picked -> checkOutMillis = picked }
            }
            Spacer(Modifier.height(10.dp))

            TitleToggleRow(selected = title, onSelect = { title = it })
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GuestField(label = "FIRST NAME *", value = firstName, onValueChange = { firstName = it }, modifier = Modifier.weight(1f))
                GuestField(label = "LAST NAME *", value = lastName, onValueChange = { lastName = it }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            GuestField(
                label = "EMAIL ADDRESS *",
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                isError = email.isNotEmpty() && !email.matches(emailRegex),
                errorText = "Enter a valid email address"
            )
            Spacer(Modifier.height(8.dp))
            GuestField(
                label = "MOBILE NUMBER *",
                value = mobile,
                onValueChange = { input -> mobile = input.filter { it.isDigit() }.take(10) },
                modifier = Modifier.fillMaxWidth(),
                isError = mobile.isNotEmpty() && mobile.length < 10,
                errorText = "Enter a valid 10-digit mobile number"
            )

            Spacer(Modifier.height(6.dp))

            additionalGuests.forEachIndexed { index, guest ->
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Guest ${index + 2}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { additionalGuests = additionalGuests.filterNot { it.id == guest.id } },
                        modifier = Modifier.size(26.dp)
                    ) { Icon(Icons.Default.Close, contentDescription = "Remove guest", tint = FreiSubtext, modifier = Modifier.size(16.dp)) }
                }
                Spacer(Modifier.height(6.dp))
                TitleToggleRow(
                    selected = guest.title,
                    onSelect = { newTitle ->
                        additionalGuests = additionalGuests.map { if (it.id == guest.id) it.copy(title = newTitle) else it }
                    }
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GuestField(
                        label = "FIRST NAME",
                        value = guest.firstName,
                        onValueChange = { newVal -> additionalGuests = additionalGuests.map { if (it.id == guest.id) it.copy(firstName = newVal) else it } },
                        modifier = Modifier.weight(1f)
                    )
                    GuestField(
                        label = "LAST NAME",
                        value = guest.lastName,
                        onValueChange = { newVal -> additionalGuests = additionalGuests.map { if (it.id == guest.id) it.copy(lastName = newVal) else it } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { additionalGuests = additionalGuests + AdditionalGuest() }) {
                Text("+ Add another guest", color = FreiPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(11.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Brush.linearGradient(listOf(Color(0xFFD9CBF2), Color(0xFFB79AE0))), RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(hotel.name, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk, maxLines = 1)
                    Text(
                        "1 \u00D7 Studio Suite \u00B7 $totalGuests ${if (totalGuests > 1) "Guests" else "Guest"} \u00B7 $nights ${if (nights == 1) "Night" else "Nights"}",
                        fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = FreiSubtext
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Per night", fontSize = 11.sp, color = FreiSubtext)
                Text(priceLabel, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
            }
            Button(
                onClick = {
                    onContinueClick(
                        hotel.id, totalGuests, "$title $firstName $lastName", email, mobile,
                        nights,
                        checkInMillis?.let { formatDate(it) } ?: "",
                        checkOutMillis?.let { formatDate(it) } ?: ""
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreiPrimary),
                enabled = isFormValid
            ) {
                Text("Continue", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun DateChip(
    label: String,
    dateMillis: Long?,
    modifier: Modifier = Modifier,
    minMillis: Long? = null,
    onPick: (Long) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier
            .background(Color(0xFFF5F2FC), RoundedCornerShape(11.dp))
            .clickable {
                val cal = Calendar.getInstance()
                dateMillis?.let { cal.timeInMillis = it } ?: minMillis?.let { cal.timeInMillis = it }
                val dialog = DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val picked = Calendar.getInstance().apply {
                            set(year, month, day, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onPick(picked.timeInMillis)
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                )
                minMillis?.let { dialog.datePicker.minDate = it + 86_400_000L }
                dialog.show()
            }
            .padding(horizontal = 11.dp, vertical = 10.dp)
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FreiSubtext)
        Spacer(Modifier.height(3.dp))
        Text(dateMillis?.let { formatDate(it) } ?: "Select", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = FreiInk)
    }
}

@Composable
private fun TitleToggleRow(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Mr.", "Mrs.", "Miss.").forEach { option ->
            val isSelected = selected == option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(if (isSelected) Color(0xFFF5F2FC) else Color.Transparent, RoundedCornerShape(99.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .clickableNoRipple { onSelect(option) }
            ) {
                Text(option, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isSelected) FreiPrimary else FreiSubtext)
            }
        }
    }
}

@Composable
private fun GuestField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FreiSubtext)
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFFF5F2FC), RoundedCornerShape(11.dp))
                .then(if (isError) Modifier.border(1.5.dp, Color(0xFFD9534F), RoundedCornerShape(11.dp)) else Modifier)
                .padding(horizontal = 11.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = FreiInk),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (isError && errorText != null) {
            Spacer(Modifier.height(3.dp))
            Text(errorText, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD9534F))
        }
    }
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.background(Color.Transparent)
).let {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}