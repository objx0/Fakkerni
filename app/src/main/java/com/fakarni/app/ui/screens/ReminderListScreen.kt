package com.fakarni.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.data.CompletionEvent
import com.fakarni.app.data.Recurrence
import com.fakarni.app.data.Reminder
import com.fakarni.app.data.ReminderCategory
import com.fakarni.app.ui.components.CompletionToggle
import com.fakarni.app.ui.components.FakIcons
import com.fakarni.app.ui.components.ReminderGlyph
import com.fakarni.app.ui.components.categoryGradient
import com.fakarni.app.ui.components.formatClock
import com.fakarni.app.ui.components.formatShortDate
import com.fakarni.app.ui.components.glass
import com.fakarni.app.ui.components.habitCountsOn
import com.fakarni.app.ui.components.toLocalDate
import com.fakarni.app.ui.labelResource
import com.fakarni.app.ui.theme.LocalGlass
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/** All reminders, grouped by when they are due, as in the Liquid Glass concept. */
@Composable
fun ReminderListScreen(
    reminders: List<Reminder>,
    completionEvents: List<CompletionEvent>,
    contentPadding: PaddingValues,
    onToggle: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val zone = ZoneId.systemDefault()
    val now = System.currentTimeMillis()
    val today = LocalDate.now(zone)
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<ReminderCategory?>(null) }
    var showCompleted by rememberSaveable { mutableStateOf(false) }
    val habitCounts = remember(completionEvents, today) { habitCountsOn(completionEvents, today, zone) }

    val visible = reminders.filter {
        (query.isBlank() || it.title.contains(query, ignoreCase = true) || it.note.contains(query, ignoreCase = true)) &&
            (selectedCategory == null || it.category == selectedCategory)
    }.sortedBy { it.date }
    val weekEnd = today.plusDays(7)
    val active = visible.filter { !it.done }
    val overdue = active.filter { !it.isHabit && it.date < now }
    val thisWeek = active.filter { (it.isHabit || it.date >= now) && it.date.toLocalDate(zone).isBefore(weekEnd) }
    val later = active.filter { !it.isHabit && it.date >= now && !it.date.toLocalDate(zone).isBefore(weekEnd) }
    val completed = visible.filter { it.done }.sortedByDescending { it.date }

    fun subtitle(reminder: Reminder): String = buildString {
        val day = reminder.date.toLocalDate(zone)
        append(if (day == today) "" else formatShortDate(reminder.date, locale) + " ")
        append(formatClock(reminder.date, locale))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding()),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = contentPadding.calculateBottomPadding() + 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "header") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.reminders_subtitle), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.my_reminders), style = MaterialTheme.typography.headlineLarge)
                }
                Box(Modifier.glass(RoundedCornerShape(50), elevation = 6.dp).padding(horizontal = 14.dp, vertical = 7.dp)) {
                    Text(stringResource(R.string.active_count, reminders.count { !it.done }), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        item(key = "search") { SearchField(query) { query = it } }
        item(key = "chips") {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CategoryChip(stringResource(R.string.category_all), null, selectedCategory == null) { selectedCategory = null }
                ReminderCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    CategoryChip(stringResource(category.labelResource()), categoryGradient(category).last(), isSelected) {
                        selectedCategory = if (isSelected) null else category
                    }
                }
            }
        }

        if (overdue.isNotEmpty()) item(key = "overdue") {
            Group(stringResource(R.string.overdue_group), overdue.size, MaterialTheme.colorScheme.error) {
                overdue.forEachIndexed { i, r ->
                    ListRow(r, subtitle(r) + recurrenceSuffix(r), true, false, i < overdue.lastIndex, onToggle, onEdit)
                }
            }
        }
        if (thisWeek.isNotEmpty()) item(key = "week") {
            Group(stringResource(R.string.this_week), thisWeek.size) {
                thisWeek.forEachIndexed { i, r ->
                    val count = habitCounts[r.id] ?: 0
                    val line = if (r.isHabit) {
                        stringResource(R.string.habit_short) + " · " + stringResource(R.string.habit_progress, count.coerceAtMost(r.dailyGoal), r.dailyGoal)
                    } else {
                        (if (r.date.toLocalDate(zone) == today) stringResource(R.string.today) + " " else "") + subtitle(r) + recurrenceSuffix(r)
                    }
                    ListRow(r, line, false, r.isHabit && count >= r.dailyGoal, i < thisWeek.lastIndex, onToggle, onEdit)
                }
            }
        }
        if (later.isNotEmpty()) item(key = "later") {
            Group(stringResource(R.string.later), later.size) {
                later.forEachIndexed { i, r -> ListRow(r, subtitle(r) + recurrenceSuffix(r), false, false, i < later.lastIndex, onToggle, onEdit) }
            }
        }
        if (completed.isNotEmpty()) item(key = "completed") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val shape = RoundedCornerShape(23.dp)
                val glass = LocalGlass.current
                Row(
                    Modifier.fillMaxWidth().height(48.dp).clip(shape).background(glass.fillBottom, shape).border(1.dp, glass.edge, shape)
                        .clickable(role = Role.Button) { showCompleted = !showCompleted }.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.completed_count, completed.size), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(FakIcons.Back, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp).rotate(if (showCompleted) 90f else -90f))
                }
                AnimatedVisibility(showCompleted) {
                    GroupCard {
                        completed.forEachIndexed { i, r -> ListRow(r, subtitle(r), false, true, i < completed.lastIndex, onToggle, onEdit) }
                    }
                }
            }
        }
        if (visible.isEmpty()) item(key = "empty") {
            Text(
                stringResource(R.string.no_matching_reminders),
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun recurrenceSuffix(reminder: Reminder): String =
    if (reminder.recurrence == Recurrence.NONE) "" else " · " + stringResource(reminder.recurrence.labelResource())

@Composable
private fun Group(title: String, count: Int, tint: Color = MaterialTheme.colorScheme.onSurface, rows: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = tint)
            Text("$count", style = MaterialTheme.typography.labelLarge, color = if (tint == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.onSurfaceVariant else tint)
        }
        GroupCard(rows)
    }
}

