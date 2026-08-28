package com.fakarni.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.data.Reminder
import com.fakarni.app.ui.labelResource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReminderCard(
    reminder: Reminder,
    onToggle: (Reminder) -> Unit,
    onEdit: ((Reminder) -> Unit)? = null,
    onDelete: ((Reminder) -> Unit)? = null,
    onSnooze: ((Reminder) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    var completionAnimating by remember(reminder.id) { mutableStateOf(false) }
    var dragOffset by remember(reminder.id) { mutableFloatStateOf(0f) }
    val completed = reminder.done || completionAnimating
    val checkScale = remember(reminder.id) { Animatable(if (reminder.done) 1f else 0f) }
    val washAlpha = remember(reminder.id) { Animatable(0f) }
    val washColor = MaterialTheme.colorScheme.primaryContainer
    val completionColor by animateColorAsState(
        targetValue = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(180),
        label = "completionColor",
    )

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(
            when {
                dragOffset > 0f -> MaterialTheme.colorScheme.primaryContainer
                dragOffset < -180f && onDelete != null -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            },
        ),
        contentAlignment = if (dragOffset >= 0f) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        if (dragOffset != 0f) {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (dragOffset > 0f) Icons.Default.Check else if (dragOffset < -180f && onDelete != null) Icons.Default.DeleteOutline else Icons.Default.Alarm,
                    contentDescription = null,
                    tint = if (dragOffset < -180f && onDelete != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (dragOffset > 0f) stringResource(R.string.mark_complete)
                    else if (dragOffset < -180f && onDelete != null) stringResource(R.string.delete)
                    else stringResource(R.string.snooze_one_hour),
                    color = if (dragOffset < -180f && onDelete != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Surface(
        modifier = Modifier.fillMaxWidth()
            .offset { IntOffset(dragOffset.roundToInt(), 0) }
            .pointerInput(reminder.id) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount -> dragOffset = (dragOffset + amount).coerceIn(-280f, 280f) },
                    onDragEnd = {
                        when {
                            dragOffset > 120f -> onToggle(reminder)
                            dragOffset < -180f && onDelete != null -> onDelete(reminder)
                            dragOffset < -80f -> onSnooze?.invoke(reminder)
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                )
            },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Box(
            Modifier.drawBehind {
                drawRect(color = washColor, alpha = washAlpha.value * 0.75f)
            },
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    AppEmoji(reminder.icon, size = 36.dp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        reminder.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (completed) TextDecoration.LineThrough else null,
                        color = if (completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        stringResource(reminder.category.labelResource()),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Surface(
                    onClick = {
                        if (reminder.done) {
                            onToggle(reminder)
                        } else if (!completionAnimating) {
                            completionAnimating = true
                            scope.launch {
                                checkScale.snapTo(0.2f)
                                val washJob = launch {
                                    washAlpha.animateTo(1f, tween(180))
                                    washAlpha.animateTo(0f, tween(320))
                                }
                                checkScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                )
                                washJob.join()
                                delay(150)
                                onToggle(reminder)
                            }
                        }
                    },
                    enabled = !completionAnimating,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = completionColor,
                    border = if (completed) null else BorderStroke(3.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (completed) {
                            Icon(
                                Icons.Default.Check,
                                stringResource(if (reminder.done) R.string.content_description_incomplete else R.string.content_description_complete),
                                modifier = Modifier
                                    .size(26.dp)
                                    .graphicsLayer {
                                        scaleX = checkScale.value
                                        scaleY = checkScale.value
                                        alpha = checkScale.value.coerceIn(0f, 1f)
                                    },
                            )
                        }
                    }
                }
            }

            if (reminder.note.isNotBlank()) {
                Text(reminder.note, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${stringResource(R.string.reminder_time_prefix)}  ${formatDate(reminder.date)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                onEdit?.let {
                    IconButton(onClick = { it(reminder) }, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.Edit, stringResource(R.string.edit), modifier = Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                onDelete?.let {
                    IconButton(onClick = { it(reminder) }, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.DeleteOutline, stringResource(R.string.delete), modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            }
        }
    }
    }
}

private fun formatDate(time: Long): String = DateTimeFormatter
    .ofPattern("EEE، d MMM · h:mm a", Locale("ar"))
    .format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()))
