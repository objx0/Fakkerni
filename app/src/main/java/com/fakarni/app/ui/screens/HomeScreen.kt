package com.fakarni.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.data.CompletionEvent
import com.fakarni.app.data.Recurrence
import com.fakarni.app.data.Reminder
import com.fakarni.app.ui.components.CompletionToggle
import com.fakarni.app.ui.components.FakIcons
import com.fakarni.app.ui.components.GlassIconButton
import com.fakarni.app.ui.components.GlassSurface
import com.fakarni.app.ui.components.InkButton
import com.fakarni.app.ui.components.ReminderGlyph
import com.fakarni.app.ui.components.WeeklyStatsCard
import com.fakarni.app.ui.components.currentStreak
import com.fakarni.app.ui.components.formatClock
import com.fakarni.app.ui.components.formatShortDate
import com.fakarni.app.ui.components.glass
import com.fakarni.app.ui.components.habitCountsOn
import com.fakarni.app.ui.components.keptDays
import com.fakarni.app.ui.components.toLocalDate
import com.fakarni.app.ui.labelResource
import com.fakarni.app.ui.theme.LocalGlass
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

private const val WEEKS_BACK = 8
private const val WEEKS_AHEAD = 26

// Concept abbreviations, Monday-first to match java.time.DayOfWeek.value.
private val ARABIC_DAY_NAMES = listOf("اثن", "ثلا", "أرب", "خمي", "جمعة", "سبت", "أحد")

// Streak badge uses the concept's warm peach regardless of theme.
private val StreakInkLight = Color(0xFFC2410C)
private val StreakInkDark = Color(0xFFFDBA74)
private val StreakFill = Color(0xFFFFB38A)

