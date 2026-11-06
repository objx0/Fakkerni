package com.fakarni.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.fakarni.app.R

// Ink and violet are the two anchors of the Liquid Glass palette.
val InkLight = Color(0xFF1B1530)
val InkMutedLight = Color(0xFF4A4466)
val VioletLight = Color(0xFF4B2FE0)
val VioletDark = Color(0xFFC4B5FD)

// Backwards-compatible aliases used by older code paths.
val ElectricBlue = VioletDark
val Mint = VioletDark
val Ink = InkLight
val Cream = Color(0xFFECEBFA)

private val LightColors = lightColorScheme(
    primary = VioletLight, onPrimary = Color.White,
    primaryContainer = Color(0xFFE4DEFF), onPrimaryContainer = Color(0xFF26178A),
    secondary = Color(0xFFC2185B), onSecondary = Color.White,
    tertiary = Color(0xFF0E7490), onTertiary = Color.White,
    background = Cream, onBackground = InkLight,
    surface = Color(0xFFFFFFFF), onSurface = InkLight,
    surfaceVariant = Color(0xFFF1EEFB), onSurfaceVariant = InkMutedLight,
    outline = Color(0xFFCBC5DD), error = Color(0xFFC0264B), onError = Color.White,
    inverseSurface = InkLight, inverseOnSurface = Color.White,
)

// Graphite dark theme: neutral greys, colour kept for icons and a soft lavender accent.
private val DarkColors = darkColorScheme(
    primary = VioletDark, onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF2E2A45), onPrimaryContainer = Color(0xFFE9E5FF),
    secondary = Color(0xFFFF9EC0), onSecondary = Color(0xFF3E0820),
    tertiary = Color(0xFF7AE3F0), onTertiary = Color(0xFF00363F),
    background = Color(0xFF121214), onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF1C1C1F), onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF26262B), onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF3F3F46), error = Color(0xFFFCA5A5), onError = Color(0xFF450A0A),
    inverseSurface = Color(0xFFF4F4F5), inverseOnSurface = Color(0xFF121214),
)

/** Material tokens for the translucent "liquid glass" surfaces. */
@Immutable
data class GlassColors(
    val isDark: Boolean,
    val base: Color,
    val blobs: List<Color>,
    val fillTop: Color,
    val fillBottom: Color,
    val fillStrongTop: Color,
    val fillStrongBottom: Color,
    val edge: Color,
    val highlight: Color,
    val shadow: Color,
    val ink: Color,
    val onInk: Color,
)

private val LightGlass = GlassColors(
    isDark = false,
    base = Cream,
    blobs = listOf(Color(0xFFFFB38A), Color(0xFFB8A6FF), Color(0xFF7FD8F0), Color(0xFFFF8FB8)),
    fillTop = Color.White.copy(alpha = .64f),
    fillBottom = Color.White.copy(alpha = .30f),
    fillStrongTop = Color.White.copy(alpha = .86f),
    fillStrongBottom = Color.White.copy(alpha = .62f),
    edge = Color.White.copy(alpha = .85f),
    highlight = Color.White,
    shadow = Color(0xFF46328C),
    ink = InkLight,
    onInk = Color.White,
)

private val DarkGlass = GlassColors(
    isDark = true,
    base = Color(0xFF121214),
    blobs = listOf(Color(0xFF3A3A44), Color(0xFF4B2FE0), Color(0xFF2A2A30), Color(0xFF3A3A44)),
    fillTop = Color.White.copy(alpha = .09f),
    fillBottom = Color.White.copy(alpha = .035f),
    fillStrongTop = Color(0xFF26262B).copy(alpha = .96f),
    fillStrongBottom = Color(0xFF1B1B1F).copy(alpha = .97f),
    edge = Color.White.copy(alpha = .12f),
    highlight = Color.White.copy(alpha = .22f),
    shadow = Color.Black,
    ink = Color(0xFFF4F4F5),
    onInk = Color(0xFF121214),
)

val LocalGlass = staticCompositionLocalOf { LightGlass }

/** Display face from the Liquid Glass concept: geometric, wide Arabic forms. */
private val ReadexPro = FontFamily(
    Font(R.font.readex_pro_medium, FontWeight.Medium),
    Font(R.font.readex_pro_semibold, FontWeight.SemiBold),
    Font(R.font.readex_pro_bold, FontWeight.Bold),
)

/** Text face from the concept: highly legible at small sizes in Arabic and Latin. */
private val PlexArabic = FontFamily(
    Font(R.font.ibm_plex_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_arabic_bold, FontWeight.Bold),
)

private val FakarniTypography = Typography(
    displayLarge = TextStyle(fontFamily = ReadexPro, fontWeight = FontWeight.Bold, fontSize = 52.sp, lineHeight = 62.sp),
    displayMedium = TextStyle(fontFamily = ReadexPro, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 54.sp),
    displaySmall = TextStyle(fontFamily = ReadexPro, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 46.sp),
    headlineLarge = TextStyle(fontFamily = ReadexPro, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = ReadexPro, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = ReadexPro, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = ReadexPro, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = PlexArabic, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = PlexArabic, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = PlexArabic, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = PlexArabic, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = PlexArabic, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = PlexArabic, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = PlexArabic, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = PlexArabic, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
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
            LocalGlass provides if (darkTheme) DarkGlass else LightGlass,
            LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr,
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            content = content,
        )
    }
}
