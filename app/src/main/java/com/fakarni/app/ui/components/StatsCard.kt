package com.fakarni.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.data.CompletionEvent
import com.fakarni.app.data.Reminder
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyStatsCard(
    reminders: List<Reminder>,
    events: List<CompletionEvent>,
    modifier: Modifier = Modifier,
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val dates = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val eventDates = events.map { Instant.ofEpochMilli(it.completedAt).atZone(zone).toLocalDate() }
    val counts = dates.map { date -> eventDates.count { it == date } }
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val healthIds = reminders.filter { it.category.name == "HEALTH" && it.recurrence.name != "NONE" }.map { it.id }.toSet()
    val healthDates = events.filter { it.reminderId in healthIds }.map { Instant.ofEpochMilli(it.completedAt).atZone(zone).toLocalDate() }.toSet()
    var streak = 0
    var cursor = if (today in healthDates) today else today.minusDays(1)
    while (cursor in healthDates) { streak++; cursor = cursor.minusDays(1) }

    GlossySurface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.weekly_progress), Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.habit_streak, streak), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            dates.forEachIndexed { index, date ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Column(Modifier.height(58.dp), verticalArrangement = Arrangement.Bottom) {
                        androidx.compose.foundation.layout.Box(
                            Modifier.width(18.dp).height((48f * counts[index] / maxCount).coerceAtLeast(5f).dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary),
                        )
                    }
                    Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(stringResource(R.string.weekly_completed_count, counts.sum()), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
