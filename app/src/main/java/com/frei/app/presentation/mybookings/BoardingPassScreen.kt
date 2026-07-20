package com.frei.app.presentation.mybookings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddToHomeScreen
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.BarcodeBars
import com.frei.app.FakeQrCode
import com.frei.app.FreiDetailTopBar
import com.frei.app.FreiField
import com.frei.app.FreiFieldLabel
import com.frei.app.FreiGhostButton
import com.frei.app.FreiPrimaryButton
import com.frei.app.PerforatedDivider
import com.frei.app.ui.theme.*
import com.frei.app.ui.theme.FreiBg
import com.frei.app.ui.theme.FreiInkFaint
import com.frei.app.ui.theme.FreiInkSoft
import com.frei.app.ui.theme.FreiTealSoft
import com.frei.app.ui.theme.SecondaryMint
import com.frei.app.ui.theme.TextDarkInk

/**
 * Immutable snapshot of everything the boarding pass needs to render.
 * Populate this from your booking repository once a flight booking's
 * status flips to CONFIRMED (see FlightConfirmPayScreen -> Firestore write).
 */
data class BoardingPassUiState(
    val airline: String,
    val flightNumber: String,
    val travelClass: String,
    val fromCode: String,
    val fromCity: String,
    val toCode: String,
    val toCity: String,
    val durationLabel: String,
    val passengerName: String,
    val date: String,
    val boardingTime: String,
    val departureTime: String,
    val gate: String,
    val terminal: String,
    val seat: String,
    val baggageAllowance: String,
    val boardingGroup: String,
    val pnr: String
)

@Composable
fun BoardingPassScreen(
    state: BoardingPassUiState,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onAddToWalletClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FreiBg)
    ) {
        FreiDetailTopBar(
            title = "Boarding Pass",
            onBackClick = onBackClick,
            onShareClick = onShareClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.TouchApp,
                    contentDescription = null,
                    tint = FreiInkFaint,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Tap the pass to flip & scan",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = FreiInkFaint
                )
            }

            FlippableBoardingPassCard(state = state, modifier = Modifier.padding(bottom = 22.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                FreiGhostButton("Add to Wallet", Icons.Outlined.AddToHomeScreen, onAddToWalletClick)
                FreiPrimaryButton("Download", Icons.Outlined.Download, onDownloadClick)
            }

            InfoNote(
                text = "Web check-in opens 48 hours before departure. Arrive at the airport at least 2 hours prior to your flight."
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** The tappable, flippable card. Front = pass details, back = barcode/QR for gate scanning. */
@Composable
private fun FlippableBoardingPassCard(state: BoardingPassUiState, modifier: Modifier = Modifier) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "boardingPassFlip"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 430.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { flipped = !flipped }
    ) {
        if (rotation <= 90f) {
            BoardingPassFront(state)
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                BoardingPassBack(state)
            }
        }
    }
}

@Composable
private fun BoardingPassFront(state: BoardingPassUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
    ) {
        // Header: gradient banner with route
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FreiGradPurple)
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Flight, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${state.airline} · ${state.flightNumber}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(state.fromCode, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                        Text(state.fromCity, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.Flight, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(state.durationLabel, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(state.toCode, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                        Text(state.toCity, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Text(
                state.travelClass.uppercase(),
                color = Color.White,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Detail grid
        Column(modifier = Modifier.padding(20.dp)) {
            val fields = listOf(
                "Passenger" to state.passengerName,
                "Seat" to state.seat,
                "Gate" to state.gate,
                "Date" to state.date,
                "Boarding" to state.boardingTime,
                "Departure" to state.departureTime,
                "Terminal" to state.terminal,
                "Baggage" to state.baggageAllowance,
                "Boarding Grp" to state.boardingGroup
            )
            val accentFields = setOf("Seat", "Departure")
            fields.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { (label, value) ->
                        FreiField(
                            label = label,
                            value = value,
                            accent = label in accentFields,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        PerforatedDivider(backgroundColor = FreiBg, modifier = Modifier.padding(horizontal = 4.dp))

        // Stub
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                FreiFieldLabel("PNR")
                Text(state.pnr, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = TextDarkInk)
                Text("Booked via Frei", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = FreiInkSoft)
            }
            BarcodeBars(
                modifier = Modifier
                    .width(110.dp)
                    .height(34.dp),
                barCount = 22,
                seed = state.pnr.hashCode().toLong()
            )
        }
    }
}

@Composable
private fun BoardingPassBack(state: BoardingPassUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 430.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FakeQrCode(
            modifier = Modifier.size(170.dp),
            seed = state.pnr.hashCode().toLong()
        )
        Spacer(Modifier.height(18.dp))
        Text(state.pnr, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp, color = TextDarkInk)
        Spacer(Modifier.height(6.dp))
        Text(
            "Present this at the boarding gate",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = FreiInkSoft
        )
        Spacer(Modifier.height(22.dp))
        BarcodeBars(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            barCount = 40,
            seed = state.pnr.hashCode().toLong() + 1
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "${state.fromCode} · ${state.toCode} · ${state.flightNumber} · ${state.date}",
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            color = FreiInkFaint,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InfoNote(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FreiTealSoft, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = SecondaryMint, modifier = Modifier.size(18.dp))
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = SecondaryMint,
            lineHeight = 17.sp
        )
    }
}