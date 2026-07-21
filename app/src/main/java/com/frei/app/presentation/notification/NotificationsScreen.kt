package com.frei.app.presentation.notification

import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.frei.app.data.model.AppNotification
import com.frei.app.data.model.NotificationType
import java.util.concurrent.TimeUnit

private val FreiPurple = Color(0xFF6C3CF0)
private val FreiPurpleBg = Color(0xFFEDE7FC)
private val FreiTeal = Color(0xFF14B8A6)
private val FreiTealBg = Color(0xFFE0F5F1)

private fun iconFor(type: NotificationType): Pair<ImageVector, Boolean> = when (type) {
    NotificationType.TRIP_START -> Icons.AutoMirrored.Filled.Send to true       // purple
    NotificationType.FLIGHT_REMINDER -> Icons.Filled.FlightTakeoff to false     // teal
    NotificationType.HOTEL_CHECKIN -> Icons.AutoMirrored.Filled.Login to true   // purple
    NotificationType.HOTEL_CHECKOUT -> Icons.AutoMirrored.Filled.Logout to false // teal
    NotificationType.GENERAL -> Icons.Filled.Notifications to true
}

private fun relativeTime(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        hours < 1 -> "now"
        hours < 24 -> "${hours}h"
        else -> "${days}d"
    }
}

private fun isToday(millis: Long): Boolean {
    val diff = System.currentTimeMillis() - millis
    return TimeUnit.MILLISECONDS.toHours(diff) < 24
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val (today, earlier) = remember(notifications) {
        notifications.partition { isToday(it.timestamp.toDate().time) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No notifications yet", color = Color.Gray)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (today.isNotEmpty()) {
                item { SectionHeader("TODAY") }
                items(today, key = { it.id }) { NotificationCard(it) }
            }
            if (earlier.isNotEmpty()) {
                item { SectionHeader("EARLIER") }
                items(earlier, key = { it.id }) { NotificationCard(it) }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Color.Gray,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun NotificationCard(notification: AppNotification) {
    val (icon, isPurple) = iconFor(notification.type)
    val bg = if (isPurple) FreiPurpleBg else FreiTealBg
    val tint = if (isPurple) FreiPurple else FreiTeal

    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(bg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(notification.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(
                        relativeTime(notification.timestamp.toDate().time),
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(notification.message, color = Color(0xFF8C89A3), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}