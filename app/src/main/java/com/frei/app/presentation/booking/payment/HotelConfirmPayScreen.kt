package com.frei.app.presentation.booking.payment

import android.app.Activity
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.data.model.hotel.Hotel
import com.frei.app.presentation.booking.flight.FreiBackground
import com.frei.app.presentation.booking.flight.FreiInk
import com.frei.app.presentation.booking.flight.FreiPrimary
import com.frei.app.presentation.booking.flight.FreiSubtext

private val CardBorder = Color(0xFFF0EEF6)
private val ChipBg = Color(0xFFF7F6FB)
private val ChipLabel = Color(0xFF9A97AE)
private val MethodBorder = Color(0xFFE4E1EE)
private val MethodSelectedBg = Color(0xFFF5F2FC)
private val DiscountGreen = Color(0xFF1EA672)

public enum class PaymentMethod(val label: String, val razorpayValue: String) {
    WALLET("Wallet", "wallet"),
    CARD("Card", "card"),
    NET_BANKING("Net Banking", "netbanking")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelConfirmPayScreen(
    viewModel: HotelConfirmPayViewModel,
    onBackClick: () -> Unit,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current as Activity
    var selectedMethod by remember { mutableStateOf(PaymentMethod.WALLET) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm & Pay", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(8.dp).size(38.dp).background(Color.White, RoundedCornerShape(12.dp))
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreiInk) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FreiBackground)
            )
        },
        containerColor = FreiBackground
    ) { innerPadding ->
        when (val state = uiState) {
            HotelPaymentUiState.Loading -> Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                CircularProgressIndicator(color = FreiPrimary)
            }

            is HotelPaymentUiState.Failed -> Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Close, contentDescription = null,
                        tint = Color(0xFFD9534F),
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50)).background(Color(0xFFFCEEEE)).padding(10.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text("Payment Failed", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        state.message, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FreiSubtext,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(containerColor = FreiPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.TextButton(onClick = onBackClick) {
                        Text("Go Back", color = FreiSubtext, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            HotelPaymentUiState.Success -> Column(
                Modifier.fillMaxSize().padding(innerPadding), Arrangement.Center, Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DiscountGreen, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text("Booking Confirmed", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
                Spacer(Modifier.height(20.dp))
                Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = FreiPrimary)) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            is HotelPaymentUiState.Ready, HotelPaymentUiState.Processing -> {
                val ready = state as? HotelPaymentUiState.Ready
                Column(Modifier.fillMaxSize().padding(innerPadding)) {
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)
                    ) {
                        Spacer(Modifier.height(6.dp))
                        ready?.let {
                            StaySummaryCard(
                                hotel = it.hotel,
                                guestName = viewModel.guestName,
                                nights = viewModel.nights,
                                checkIn = viewModel.checkInDate,
                                checkOut = viewModel.checkOutDate,
                                roomType = viewModel.roomType
                            )
                            Spacer(Modifier.height(12.dp))
                            FareSummaryCard(viewModel.nights, it.roomCost, it.taxesAndCharges, it.totalPrice)
                            Spacer(Modifier.height(16.dp))
                            Text("Payment Method", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
                            Spacer(Modifier.height(8.dp))
                            PaymentMethodRow(PaymentMethod.WALLET, Icons.Default.AccountBalanceWallet, selectedMethod) { selectedMethod = it }
                            Spacer(Modifier.height(8.dp))
                            PaymentMethodRow(PaymentMethod.CARD, Icons.Default.CreditCard, selectedMethod) { selectedMethod = it }
                            Spacer(Modifier.height(8.dp))
                            PaymentMethodRow(PaymentMethod.NET_BANKING, Icons.Default.AccountBalance, selectedMethod) { selectedMethod = it }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Column(Modifier.fillMaxWidth().background(Color.White)) {
                        Divider(color = CardBorder)
                        Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = FreiSubtext, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Secured by 256-bit encryption", fontSize = 10.sp, color = FreiSubtext, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(9.dp))
                            ready?.let {
                                Button(
                                    onClick = {
                                        viewModel.createOrder { orderId, keyId, amountPaise ->
                                            viewModel.paymentManager.startCheckout(
                                                activity = activity,
                                                keyId = keyId,
                                                orderId = orderId,
                                                amountPaise = amountPaise,
                                                name = "Frei",
                                                description = "Hotel booking",
                                                prefillEmail = viewModel.guestEmail,
                                                prefillContact = viewModel.guestPhone,
                                                preferredMethod = selectedMethod.razorpayValue
                                            )
                                        }
                                    },
                                    enabled = state !is HotelPaymentUiState.Processing,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FreiPrimary),
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    if (state is HotelPaymentUiState.Processing) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text("Pay \u20B9${it.totalPrice.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaySummaryCard(
    hotel: Hotel,
    guestName: String,
    nights: Int,
    checkIn: String,
    checkOut: String,
    roomType: String
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp)).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFD9CBF2), Color(0xFFB79AE0))))
            )
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(hotel.name, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk, maxLines = 1)
                Text(
                    "$roomType \u00B7 1 Room, $nights ${if (nights == 1) "Night" else "Nights"}",
                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = FreiSubtext
                )
            }
        }
        Spacer(Modifier.height(11.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoChip(Modifier.weight(1f), "CHECK-IN", checkIn.ifBlank { "—" })
            InfoChip(Modifier.weight(1f), "CHECK-OUT", checkOut.ifBlank { "—" })
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoChip(Modifier.weight(1f), "GUEST", guestName.ifBlank { "—" })
            InfoChip(Modifier.weight(1f), "ROOM", "1 \u00D7 $roomType")
        }
    }
}

@Composable
private fun InfoChip(modifier: Modifier, label: String, value: String) {
    Column(modifier.clip(RoundedCornerShape(10.dp)).background(ChipBg).padding(horizontal = 9.dp, vertical = 7.dp)) {
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ChipLabel)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FreiInk, maxLines = 1)
    }
}

@Composable
private fun FareSummaryCard(nights: Int, roomCost: Double, taxes: Double, total: Double) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp)).padding(14.dp)
    ) {
        Text("Fare Summary", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
        Spacer(Modifier.height(9.dp))
        FareLine("1 Room, $nights ${if (nights == 1) "Night" else "Nights"}", roomCost)
        FareLine("Taxes & charges", taxes)
        Divider(color = CardBorder, modifier = Modifier.padding(vertical = 7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = FreiInk)
            Text("\u20B9${total.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = FreiPrimary)
        }
    }
}

@Composable
private fun FareLine(label: String, amount: Double) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FreiSubtext)
        Text("\u20B9${amount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FreiInk)
    }
}

@Composable
private fun PaymentMethodRow(
    method: PaymentMethod,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit
) {
    val isSelected = method == selected
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(if (isSelected) MethodSelectedBg else Color.White)
            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) FreiPrimary else MethodBorder, RoundedCornerShape(13.dp))
            .clickable { onSelect(method) }
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) FreiPrimary else FreiSubtext, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(11.dp))
        Text(method.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FreiInk, modifier = Modifier.weight(1f))
        if (isSelected) {
            Box(Modifier.size(18.dp).clip(RoundedCornerShape(50)).background(FreiPrimary), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
        }
    }
}