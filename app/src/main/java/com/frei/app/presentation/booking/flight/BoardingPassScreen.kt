package com.frei.app.presentation.booking.flight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class BoardingPass(
    val airline: String,
    val flightNumber: String,
    val fromCode: String,
    val fromCity: String,
    val toCode: String,
    val toCity: String,
    val passengerName: String,
    val gate: String,
    val seat: String,
    val boardingTime: String,
    val departureTime: String,
    val terminal: String
)

@Composable
fun BoardingPassScreen(
    boardingPass: BoardingPass,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(24.dp)
        ) {
            BoardingPassCard(boardingPass)
        }
    }
}

@Composable
private fun BoardingPassCard(pass: BoardingPass) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
    ) {
        // Top section: airline + route
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = pass.airline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RouteEndpoint(code = pass.fromCode, city = pass.fromCity, alignEnd = false)

                Icon_PlaneDivider()

                RouteEndpoint(code = pass.toCode, city = pass.toCity, alignEnd = true)
            }
        }

        PerforatedDivider()

        // Bottom section: details grid
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(label = "PASSENGER", value = pass.passengerName)
                DetailItem(label = "FLIGHT", value = pass.flightNumber, alignEnd = true)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(label = "GATE", value = pass.gate)
                DetailItem(label = "SEAT", value = pass.seat)
                DetailItem(label = "TERMINAL", value = pass.terminal, alignEnd = true)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(label = "BOARDING", value = pass.boardingTime)
                DetailItem(label = "DEPARTS", value = pass.departureTime, alignEnd = true)
            }
        }

        PerforatedDivider()

        // Barcode section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Barcode()
        }
    }
}

@Composable
private fun RouteEndpoint(code: String, city: String, alignEnd: Boolean) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = city,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String, alignEnd: Boolean = false) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Icon_PlaneDivider() {
    Row(
        modifier = Modifier.width(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Divider(
            modifier = Modifier.width(30.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
    }
}

@Composable
private fun PerforatedDivider() {
    val dotColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = dotColor,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
        )
    }
}

@Composable
private fun Barcode() {
    val barColor = MaterialTheme.colorScheme.onSurface
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        val barCount = 40
        val gap = size.width / barCount
        for (i in 0 until barCount) {
            val barWidth = if (i % 3 == 0) gap * 0.6f else gap * 0.3f
            drawLine(
                color = barColor,
                start = Offset(i * gap, 0f),
                end = Offset(i * gap, size.height),
                strokeWidth = barWidth,
                blendMode = BlendMode.SrcOver
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardingPassScreenPreview() {
    BoardingPassScreen(
        boardingPass = BoardingPass(
            airline = "IndiGo",
            flightNumber = "6E 2341",
            fromCode = "JAI",
            fromCity = "Jaipur",
            toCode = "BOM",
            toCity = "Mumbai",
            passengerName = "Chelsi Sharma",
            gate = "12",
            seat = "14A",
            boardingTime = "09:30",
            departureTime = "10:05",
            terminal = "2"
        )
    )
}