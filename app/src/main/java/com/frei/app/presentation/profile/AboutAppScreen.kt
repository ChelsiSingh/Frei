package com.frei.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.ui.theme.FreiBorder
import com.frei.app.ui.theme.FreiInk
import com.frei.app.ui.theme.FreiInkSoft

private val Purple = Color(0xFF6C3FCF)
private val PurpleSoft = Color(0xFFEFE8FC)
private val Teal = Color(0xFF14B8A6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    onBackClick: () -> Unit = {},
    appVersion: String = "1.0.0",
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    onRateAppClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "About Frei",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FreiInk
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(38.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreiInk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAFAFC))
            )
        },
        containerColor = Color(0xFFFAFAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            AppLogoBadge()

            Spacer(Modifier.height(16.dp))

            Text(
                "Frei",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FreiInk
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "Plan trips, book flights & stays, all in one place",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FreiInkSoft,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PurpleSoft
            ) {
                Text(
                    "Version $appVersion",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            AboutSectionCard(title = "About this app") {
                Text(
                    "Frei is a travel companion built to make planning trips " +
                            "simpler — search and book flights and hotels, track " +
                            "your travel expenses, and keep every trip organized " +
                            "in one app.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FreiInkSoft,
                    lineHeight = 19.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            AboutSectionCard(title = "Support & legal") {
                Column {
                    AboutRow(
                        icon = Icons.Default.Email,
                        label = "Contact us",
                        onClick = onContactClick
                    )
                    AboutDivider()
                    AboutRow(
                        icon = Icons.Default.PrivacyTip,
                        label = "Privacy Policy",
                        trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                        onClick = onPrivacyPolicyClick
                    )
                    AboutDivider()
                    AboutRow(
                        icon = Icons.Default.Gavel,
                        label = "Terms of Service",
                        trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                        onClick = onTermsClick
                    )
                    AboutDivider()
                    AboutRow(
                        icon = Icons.Default.Star,
                        label = "Rate the app",
                        onClick = onRateAppClick
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Made with",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = FreiInkSoft
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE23F3F),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "by a solo developer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = FreiInkSoft
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun AppLogoBadge() {
    Box(
        modifier = Modifier
            .size(76.dp)
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(listOf(Purple, Teal)),
                RoundedCornerShape(22.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Flight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
private fun AboutSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title.uppercase(),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = FreiInkSoft,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, FreiBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun AboutRow(
    icon: ImageVector,
    label: String,
    trailingIcon: ImageVector = Icons.Default.ChevronRight,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(PurpleSoft, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Purple, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = FreiInk,
            modifier = Modifier.weight(1f)
        )
        Icon(
            trailingIcon,
            contentDescription = null,
            tint = FreiInkSoft,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun AboutDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 54.dp)
            .height(1.dp)
            .background(FreiBorder)
    )
}