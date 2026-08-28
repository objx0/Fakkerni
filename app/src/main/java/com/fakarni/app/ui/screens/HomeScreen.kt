package com.fakarni.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.data.Reminder
import com.fakarni.app.data.CompletionEvent
import com.fakarni.app.ui.components.ReminderCard
import com.fakarni.app.ui.components.AppEmoji
import com.fakarni.app.ui.components.WeeklyStatsCard
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private enum class HomeFilter { TODAY, ALL, DONE }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    reminders: List<Reminder>,
    contentPadding: PaddingValues,
    onAdd: () -> Unit,
    onToggle: (Reminder) -> Unit,
    onOpenSettings: () -> Unit,
    onSnooze: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    completionEvents: List<CompletionEvent>,
) {
    var filter by rememberSaveable { mutableStateOf(HomeFilter.TODAY) }
    var showWeeklyProgress by rememberSaveable { mutableStateOf(false) }
    var currentHour by remember { mutableIntStateOf(LocalTime.now().hour) }
    LaunchedEffect(Unit) {
        while (true) {
            currentHour = LocalTime.now().hour
            delay(60_000)
        }
    }
    val today = LocalDate.now()
    val visible = reminders.filter { reminder ->
        when (filter) {
            HomeFilter.TODAY -> !reminder.done && Instant.ofEpochMilli(reminder.date).atZone(ZoneId.systemDefault()).toLocalDate() == today
            HomeFilter.ALL -> !reminder.done
            HomeFilter.DONE -> reminder.done
        }
    }
    val doneCount = reminders.count { it.done }
    val progress = if (reminders.isEmpty()) 0f else doneCount.toFloat() / reminders.size

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding()).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = contentPadding.calculateBottomPadding() + 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall)
                    Text(
                        text = today.format(DateTimeFormatter.ofPattern("EEEE، d MMMM", Locale("ar"))),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Surface(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.open_settings), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(26.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterButton(stringResource(R.string.today), filter == HomeFilter.TODAY) { filter = HomeFilter.TODAY }
                FilterButton(stringResource(R.string.category_all), filter == HomeFilter.ALL) { filter = HomeFilter.ALL }
                FilterButton(stringResource(R.string.completed), filter == HomeFilter.DONE) { filter = HomeFilter.DONE }
            }
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.progress_count, doneCount, reminders.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Text("${(progress * 100).toInt()}٪", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            WeeklyProgressRow(
                onClick = { showWeeklyProgress = true },
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (filter) {
                    HomeFilter.TODAY -> stringResource(R.string.today_reminders)
                    HomeFilter.ALL -> stringResource(R.string.upcoming_reminders)
                    HomeFilter.DONE -> stringResource(R.string.completed_reminders)
                },
                style = MaterialTheme.typography.titleLarge,
            )
        }

        items(visible, key = { it.id }) {
            ReminderCard(
                reminder = it,
                onToggle = onToggle,
                onSnooze = onSnooze,
                onDelete = onDelete,
            )
        }

        if (visible.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surface).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppEmoji(
                        if (filter == HomeFilter.DONE) "✨" else if (currentHour in 6..17) "☀️" else "🌙",
                        size = 48.dp,
                    )
                    Text(stringResource(if (filter == HomeFilter.DONE) R.string.no_completed_reminders else R.string.day_is_clear), fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(if (filter == HomeFilter.DONE) R.string.complete_first_reminder else R.string.add_first_reminder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (filter != HomeFilter.DONE) {
                        TextButton(onClick = onAdd) { Text(stringResource(R.string.add_reminder)) }
                    }
                }
            }
        }
    }

    if (showWeeklyProgress) {
        ModalBottomSheet(onDismissRequest = { showWeeklyProgress = false }) {
            WeeklyStatsCard(
                reminders = reminders,
                events = completionEvents,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

@Composable
private fun WeeklyProgressRow(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.QueryStats,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.weekly_progress),
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun RowScope.FilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(48.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(label, maxLines = 1, fontWeight = FontWeight.Bold)
    }
}
