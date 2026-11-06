package com.fakarni.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.data.CompletionEvent
import com.fakarni.app.data.Reminder
import com.fakarni.app.ui.components.FakIcons
import com.fakarni.app.ui.components.GlassSurface
import com.fakarni.app.ui.components.IconTile
import com.fakarni.app.ui.components.InkButton
import com.fakarni.app.ui.components.appEmojiVector
import com.fakarni.app.ui.components.categoryGradient
import com.fakarni.app.ui.components.currentStreak
import com.fakarni.app.ui.components.glass
import com.fakarni.app.ui.components.habitCountsOn
import com.fakarni.app.ui.components.habitDayScores
import com.fakarni.app.ui.components.keptDays
import com.fakarni.app.ui.components.longestStreak
import com.fakarni.app.ui.components.toLocalDate
import com.fakarni.app.ui.theme.LocalGlass
import kotlinx.coroutines.delay
import java.time.ZoneId

private const val HEATMAP_WEEKS = 5

@Composable
fun HabitsScreen(
    reminders: List<Reminder>,
    completionEvents: List<CompletionEvent>,
    contentPadding: PaddingValues,
    onLog: (Reminder) -> Unit,
    onUndo: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
    onAddHabit: () -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(60_000)
            value = System.currentTimeMillis()
        }
    }
    val today = now.toLocalDate(zone)
    val habits = remember(reminders) { reminders.filter { it.isHabit }.sortedBy { it.title } }
    val counts = remember(completionEvents, today) { habitCountsOn(completionEvents, today, zone) }
    val scores = remember(habits, completionEvents) { habitDayScores(habits, completionEvents, zone) }
    val kept = remember(habits, completionEvents) { keptDays(habits, completionEvents, zone) }
    val streak = currentStreak(kept, today)
    val best = longestStreak(kept)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding()),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "header") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.habits_subtitle), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.my_habits), style = MaterialTheme.typography.headlineLarge)
                }
                Row(
                    Modifier.height(44.dp).glass(RoundedCornerShape(22.dp), elevation = 8.dp)
                        .clickable(role = Role.Button, onClick = onAddHabit).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(FakIcons.Plus, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.habit_short), style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        item(key = "streak") {
            GlassSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.current_streak), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("$streak", style = MaterialTheme.typography.displaySmall)
                                Text(
                                    stringResource(R.string.days_in_a_row),
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (best > streak) Text(stringResource(R.string.longest_streak, best), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconTile(listOf(Color(0xFFFFB38A), Color(0xFFF0506E)), size = 52.dp) {
                            Icon(FakIcons.Flame, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                    Heatmap(scores, today)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.last_five_weeks), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.less), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        listOf(.08f, .3f, .55f, .85f).forEach { alpha ->
                            Box(Modifier.padding(horizontal = 2.dp).size(10.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha)))
                        }
                        Text(stringResource(R.string.more), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item(key = "today-title") {
            Text(stringResource(R.string.today), style = MaterialTheme.typography.headlineSmall)
        }

        if (habits.isEmpty()) {
            item(key = "empty") {
                GlassSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp)) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(FakIcons.Flame, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        Text(stringResource(R.string.no_habits_title), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.no_habits_message), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        InkButton(onClick = onAddHabit, height = 48.dp) {
                            Text(stringResource(R.string.add_habit), color = LocalGlass.current.onInk, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        } else {
            items(habits.chunked(2), key = { row -> row.first().id }) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { habit ->
                        HabitCard(
                            habit = habit,
                            count = counts[habit.id] ?: 0,
                            modifier = Modifier.weight(1f),
                            onLog = { onLog(habit) },
                            onUndo = { onUndo(habit) },
                            onEdit = { onEdit(habit) },
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            item(key = "hint") {
                Text(
                    stringResource(R.string.habit_undo_hint),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Heatmap(scores: Map<java.time.LocalDate, Float>, today: java.time.LocalDate) {
    val primary = MaterialTheme.colorScheme.primary
    val ink = MaterialTheme.colorScheme.onSurface
    val days = (HEATMAP_WEEKS * 7 - 1 downTo 0).map { today.minusDays(it.toLong()) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        days.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { day ->
                    val score = scores[day] ?: 0f
                    val alpha = when {
                        score <= 0f -> .08f
                        score < .4f -> .3f
                        score < .8f -> .55f
                        else -> .85f
                    }
                    val shape = RoundedCornerShape(9.dp)
                    Box(
                        Modifier.weight(1f).height(30.dp).clip(shape).background(primary.copy(alpha = alpha), shape),
                    ) {
                        if (day == today) Canvas(Modifier.fillMaxSize()) {
                            drawRoundRect(ink, style = Stroke(2.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(9.dp.toPx()))
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HabitCard(
    habit: Reminder,
    count: Int,
    modifier: Modifier,
    onLog: () -> Unit,
    onUndo: () -> Unit,
    onEdit: () -> Unit,
) {
    val goal = habit.dailyGoal.coerceAtLeast(1)
    val complete = count >= goal
    val accent = categoryGradient(habit.category).last()
    val glass = LocalGlass.current
    val progressLabel = stringResource(R.string.habit_progress, count.coerceAtMost(goal), goal)
    GlassSurface(modifier, shape = RoundedCornerShape(28.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "${habit.title}، $progressLabel" }
                .combinedClickable(role = Role.Button, onClick = onEdit, onLongClick = onUndo)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 6.dp.toPx()
                        val inset = stroke / 2
                        val arc = Size(size.width - stroke, size.height - stroke)
                        drawArc(accent.copy(alpha = .16f), -90f, 360f, false, topLeft = Offset(inset, inset), size = arc, style = Stroke(stroke))
                        if (count > 0) drawArc(
                            accent, -90f, 360f * (count.coerceAtMost(goal).toFloat() / goal), false,
                            topLeft = Offset(inset, inset), size = arc, style = Stroke(stroke, cap = StrokeCap.Round),
                        )
                    }
                    Icon(
                        if (complete) FakIcons.Check else appEmojiVector(habit.icon) ?: FakIcons.Sparkle,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                if (complete) {
                    Box(
                        Modifier.height(30.dp).clip(RoundedCornerShape(15.dp)).background(accent.copy(alpha = .15f)).padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text(stringResource(R.string.mark_complete), style = MaterialTheme.typography.labelMedium, color = accent) }
                } else {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(glass.ink, CircleShape)
                            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.log_habit), onClick = onLog),
                        contentAlignment = Alignment.Center,
                    ) { Icon(FakIcons.Plus, contentDescription = stringResource(R.string.log_habit), tint = glass.onInk, modifier = Modifier.size(20.dp)) }
                }
            }
            Column {
                Text(habit.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(progressLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
