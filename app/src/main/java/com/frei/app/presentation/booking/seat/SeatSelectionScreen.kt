package com.frei.app.presentation.booking.seat

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.frei.app.data.model.flight.SeatClass
import com.frei.app.data.model.flight.SeatInfo
import com.frei.app.data.model.flight.SeatStatus

private val PurplePrimary = Color(0xFF6C3FCF)
private val PurpleGradientStart = Color(0xFF7C4DDB)
private val TealPremium = Color(0xFF14B8A6)
private val TealPremiumBg = Color(0xFFCFF3EC)
private val OccupiedGrey = Color(0xFFE4E1EE)
private val BorderGrey = Color(0xFFD9D5E8)
private val MutedText = Color(0xFF8C89A3)

@Composable
fun SeatSelectionScreen(
    onBack: () -> Unit,
    onContinue: (seat: SeatInfo) -> Unit,
    viewModel: SeatSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECEAF3), RoundedCornerShape(12.dp))
                ) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Select Seat", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            }
        },
        bottomBar = {
            val ready = uiState as? SeatSelectionUiState.Ready
            val selected = ready?.selectedSeat
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Divider(color = Color(0xFFF0EEF6))
                Row(
                    Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (selected != null)
                                "Seat ${selected.seatNumber}" +
                                        (if (selected.seatClass == SeatClass.PREMIUM) " · Premium · +₹${selected.extraPrice.toInt()}" else "")
                            else "No seat selected",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedText
                        )
                    }
                    Button(
                        onClick = { selected?.let(onContinue) },
                        enabled = selected != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleGradientStart),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Text("Continue", fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is SeatSelectionUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = PurplePrimary) }

            is SeatSelectionUiState.Failed -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(state.message, color = MutedText) }

            is SeatSelectionUiState.Ready -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp)
            ) {
                Text(state.flightMeta.let { "${state.flightRoute} · $it" }, fontSize = 11.sp, color = MutedText)
                Spacer(Modifier.height(10.dp))
                Legend()
                Spacer(Modifier.height(12.dp))
                Text(
                    "FRONT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB6B3C6),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.rows) { row ->
                        SeatRow(row = row, onSeatClick = viewModel::selectSeat)
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }
}

@Composable
private fun Legend() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LegendItem(BorderGrey, Color.White, "Available")
        LegendItem(PurplePrimary, PurplePrimary, "Selected")
        LegendItem(OccupiedGrey, OccupiedGrey, "Occupied")
        LegendItem(TealPremium, TealPremiumBg, "Premium")
    }
}

@Composable
private fun LegendItem(border: Color, fill: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(fill)
                .border(1.5.dp, border, RoundedCornerShape(5.dp))
        )
        Text(label, fontSize = 10.sp, color = Color(0xFF6E6B82), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SeatRow(row: List<SeatInfo>, onSeatClick: (SeatInfo) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.take(3).forEach { Seat(it, onSeatClick) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.drop(3).forEach { Seat(it, onSeatClick) }
        }
    }
}

@Composable
private fun Seat(seat: SeatInfo, onClick: (SeatInfo) -> Unit) {
    val (bg, border) = when {
        seat.status == SeatStatus.SELECTED -> PurplePrimary to PurplePrimary
        seat.status == SeatStatus.OCCUPIED -> OccupiedGrey to OccupiedGrey
        seat.seatClass == SeatClass.PREMIUM -> TealPremiumBg to TealPremium
        else -> Color.White to BorderGrey
    }
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = seat.status != SeatStatus.OCCUPIED) { onClick(seat) },
        contentAlignment = Alignment.Center
    ) {
        if (seat.status == SeatStatus.SELECTED) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        }
    }
}