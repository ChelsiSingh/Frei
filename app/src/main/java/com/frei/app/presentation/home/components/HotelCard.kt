package com.frei.app.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.ui.theme.FreiBorder
import com.frei.app.ui.theme.FreiGold
import com.frei.app.ui.theme.FreiInk
import com.frei.app.ui.theme.FreiInkSoft
import com.frei.app.ui.theme.FreiPlaceholderTones
import com.frei.app.ui.theme.FreiTeal
import com.frei.app.ui.theme.FreiTealSoft

@Composable
fun HotelCard(
    hotel: RecommendedHotel,
    toneIndex: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.width(208.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, FreiBorder),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(132.dp)) {
                if (hotel.imageUrl != null) {
                    // AsyncImage(
                    //     model = hotel.imageUrl,
                    //     contentDescription = hotel.name,
                    //     modifier = Modifier.fillMaxSize(),
                    //     contentScale = ContentScale.Crop
                    // )
                } else {
                    val tone = FreiPlaceholderTones[toneIndex % FreiPlaceholderTones.size]
                    Box(
                        modifier = Modifier.fillMaxSize().background(tone),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hotel,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = hotel.city.substringBefore(","),
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                        )
                    }
                }

                // Rating pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xCC140C24),
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = FreiGold, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(hotel.rating, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Heart pill
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Favorite, null, tint = FreiInkSoft, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(13.dp)) {
                Text(hotel.name, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = FreiInk)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(hotel.city, fontSize = 11.5.sp, color = FreiInkSoft, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(9.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(hotel.price, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = FreiInk)
                        Text(" /night", fontSize = 10.5.sp, color = FreiInkSoft)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = FreiTealSoft) {
                        Text(
                            "-18%",
                            color = FreiTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}