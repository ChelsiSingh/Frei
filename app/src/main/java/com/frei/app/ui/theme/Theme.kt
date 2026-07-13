package com.frei.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
val BorderGray = Color(0xFFEFEFF4) // Standardized 6-char hex color hex

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