/** One row of the day's timeline: everything it needs to draw itself. */
private data class TimelineEntry(
    val reminder: Reminder,
    val timeLabel: String,
    val complete: Boolean,
    val isNext: Boolean,
    val isOverdue: Boolean,
    val habitCount: Int,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    reminders: List<Reminder>,
    completionEvents: List<CompletionEvent>,
    contentPadding: PaddingValues,
    onAdd: () -> Unit,
    onToggle: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
    onSnooze: (Reminder) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHabits: () -> Unit,
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val zone = ZoneId.systemDefault()
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(30_000)
            value = System.currentTimeMillis()
        }
    }
    val today = now.toLocalDate(zone)
    var selectedEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now(zone).toEpochDay()) }
    var showWeeklyProgress by rememberSaveable { mutableStateOf(false) }
    val selected = LocalDate.ofEpochDay(selectedEpochDay)

    val habitCounts = remember(completionEvents, today) { habitCountsOn(completionEvents, today, zone) }
    val streak = remember(reminders, completionEvents, today) {
        currentStreak(keptDays(reminders.filter { it.isHabit }, completionEvents, zone), today)
    }
    fun Reminder.isComplete(): Boolean =
        done || (isHabit && (habitCounts[id] ?: 0) >= dailyGoal.coerceAtLeast(1))

    val dayItems = reminders.filter { it.date.toLocalDate(zone) == selected }.sortedBy { it.date }
    val overdue = if (selected == today) {
        reminders.filter { !it.done && !it.isHabit && it.date.toLocalDate(zone).isBefore(today) }.sortedBy { it.date }
    } else emptyList()
    val next = dayItems.firstOrNull { !it.isComplete() && it.date >= now }
    val doneCount = dayItems.count { it.isComplete() }
    val remaining = dayItems.size - doneCount

    val headline = when {
        dayItems.isEmpty() -> stringResource(if (selected == today) R.string.day_is_clear else R.string.day_clear_selected)
        remaining == 0 -> stringResource(R.string.all_done_today)
        else -> context.resources.getQuantityString(R.plurals.remaining_count, remaining, remaining)
    }
    val subline = when {
        next != null -> stringResource(R.string.next_up, next.title) + (relativeLabel(next.date, now)?.let { " · $it" } ?: "")
        dayItems.isNotEmpty() -> stringResource(R.string.progress_count, doneCount, dayItems.size)
        else -> stringResource(R.string.add_first_reminder)
    }

    val schedule = dayItems.map {
        TimelineEntry(
            reminder = it,
            timeLabel = formatClock(it.date, locale),
            complete = it.isComplete(),
            isNext = it.id == next?.id,
            isOverdue = !it.isComplete() && it.date < now,
            habitCount = habitCounts[it.id] ?: 0,
        )
    }
    val missed = overdue.map {
        TimelineEntry(
            reminder = it,
            timeLabel = formatShortDate(it.date, locale),
            complete = false,
            isNext = false,
            isOverdue = true,
            habitCount = 0,
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding()),
        contentPadding = PaddingValues(top = 16.dp, bottom = contentPadding.calculateBottomPadding() + 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "header") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(if (LocalTime.now().hour < 12) R.string.greeting_morning else R.string.greeting_evening),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        today.format(DateTimeFormatter.ofPattern(if (locale.language == "ar") "EEEE، d MMMM" else "EEEE, d MMMM", locale)),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }
                GlassIconButton(onClick = onOpenSettings, contentDescription = stringResource(R.string.open_settings)) {
                    Icon(FakIcons.Sliders, contentDescription = null)
                }
            }
        }

        item(key = "week") {
            WeekPager(today = today, selected = selected, locale = locale, onSelect = { selectedEpochDay = it.toEpochDay() })
        }

        item(key = "summary") {
            GlassSurface(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(30.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ProgressRing(done = doneCount, total = dayItems.size)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(headline, style = MaterialTheme.typography.titleMedium)
                        Text(subline, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    StreakBadge(streak) { showWeeklyProgress = true }
                }
            }
        }

        // The schedule only appears when the day has something on it; an empty
        // day is already announced by the summary card above.
        if (schedule.isNotEmpty()) {
            item(key = "schedule-title") {
                SectionHeader(stringResource(R.string.schedule)) {
                    Text(
                        stringResource(R.string.my_habits_link),
                        modifier = Modifier.clip(RoundedCornerShape(50)).clickable(role = Role.Button, onClick = onOpenHabits)
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            item(key = "schedule") {
                Timeline(schedule, now, onToggle, onEdit, onSnooze)
            }
        }

        if (missed.isNotEmpty()) {
            item(key = "missed-title") {
                SectionHeader(stringResource(R.string.overdue_section), color = MaterialTheme.colorScheme.error) {}
            }
            item(key = "missed") {
                Timeline(missed, now, onToggle, onEdit, onSnooze)
            }
        }

        if (schedule.isEmpty() && missed.isEmpty()) {
            item(key = "empty") {
                EmptySchedule(onAdd = onAdd, morning = LocalTime.now().hour in 6..17)
            }
        }
    }

    if (showWeeklyProgress) ModalBottomSheet(
        onDismissRequest = { showWeeklyProgress = false },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        WeeklyStatsCard(reminders, completionEvents, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp))
    }
}

@Composable
private fun SectionHeader(title: String, color: Color = MaterialTheme.colorScheme.onSurface, trailing: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f).padding(vertical = 8.dp), style = MaterialTheme.typography.headlineSmall, color = color)
        trailing()
    }
}

