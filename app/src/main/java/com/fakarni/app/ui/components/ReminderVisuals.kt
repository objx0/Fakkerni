package com.fakarni.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakarni.app.data.ReminderCategory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Gradient pair for a category; vivid enough to carry a white glyph at 4.5:1. */
fun categoryGradient(category: ReminderCategory): List<Color> = when (category) {
    ReminderCategory.PERSONAL -> listOf(Color(0xFF8E74FF), Color(0xFF4B2FE0))
    ReminderCategory.CAR -> listOf(Color(0xFFF2685E), Color(0xFFC62848))
    ReminderCategory.HEALTH -> listOf(Color(0xFFF06292), Color(0xFFB0185A))
    ReminderCategory.SUBSCRIPTIONS -> listOf(Color(0xFF9C6BFF), Color(0xFF6A2BD0))
    ReminderCategory.HOME -> listOf(Color(0xFFF59E3B), Color(0xFFC2410C))
    ReminderCategory.MONEY -> listOf(Color(0xFF26A69A), Color(0xFF0E7490))
}

/** Category-coloured tile with the reminder's glyph in white. */
@Composable
fun ReminderGlyph(icon: String, category: ReminderCategory, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    IconTile(categoryGradient(category), modifier.size(size), size) {
        val vector = appEmojiVector(icon)
        if (vector != null) {
            Icon(vector, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * .5f))
        } else if (icon.isNotBlank()) {
            Text(icon, fontFamily = FontFamily.Default, fontSize = (size.value * .45f).sp, maxLines = 1)
        } else {
            Icon(FakIcons.Sparkle, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * .5f))
        }
    }
}

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

fun formatClock(time: Long, locale: Locale): String =
    DateTimeFormatter.ofPattern("h:mm a", locale).format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()))

fun formatShortDate(time: Long, locale: Locale): String =
    DateTimeFormatter.ofPattern("EEE d MMM", locale).format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()))
