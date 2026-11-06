package com.fakarni.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.data.Recurrence
import com.fakarni.app.data.Reminder
import com.fakarni.app.ui.labelResource
import com.fakarni.app.ui.theme.LocalGlass

@Composable
fun ReminderCard(
    reminder: Reminder,
    onToggle: (Reminder) -> Unit,
    onEdit: ((Reminder) -> Unit)? = null,
    onDelete: ((Reminder) -> Unit)? = null,
    onSnooze: ((Reminder) -> Unit)? = null,
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val colors = MaterialTheme.colorScheme
    val glass = LocalGlass.current
    val overdue = !reminder.done && reminder.date < System.currentTimeMillis()
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        onClick = onEdit?.let { action -> { action(reminder) } },
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReminderGlyph(reminder.icon, reminder.category, size = 50.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (reminder.done) colors.onSurfaceVariant else colors.onSurface,
                        textDecoration = if (reminder.done) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val category = stringResource(reminder.category.labelResource())
                    Text(
                        if (reminder.recurrence == Recurrence.NONE) category else "$category · ${stringResource(reminder.recurrence.labelResource())}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary,
                    )
                }
                CompletionToggle(reminder.done) { onToggle(reminder) }
            }
            if (reminder.note.isNotBlank()) Text(reminder.note, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (overdue) {
                    val shape = RoundedCornerShape(50)
                    Box(Modifier.height(26.dp).clip(shape).background(colors.error.copy(alpha = .14f), shape).padding(horizontal = 9.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.overdue), style = MaterialTheme.typography.labelSmall, color = colors.error)
                    }
                }
                Text(
                    "${formatShortDate(reminder.date, locale)} · ${formatClock(reminder.date, locale)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val capsule = RoundedCornerShape(50)
                Row(
                    Modifier.clip(capsule).background(glass.fillTop, capsule).border(1.dp, glass.edge, capsule),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!reminder.done) onSnooze?.let { action ->
                        IconButton(onClick = { action(reminder) }, modifier = Modifier.size(44.dp)) {
                            Icon(FakIcons.Snooze, stringResource(R.string.snooze_one_hour), modifier = Modifier.size(19.dp), tint = colors.primary)
                        }
                    }
                    onEdit?.let { action ->
                        IconButton(onClick = { action(reminder) }, modifier = Modifier.size(44.dp)) {
                            Icon(FakIcons.Edit, stringResource(R.string.edit), modifier = Modifier.size(18.dp), tint = colors.onSurfaceVariant)
                        }
                    }
                    onDelete?.let { action ->
                        IconButton(onClick = { action(reminder) }, modifier = Modifier.size(44.dp)) {
                            Icon(FakIcons.Trash, stringResource(R.string.delete), modifier = Modifier.size(19.dp), tint = colors.error)
                        }
                    }
                }
            }
        }
    }
}

/** Round completion control: hollow ring when open, filled ink disc with a check when done. */
@Composable
fun CompletionToggle(done: Boolean, onClick: () -> Unit) {
    val glass = LocalGlass.current
    val fill by animateColorAsState(if (done) glass.ink else Color.Transparent, tween(160), label = "completion-fill")
    val description = stringResource(if (done) R.string.content_description_incomplete else R.string.content_description_complete)
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .semantics { contentDescription = description }
            .clickable(role = Role.Checkbox, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(fill, CircleShape)
                .border(2.dp, if (done) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .55f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (done) Icon(FakIcons.Check, contentDescription = null, tint = glass.onInk, modifier = Modifier.size(18.dp))
        }
    }
}
