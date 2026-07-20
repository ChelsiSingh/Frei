package com.frei.app.presentation.mybookings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.FreiGhostButton
import com.frei.app.FreiPrimaryButton
import com.frei.app.FreiSectionTitle
import com.frei.app.ui.theme.*
import com.frei.app.ui.theme.FreiBg
import com.frei.app.ui.theme.FreiGold
import com.frei.app.ui.theme.FreiInkFaint
import com.frei.app.ui.theme.FreiInkSoft
import com.frei.app.ui.theme.FreiPlaceholderTones
import com.frei.app.ui.theme.FreiPurpleSoft
import com.frei.app.ui.theme.PrimaryPurple
import com.frei.app.ui.theme.SecondaryMint
import com.frei.app.ui.theme.TextDarkInk
import kotlin.math.abs

data class HotelAmenity(val icon: ImageVector, val label: String)

/** Populate from the hotelDetails Firestore doc + the hotel/property API used at booking time. */
data class HotelBookingDetailsUiState(
    val hotelName: String,
    val address: String,
    val heroImageUrl: String,
    val rating: Float,
    val reviewCount: Int,
    val statusLabel: String, // e.g. "Confirmed"
    val checkInDate: String,
    val checkInTime: String,
    val checkOutDate: String,
    val checkOutTime: String,
    val roomType: String,
    val roomImageUrl: String,
    val guestCount: Int,
    val bedType: String,
    val nights: Int,
    val amenities: List<HotelAmenity>,
    val contactName: String,
    val contactRole: String,
    val roomCharges: String,
    val taxesAndFees: String,
    val discount: String?,
    val total: String,
    val cancellationNote: String
)

/** Default amenity set — pass your own list from the hotel API response when available. */
fun defaultHotelAmenities(): List<HotelAmenity> = listOf(
    HotelAmenity(Icons.Outlined.Wifi, "Free Wi-Fi"),
    HotelAmenity(Icons.Outlined.Pool, "Pool"),
    HotelAmenity(Icons.Outlined.LocalParking, "Parking"),
    HotelAmenity(Icons.Outlined.Restaurant, "Breakfast")
)

/**
 * Self-contained placeholder image — no image-loading library required. Picks one of
 * [FreiPlaceholderTones] deterministically off [seed] so the same hotel/room always gets
 * the same tone. Swap the call sites below for `coil.compose.AsyncImage(model = url, ...)`
 * once you add `implementation("io.coil-kt:coil-compose:2.6.0")` to build.gradle.kts.
 */
@Composable
private fun PlaceholderImage(
    seed: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Hotel
) {
    val tone = remember(seed) {
        FreiPlaceholderTones[abs(seed.hashCode()) % FreiPlaceholderTones.size]
    }
    Box(
        modifier = modifier.background(tone),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun HotelBookingDetailsScreen(
    state: HotelBookingDetailsUiState,
    onBackClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onDirectionsClick: () -> Unit = {},
    onViewInvoiceClick: () -> Unit = {},
    onCallContactClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FreiBg)
            .verticalScroll(rememberScrollState())
    ) {
        HeroSection(state, onBackClick, onFavoriteClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-30).dp)
                .background(Color.White, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            RatingRow(state.rating, state.reviewCount)
            Spacer(Modifier.height(16.dp))
            StayStrip(state)
            Spacer(Modifier.height(20.dp))

            FreiSectionTitle("Room")
            RoomRow(state)
            Spacer(Modifier.height(4.dp))

            FreiSectionTitle("Amenities")
            AmenitiesGrid(state.amenities)
            Spacer(Modifier.height(4.dp))

            FreiSectionTitle("Property Contact")
            ContactRow(state, onCallContactClick)
            Spacer(Modifier.height(4.dp))

            FreiSectionTitle("Price Summary")
            PriceSummaryCard(state)
            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                FreiGhostButton("Directions", Icons.Outlined.Directions, onDirectionsClick)
                FreiPrimaryButton("View Invoice", Icons.AutoMirrored.Outlined.ReceiptLong, onViewInvoiceClick)
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Booking confirmed · ${state.cancellationNote}",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = FreiInkFaint,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HeroSection(state: HotelBookingDetailsUiState, onBackClick: () -> Unit, onFavoriteClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        PlaceholderImage(
            seed = state.hotelName,
            modifier = Modifier.fillMaxSize(),
            icon = Icons.Outlined.Hotel
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.4f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.92f), CircleShape)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(18.dp)
                )
            }
            Row(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(SecondaryMint, CircleShape)
                )
                Spacer(Modifier.width(5.dp))
                Text(state.statusLabel, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryPurple)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.92f), CircleShape)
                    .clickable(onClick = onFavoriteClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Save",
                    tint = PrimaryPurple,
                    modifier = Modifier
                        .size(18.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 34.dp)
        ) {
            Text(state.hotelName, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(3.dp))
                Text(state.address, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RatingRow(rating: Float, reviewCount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier
                .background(PrimaryPurple, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(3.dp))
            Text(rating.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(10.dp))
        Text("★★★★☆", color = FreiGold, fontSize = 13.sp)
        Spacer(Modifier.width(10.dp))
        Text("($reviewCount reviews)", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = FreiInkSoft)
    }
}

@Composable
private fun StayStrip(state: HotelBookingDetailsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FreiBg, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StayBlock("Check-in", state.checkInDate, state.checkInTime, Modifier.weight(1f))
        Box(modifier = Modifier.padding(horizontal = 6.dp)) {
            Icon(Icons.Outlined.NightsStay, contentDescription = null, tint = PrimaryPurple)
        }
        StayBlock("Check-out", state.checkOutDate, state.checkOutTime, Modifier.weight(1f))
    }
}

@Composable
private fun StayBlock(label: String, date: String, time: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label.uppercase(), fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, color = FreiInkFaint)
        Spacer(Modifier.height(4.dp))
        Text(date, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = TextDarkInk)
        Text(time, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = FreiInkSoft)
    }
}

@Composable
private fun RoomRow(state: HotelBookingDetailsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaceholderImage(
            seed = state.roomType,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            icon = Icons.Outlined.Hotel
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(state.roomType, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextDarkInk)
            Text(
                "${state.guestCount} Guests · ${state.bedType} · ${state.nights} Nights",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = FreiInkSoft
            )
        }
    }
}

@Composable
private fun AmenitiesGrid(amenities: List<HotelAmenity>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        amenities.forEach { amenity ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(FreiPurpleSoft, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(amenity.icon, contentDescription = null, tint = PrimaryPurple)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    amenity.label,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = FreiInkSoft,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ContactRow(state: HotelBookingDetailsUiState, onCallClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FreiBg, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Brush.linearGradient(listOf(SecondaryMint, PrimaryPurple)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                state.contactName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(state.contactName, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = TextDarkInk)
            Text(state.contactRole, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = FreiInkSoft)
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onCallClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Call,
                contentDescription = "Call property",
                tint = PrimaryPurple,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PriceSummaryCard(state: HotelBookingDetailsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        PriceRow("Room Charges", state.roomCharges)
        PriceRow("Taxes & Fees", state.taxesAndFees)
        state.discount?.let { PriceRow("Discount", "−$it", valueColor = SecondaryMint) }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total Paid", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextDarkInk)
            Text(state.total, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryPurple)
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, valueColor: Color = TextDarkInk) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = FreiInkSoft)
        Text(value, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
    }
}