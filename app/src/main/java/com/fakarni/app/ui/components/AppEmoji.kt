package com.fakarni.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * App-owned emoji rendering. Android's system emoji vary by device; these
 * colorful vector equivalents keep the icons consistent and closer to the
 * rounded, polished look users expect from iOS emoji.
 */
@Composable
fun AppEmoji(emoji: String, modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val icon = when (emoji) {
        "✨" -> Icons.Default.AutoAwesome
        "🚗" -> Icons.Default.DirectionsCar
        "🎬" -> Icons.Default.Movie
        "💧" -> Icons.Default.WaterDrop
        "💊" -> Icons.Default.Medication
        "🏠" -> Icons.Default.Home
        "💳" -> Icons.Default.CreditCard
        "🛠️", "🛠" -> Icons.Default.Build
        "📅" -> Icons.Default.CalendarMonth
        "🛒" -> Icons.Default.ShoppingCart
        "☀️", "☀" -> Icons.Default.WbSunny
        "🌙" -> Icons.Default.DarkMode
        else -> null
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = emojiColor(emoji),
                modifier = Modifier.size(size * 0.82f),
            )
        } else {
            androidx.compose.material3.Text(
                text = emoji,
                fontFamily = FontFamily.Default,
                fontSize = (size.value * 0.78f).sp,
                maxLines = 1,
            )
        }
    }
}

private fun emojiColor(emoji: String): Color = when (emoji) {
    "✨" -> Color(0xFFFFB300)
    "🚗" -> Color(0xFFEF5350)
    "🎬" -> Color(0xFF7E57C2)
    "💧" -> Color(0xFF29B6F6)
    "💊" -> Color(0xFFEC407A)
    "🏠" -> Color(0xFFFF7043)
    "💳" -> Color(0xFF42A5F5)
    "🛠️", "🛠" -> Color(0xFF78909C)
    "📅" -> Color(0xFF42A5F5)
    "🛒" -> Color(0xFF66BB6A)
    "☀️", "☀" -> Color(0xFFFFB300)
    "🌙" -> Color(0xFF5C6BC0)
    else -> Color.Unspecified
}
