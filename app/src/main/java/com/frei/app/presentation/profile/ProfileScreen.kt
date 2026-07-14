package com.frei.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private val FreiPurple = Color(0xFF6C3FCF)
private val FreiPurpleDark = Color(0xFF4C2A9E)
private val FreiBg = Color(0xFFFAFAFC)
private val FreiCardBorder = Color(0xFFF0EEF6)
private val FreiInk = Color(0xFF1B1830)
private val FreiMuted = Color(0xFF8C89A3)
private val FreiDanger = Color(0xFFE0463D)

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPreviousTripsClick: () -> Unit,
    onCustomerSupportClick: () -> Unit,
    onAboutAppClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(FreiBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = FreiPurple)
        }
        return
    }

    val user = uiState.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FreiBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(FreiPurple, FreiPurpleDark)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 52.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(8.dp)
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(Color.White.copy(alpha = 0.18f), CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = user?.fullName?.ifBlank { "Your Name" } ?: "Your Name",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = user?.email.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        // Overlapping content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-34).dp)
                .padding(horizontal = 20.dp)
        ) {
            // Contact number card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .border(1.dp, FreiCardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFE4F7F1), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF14B8A6))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "CONTACT NUMBER",
                        style = MaterialTheme.typography.labelSmall,
                        color = FreiMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user?.contact?.ifBlank { "Not added yet" } ?: "Not added yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = FreiInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .border(1.dp, FreiCardBorder, RoundedCornerShape(18.dp))
            ) {
                ProfileMenuRow(
                    emoji = "\uD83D\uDDFA\uFE0F", // 🗺️
                    iconBg = Color(0xFFEFE9FB),
                    label = "Previous Trips",
                    onClick = onPreviousTripsClick
                )
                HorizontalDivider(color = FreiCardBorder)
                ProfileMenuRow(
                    emoji = "\uD83C\uDFA7", // 🎧
                    iconBg = Color(0xFFEFE9FB),
                    label = "Customer Support",
                    onClick = onCustomerSupportClick
                )
                HorizontalDivider(color = FreiCardBorder)
                ProfileMenuRow(
                    emoji = "\u2139\uFE0F", // ℹ️
                    iconBg = Color(0xFFEFE9FB),
                    label = "About App",
                    onClick = onAboutAppClick
                )
                HorizontalDivider(color = FreiCardBorder)
                ProfileMenuRow(
                    emoji = "\uD83D\uDEAA", // 🚪
                    iconBg = Color(0xFFFBEAEA),
                    label = "Sign Out",
                    labelColor = FreiDanger,
                    showChevron = false,
                    onClick = { viewModel.signOut(onSignedOut) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Edit profile outlined button
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, FreiPurple, RoundedCornerShape(14.dp))
                    .clickable(onClick = onEditProfileClick)
                    .padding(vertical = 15.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = FreiPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = FreiPurple
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ProfileMenuRow(
    emoji: String,
    iconBg: Color,
    label: String,
    onClick: () -> Unit,
    labelColor: Color = FreiInk,
    showChevron: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconBg, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.width(13.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = labelColor,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = FreiMuted)
        }
    }
}