@Composable
private fun GroupCard(rows: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().glass(RoundedCornerShape(26.dp), elevation = 12.dp)) { rows() }
}

@Composable
private fun ListRow(
    reminder: Reminder,
    subtitle: String,
    overdue: Boolean,
    complete: Boolean,
    divider: Boolean,
    onToggle: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button) { onEdit(reminder) }.padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ReminderGlyph(reminder.icon, reminder.category, size = 40.dp)
        Column(Modifier.weight(1f)) {
            Text(
                reminder.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (complete) colors.onSurfaceVariant else colors.onSurface,
                textDecoration = if (complete && !reminder.isHabit) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (overdue) colors.error else colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        CompletionToggle(complete) { if (!(reminder.isHabit && complete)) onToggle(reminder) }
    }
    if (divider) HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = colors.onSurface.copy(alpha = .07f))
}

@Composable
private fun CategoryChip(label: String, dot: Color?, selected: Boolean, onClick: () -> Unit) {
    val glass = LocalGlass.current
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) glass.ink else glass.fillTop, shape)
            .border(1.dp, if (selected) Color.Transparent else glass.edge, shape)
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dot != null) Box(Modifier.size(8.dp).clip(CircleShape).background(if (selected) glass.onInk else dot))
        Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) glass.onInk else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val label = stringResource(R.string.search_reminders)
    Row(
        Modifier.fillMaxWidth().height(50.dp).glass(RoundedCornerShape(25.dp), elevation = 10.dp).padding(start = 18.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(FakIcons.Search, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(10.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier.weight(1f).semantics { contentDescription = label },
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) Text(label, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                    field()
                }
            },
        )
        if (query.isNotEmpty()) Box(
            Modifier.size(44.dp).clip(CircleShape).clickable(role = Role.Button) { onQueryChange("") },
            contentAlignment = Alignment.Center,
        ) { Icon(FakIcons.Close, contentDescription = stringResource(R.string.clear_search), modifier = Modifier.size(18.dp)) }
    }
}