/** Seven equal day pills per page, swiping between weeks — the concept's week strip. */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun WeekPager(today: LocalDate, selected: LocalDate, locale: Locale, onSelect: (LocalDate) -> Unit) {
    val firstDay = if (locale.language == "ar") DayOfWeek.SATURDAY else WeekFields.of(locale).firstDayOfWeek
    val thisWeek = remember(today, firstDay) { today.with(TemporalAdjusters.previousOrSame(firstDay)) }
    val pagerState = rememberPagerState(initialPage = WEEKS_BACK) { WEEKS_BACK + WEEKS_AHEAD + 1 }
    val glass = LocalGlass.current
    HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 20.dp), pageSpacing = 20.dp) { page ->
        val weekStart = thisWeek.plusWeeks((page - WEEKS_BACK).toLong())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (0L until 7L).map { weekStart.plusDays(it) }.forEach { day ->
                val isSelected = day == selected
                val shape = RoundedCornerShape(22.dp)
                val base = Modifier.weight(1f).height(64.dp)
                Column(
                    modifier = (
                        if (isSelected) base.shadow(10.dp, shape, ambientColor = glass.shadow, spotColor = glass.shadow).clip(shape).background(glass.ink, shape)
                        else base.clip(shape).background(glass.fillBottom.copy(alpha = glass.fillBottom.alpha + .08f), shape).border(1.dp, glass.edge, shape)
                        )
                        .semantics { this.selected = isSelected }
                        .clickable(role = Role.Tab) { onSelect(day) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                ) {
                    val name = if (locale.language == "ar") ARABIC_DAY_NAMES[day.dayOfWeek.value - 1]
                    else day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
                    Text(name, style = MaterialTheme.typography.labelSmall, color = if (isSelected) glass.onInk.copy(alpha = .8f) else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    Text(
                        day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
                        color = when {
                            isSelected -> glass.onInk
                            day == today -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressRing(done: Int, total: Int) {
    val start = Color(0xFF7B5CFF)
    val end = Color(0xFFFF7A9C)
    val track = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f)
    val progress = if (total == 0) 0f else done.toFloat() / total
    Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2
            val arc = Size(size.width - stroke, size.height - stroke)
            drawArc(track, -90f, 360f, false, topLeft = Offset(inset, inset), size = arc, style = Stroke(stroke))
            if (progress > 0f) drawArc(
                Brush.linearGradient(listOf(start, end)),
                -90f, 360f * progress, false, topLeft = Offset(inset, inset), size = arc,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Text(if (total == 0) "0/0" else "$done/$total", style = MaterialTheme.typography.titleMedium.copy(fontFamily = MaterialTheme.typography.headlineSmall.fontFamily, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun StreakBadge(streak: Int, onClick: () -> Unit) {
    val dark = LocalGlass.current.isDark
    val ink = if (dark) StreakInkDark else StreakInkLight
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .clip(shape)
            .background(StreakFill.copy(alpha = if (dark) .18f else .35f), shape)
            .border(1.dp, LocalGlass.current.edge, shape)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.weekly_progress), onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(FakIcons.Flame, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
        Text(stringResource(R.string.streak_days, streak), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ink, maxLines = 1)
    }
}

@Composable
private fun EmptySchedule(onAdd: () -> Unit, morning: Boolean) {
    GlassSurface(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(26.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(if (morning) FakIcons.Sun else FakIcons.Moon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.add_first_reminder), style = MaterialTheme.typography.titleSmall)
            }
            InkButton(onClick = onAdd, height = 44.dp) {
                Icon(FakIcons.Plus, contentDescription = null, tint = LocalGlass.current.onInk, modifier = Modifier.size(16.dp))
                Text(stringResource(R.string.add), color = LocalGlass.current.onInk, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Time rail on one side, cards on the other; the rail runs unbroken between rows. */
@Composable
private fun Timeline(
    entries: List<TimelineEntry>,
    now: Long,
    onToggle: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
    onSnooze: (Reminder) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        entries.forEachIndexed { index, entry ->
            TimelineRow(entry, isLast = index == entries.lastIndex, now = now, onToggle = onToggle, onEdit = onEdit, onSnooze = onSnooze)
        }
    }
}

@Composable
private fun TimelineRow(
    entry: TimelineEntry,
    isLast: Boolean,
    now: Long,
    onToggle: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
    onSnooze: (Reminder) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val reminder = entry.reminder
    val timeColor = when {
        entry.isOverdue -> colors.error
        entry.isNext -> colors.primary
        else -> colors.onSurfaceVariant
    }
    val railIdle = colors.onSurface.copy(alpha = .15f)
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(Modifier.width(56.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                entry.timeLabel,
                modifier = Modifier.padding(top = 14.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (entry.isNext) FontWeight.Bold else FontWeight.SemiBold),
                color = timeColor,
                maxLines = 2,
            )
            if (!isLast) Box(
                Modifier.padding(top = 6.dp).width(2.dp).weight(1f).clip(CircleShape).background(
                    if (entry.isNext) Brush.verticalGradient(listOf(colors.primary, railIdle)) else Brush.verticalGradient(listOf(railIdle, railIdle)),
                ),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 10.dp)) {
            TimelineCard(entry, now, onToggle, onEdit, onSnooze)
        }
    }
}

@Composable
private fun ColumnScope.TimelineCard(
    entry: TimelineEntry,
    now: Long,
    onToggle: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
    onSnooze: (Reminder) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LocalGlass.current
    val reminder = entry.reminder
    val shape = RoundedCornerShape(if (entry.isNext) 26.dp else 24.dp)
    val surface = when {
        entry.complete -> Modifier.clip(shape).background(glass.fillBottom, shape).border(1.dp, glass.edge.copy(alpha = glass.edge.alpha * .7f), shape)
        entry.isNext -> Modifier.glass(shape, strong = true, elevation = 20.dp)
        else -> Modifier.glass(shape, elevation = 10.dp)
    }
    Column(
        Modifier.fillMaxWidth().then(surface).clickable(role = Role.Button) { onEdit(reminder) }
            .padding(horizontal = 14.dp, vertical = if (entry.isNext) 14.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReminderGlyph(reminder.icon, reminder.category, size = 40.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (entry.complete) colors.onSurfaceVariant else colors.onSurface,
                    textDecoration = if (entry.complete) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (reminder.isHabit) {
                        stringResource(R.string.habit_progress, entry.habitCount.coerceAtMost(reminder.dailyGoal), reminder.dailyGoal) +
                            " · " + stringResource(R.string.habit_short)
                    } else secondaryLine(reminder),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            CompletionToggle(entry.complete) { if (!entry.complete || !reminder.isHabit) onToggle(reminder) }
        }
        if ((entry.isNext || entry.isOverdue) && !entry.complete) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val label = if (entry.isOverdue) stringResource(R.string.overdue) else relativeLabel(reminder.date, now)
                if (label != null) Pill(label, if (entry.isOverdue) colors.error else colors.primary)
                SnoozePill { onSnooze(reminder) }
            }
        }
    }
}

@Composable
private fun secondaryLine(reminder: Reminder): String {
    val category = stringResource(reminder.category.labelResource())
    return when {
        reminder.note.isNotBlank() -> reminder.note
        reminder.recurrence != Recurrence.NONE -> "$category · ${stringResource(reminder.recurrence.labelResource())}"
        else -> category
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    val shape = RoundedCornerShape(50)
    Box(Modifier.height(30.dp).clip(shape).background(color.copy(alpha = .14f), shape).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

@Composable
private fun SnoozePill(onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val glass = LocalGlass.current
    Row(
        Modifier.height(30.dp).clip(shape).background(glass.fillStrongTop, shape).border(1.dp, glass.edge, shape)
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(FakIcons.Clock, contentDescription = null, modifier = Modifier.size(14.dp))
        Text(stringResource(R.string.snooze_hour_action), style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
private fun relativeLabel(time: Long, now: Long): String? {
    val minutes = ((time - now) / 60_000L).toInt()
    return when {
        minutes < 0 -> null
        minutes < 60 -> stringResource(R.string.in_minutes, minutes.coerceAtLeast(1))
        minutes < 24 * 60 -> stringResource(R.string.in_hours_minutes, minutes / 60, minutes % 60)
        else -> null
    }
}
