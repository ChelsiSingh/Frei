package com.frei.app.presentation.newtrip

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.ui.theme.FreiTeal

private val Purple = Color(0xFF6C3FCF)
private val Ink = Color(0xFF1B1B23)
private val InkMuted = Color(0xFF6E6E7C)
private val Border = Color(0xFFECEAF3)
private val CardBg = Color(0xFFF6F5FA)
private val ConfettiBlue = Color(0xFF4A7FE8)
private val ConfettiOrange = Color(0xFFF59E0B)
private val ConfettiPink = Color(0xFFE85BA0)
private val ConfettiTeal = Color(0xFF14B8A6)

@Composable
fun TripCreatedScreen(
    tripName: String,
    tripId: String,
    dateRange: String,
    travelers: Int,
    onBackClick: () -> Unit = {},
    onAddBookingsClick: () -> Unit = {},
    onGoToTripClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(38.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ConfettiDot(ConfettiBlue, x = (-58).dp, y = (-38).dp, dotSize = 8.dp)
            ConfettiDot(ConfettiOrange, x = 62.dp, y = (-30).dp, dotSize = 7.dp)
            ConfettiDot(ConfettiPink, x = (-70).dp, y = 18.dp, dotSize = 6.dp)
            ConfettiDot(ConfettiTeal, x = 70.dp, y = 26.dp, dotSize = 8.dp)
            ConfettiDot(ConfettiOrange, x = (-20).dp, y = (-62).dp, dotSize = 5.dp)
            ConfettiDot(ConfettiBlue, x = 24.dp, y = (-64).dp, dotSize = 5.dp)

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        Brush.linearGradient(listOf(Purple, Color(0xFF8A5FE0))),
                        pinShape()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Trip Created!",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Your trip has been saved successfully.",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    tripName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "ID: $tripId",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkMuted
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = InkMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        dateRange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        tint = InkMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$travelers ${if (travelers == 1) "Traveler" else "Travelers"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                }
            }
        }

        Spacer(Modifier.height(230.dp))

        Button(
            onClick = onAddBookingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text("Add Bookings", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onGoToTripClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FreiTeal),
            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
        ) {
            Text("Go to Trip", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ConfettiDot(color: Color, x: Dp, y: Dp, dotSize: Dp) {
    Box(
        modifier = Modifier
            .padding(start = 100.dp + x, top = 100.dp + y)
            .size(dotSize)
            .background(color, CircleShape)
    )
}

private fun pinShape(): RoundedCornerShape = RoundedCornerShape(
    topStart = 48.dp,
    topEnd = 48.dp,
    bottomStart = 48.dp,
    bottomEnd = 4.dp
)