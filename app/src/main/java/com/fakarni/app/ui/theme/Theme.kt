package com.fakarni.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.fakarni.app.R

val ElectricBlue = Color(0xFF4F7CFF)
val DeepNavy = Color(0xFF102044)
val Night = Color(0xFF090B10)
val Charcoal = Color(0xFF171A21)
val Slate = Color(0xFF222630)
val SoftWhite = Color(0xFFF5F7FC)
val Muted = Color(0xFFAEB4C2)

// Backwards-compatible semantic aliases used by a few components.
val Mint = ElectricBlue
val Ink = Color(0xFF172033)
val Cream = Color(0xFFF4F6FA)

private val LightColors = lightColorScheme(
    primary = Color(0xFF315DDA), onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE5FF), onPrimaryContainer = Color(0xFF102A6A),
    background = Cream, surface = Color.White, surfaceVariant = Color(0xFFE8EBF2),
    onSurface = Ink, onSurfaceVariant = Color(0xFF5E6573), outline = Color(0xFFBEC4D0),
    secondary = Color(0xFF54658D),
)

private val DarkColors = darkColorScheme(
    primary = ElectricBlue, onPrimary = Color.White,
    primaryContainer = DeepNavy, onPrimaryContainer = Color(0xFFDDE6FF),
    background = Night, surface = Charcoal, surfaceVariant = Slate,
    onSurface = SoftWhite, onSurfaceVariant = Muted, outline = Color(0xFF454B59),
    secondary = Color(0xFF9DB6FF),
)

private val Beiruti = FontFamily(
    Font(R.font.beiruti_variable, FontWeight.Normal),
    Font(R.font.beiruti_variable, FontWeight.Medium),
    Font(R.font.beiruti_variable, FontWeight.SemiBold),
    Font(R.font.beiruti_variable, FontWeight.Bold),
    Font(R.font.beiruti_variable, FontWeight.ExtraBold),
    Font(R.font.beiruti_variable, FontWeight.Black),
)

private val FakarniTypography = Typography(
    displayLarge = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Black, fontSize = 52.sp, lineHeight = 60.sp),
    displayMedium = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Black, fontSize = 44.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Black, fontSize = 38.sp, lineHeight = 46.sp),
    headlineLarge = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp),
    bodyLarge = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Beiruti, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun FakarniTheme(themeMode: ThemeMode, isArabic: Boolean, content: @Composable () -> Unit) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, typography = FakarniTypography) {
        CompositionLocalProvider(
            LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr,
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            content = content,
        )
    }
}
