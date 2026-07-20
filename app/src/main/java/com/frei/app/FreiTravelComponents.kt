package com.frei.app


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.ui.theme.FreiBorder
import com.frei.app.ui.theme.FreiInkFaint
import com.frei.app.ui.theme.FreiInkSoft
import com.frei.app.ui.theme.FreiTealSoft
import com.frei.app.ui.theme.PrimaryPurple
import com.frei.app.ui.theme.SecondaryMint
import com.frei.app.ui.theme.TextDarkInk
import kotlin.random.Random

/** Top bar shared by boarding pass + invoice screens: back button, title, share action. */
@Composable
fun FreiDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FreiIconButton(icon = Icons.Outlined.ArrowBack, onClick = onBackClick)
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDarkInk
        )
        FreiIconButton(icon = Icons.Outlined.IosShare, onClick = onShareClick)
    }
}

@Composable
fun FreiIconButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        Icon(icon, contentDescription = null, tint = TextDarkInk)
    }
}

/** Small uppercase eyebrow label used above data values, e.g. "PASSENGER", "GATE". */
@Composable
fun FreiFieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 9.5.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.4.sp,
        color = FreiInkFaint
    )
}

@Composable
fun FreiFieldValue(text: String, accent: Boolean = false) {
    Text(
        text = text,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.ExtraBold,
        color = if (accent) PrimaryPurple else TextDarkInk
    )
}

/** A labelled field pair — label on top, value below. Used in grids across all four screens. */
@Composable
fun FreiField(label: String, value: String, accent: Boolean = false, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        FreiFieldLabel(label)
        Spacer(Modifier.height(4.dp))
        FreiFieldValue(value, accent)
    }
}

/** Section title used above grouped content, e.g. "Fare Summary", "Amenities". */
@Composable
fun FreiSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier.padding(bottom = 10.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp,
        color = FreiInkFaint
    )
}

/** A single row in a fare/bill breakdown: label (+ optional sub-caption) and value. */
@Composable
fun FreiLineItem(
    label: String,
    value: String,
    subCaption: String? = null,
    isDiscount: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FreiInkSoft)
            subCaption?.let {
                Text(it, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = FreiInkFaint)
            }
        }
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDiscount) SecondaryMint else TextDarkInk
        )
    }
}

/** Bold total row with a solid divider above it — the last line of any invoice. */
@Composable
fun FreiTotalRow(label: String, value: String) {
    Column {
        Divider(color = TextDarkInk, thickness = 1.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextDarkInk)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryPurple)
        }
    }
}

/** Teal "Paid" pill shown top-right of invoice cards. */
@Composable
fun FreiPaidBadge() {
    Row(
        modifier = Modifier
            .background(FreiTealSoft, RoundedCornerShape(20.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = SecondaryMint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            "Paid",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F766E)
        )
    }
}

/**
 * A horizontal dashed "tear line" with two circular notches cut into the edges —
 * the perforation between a boarding pass's main body and its stub.
 * Pass the same color as the surrounding screen/card background so the
 * notches read as true cutouts rather than filled dots.
 */
@Composable
fun PerforatedDivider(backgroundColor: Color, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
    ) {
        val y = size.height / 2f
        drawLine(
            color = FreiBorder,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 3f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )
        val notchRadius = 13.dp.toPx()
        drawCircle(color = backgroundColor, radius = notchRadius, center = Offset(0f, y))
        drawCircle(color = backgroundColor, radius = notchRadius, center = Offset(size.width, y))
    }
}

/**
 * Fake barcode made of random-height bars — swap for a real barcode renderer
 * (e.g. ZXing's Code128Writer) when wiring this up to a live PNR.
 */
@Composable
fun BarcodeBars(
    modifier: Modifier = Modifier,
    barCount: Int = 40,
    color: Color = TextDarkInk,
    seed: Long = 42L
) {
    val random = remember(seed) { Random(seed) }
    Canvas(modifier = modifier.fillMaxWidth()) {
        val barWidth = size.width / (barCount * 1.4f)
        val gap = barWidth * 0.4f
        var x = 0f
        repeat(barCount) {
            val h = size.height * (0.35f + random.nextFloat() * 0.65f)
            val w = if (random.nextFloat() > 0.75f) barWidth * 1.6f else barWidth
            drawRect(
                color = color,
                topLeft = Offset(x, size.height - h),
                size = Size(w, h)
            )
            x += w + gap
        }
    }
}

/** Filled purple CTA button used for the primary action in a two-button row (e.g. "Download PDF"). */
@Composable
fun RowScope.FreiPrimaryButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold)
    }
}

/** Outlined secondary button used alongside [FreiPrimaryButton] (e.g. "Add to Wallet"). */
@Composable
fun RowScope.FreiGhostButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDarkInk)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold)
    }
}

/**
 * Fake QR-style grid — swap for a real QR generator (e.g. ZXing's QRCodeWriter
 * rendered to a Bitmap) when wiring this up to a live PNR/booking reference.
 */
@Composable
fun FakeQrCode(modifier: Modifier = Modifier, cells: Int = 11, seed: Long = 7L) {
    val pattern = remember(seed) {
        val random = Random(seed)
        List(cells * cells) { random.nextInt(0, 100) < 45 }
    }
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.width / cells
            pattern.forEachIndexed { index, on ->
                if (on) {
                    val row = index / cells
                    val col = index % cells
                    drawRect(
                        color = TextDarkInk,
                        topLeft = Offset(col * cellSize, row * cellSize),
                        size = Size(cellSize * 0.94f, cellSize * 0.94f)
                    )
                }
            }
        }
    }
}