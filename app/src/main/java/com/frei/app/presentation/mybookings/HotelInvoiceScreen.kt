package com.frei.app.presentation.mybookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.FreiDetailTopBar
import com.frei.app.FreiGhostButton
import com.frei.app.FreiLineItem
import com.frei.app.FreiPaidBadge
import com.frei.app.FreiPrimaryButton
import com.frei.app.FreiSectionTitle
import com.frei.app.FreiTotalRow
import com.frei.app.ui.theme.*
import com.frei.app.ui.theme.FreiBg
import com.frei.app.ui.theme.FreiInkFaint
import com.frei.app.ui.theme.FreiInkSoft
import com.frei.app.ui.theme.FreiPurpleSoft
import com.frei.app.ui.theme.PrimaryPurple
import com.frei.app.ui.theme.TextDarkInk

/**
 * Populate from PaymentRepository / the hotelDetails Firestore doc once
 * payment status = SUCCESS. `discount` should be null when no promo applied.
 */
data class HotelInvoiceUiState(
    val invoiceNo: String,
    val issuedOn: String,
    val billedTo: String,
    val bookingId: String,
    val hotelName: String,
    val roomType: String,
    val guestCount: Int,
    val checkIn: String,
    val checkOut: String,
    val nights: Int,
    val roomChargePerNight: String,
    val roomChargesTotal: String,
    val taxes: String,
    val convenienceFee: String,
    val discount: String?,
    val total: String,
    val paymentMethod: String,
    val transactionId: String
)

@Composable
fun HotelInvoiceScreen(
    state: HotelInvoiceUiState,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FreiBg)
    ) {
        FreiDetailTopBar("Hotel Invoice", onBackClick, onShareClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(PrimaryPurple, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("F", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Frei", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextDarkInk)
                    }
                    FreiPaidBadge()
                }

                Spacer(Modifier.height(18.dp))

                InvoiceMetaGrid(
                    listOf(
                        "Invoice No." to state.invoiceNo,
                        "Issued On" to state.issuedOn,
                        "Billed To" to state.billedTo,
                        "Booking ID" to state.bookingId
                    )
                )

                Spacer(Modifier.height(16.dp))
                FreiSectionTitle("Stay Details")
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(FreiPurpleSoft, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Hotel, contentDescription = null, tint = PrimaryPurple)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(state.hotelName, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = TextDarkInk)
                        Text(
                            "${state.roomType} · ${state.guestCount} Guests · ${state.checkIn}–${state.checkOut} (${state.nights} nights)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FreiInkSoft
                        )
                    }
                }

                FreiSectionTitle("Bill Summary")
                FreiLineItem(
                    "Room Charges",
                    state.roomChargesTotal,
                    subCaption = "${state.roomChargePerNight} × ${state.nights} nights"
                )
                FreiLineItem("Taxes (GST)", state.taxes)
                FreiLineItem("Convenience Fee", state.convenienceFee)
                state.discount?.let {
                    FreiLineItem("Discount", "−$it", isDiscount = true)
                }

                Spacer(Modifier.height(10.dp))
                FreiTotalRow("Total Paid", state.total)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .background(FreiBg, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = FreiInkSoft, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Paid via ${state.paymentMethod} · Txn ID ${state.transactionId}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FreiInkSoft
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FreiGhostButton("Email", Icons.Outlined.Mail, onEmailClick)
                FreiPrimaryButton("Download PDF", Icons.Outlined.Download, onDownloadClick)
            }

            Text(
                "This is a computer-generated invoice issued by Frei Technologies.\nFor support, contact help@frei.app",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = FreiInkFaint,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}