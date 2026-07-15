package com.frei.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.frei.app.R

val PrimaryPurple = Color(0xFF6C3FCF)
val SecondaryMint = Color(0xFF14B8A6)
val BackgroundInkLight = Color(0xFFFAFAFC)
val TextDarkInk = Color(0xFF1B1830)
val TextMuted = Color(0xFF8C89A3)
val BorderGray = Color(0xFFEFEFF4)

val FreiBg = Color(0xFFF6F4FC)
val FreiInkSoft = Color(0xFF77738A)
val FreiInkFaint = Color(0xFFA9A5BC)
val FreiPurpleSoft = Color(0xFFEFE8FC)
val FreiTealSoft = Color(0xFFE2F8F5)
val FreiBorder = Color(0xFFEEEAF8)
val FreiGold = Color(0xFFF5A524)

val FreiGradPurple = Brush.linearGradient(
    colors = listOf(Color(0xFF8257E5), Color(0xFF6C3FCF), Color(0xFF5931B0))
)

val FreiPurple = Color(0xFF8257E5)

val FreiPlaceholderTones = listOf(
    Brush.linearGradient(listOf(Color(0xFF8257E5), Color(0xFF6C3FCF), Color(0xFF4E2A9E))),
    Brush.linearGradient(listOf(Color(0xFF1FCDBB), Color(0xFF14B8A6), Color(0xFF0E8577))),
    Brush.linearGradient(listOf(Color(0xFFF5B94A), Color(0xFFF5A524), Color(0xFFC97F0E))),
    Brush.linearGradient(listOf(Color(0xFFF0699C), Color(0xFFE23F6B), Color(0xFFB22B50))),
)

// 2. Typography Definitions
val ManropeFontFamily = FontFamily(
    Font(R.font.manrope, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

// 3. Mapping Tokens cleanly into Material 3 Color Slots
private val FreiColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryMint,
    background = BackgroundInkLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDarkInk,
    onSurface = TextDarkInk,
    outlineVariant = BorderGray
)

@Composable
fun FreiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FreiColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}