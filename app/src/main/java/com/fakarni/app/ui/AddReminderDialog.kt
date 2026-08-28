package com.fakarni.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.data.Recurrence
import com.fakarni.app.data.Reminder
import com.fakarni.app.data.ReminderCategory
import com.fakarni.app.ui.components.AppEmoji
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@Composable
fun AddReminderDialog(
    initial: Reminder? = null,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
) {
    var title by remember(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var note by remember(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var icon by remember(initial?.id) { mutableStateOf(initial?.icon ?: "✨") }
    var date by remember(initial?.id) { mutableLongStateOf(initial?.date ?: System.currentTimeMillis() + 3_600_000L) }
    var recurrence by remember(initial?.id) { mutableStateOf(initial?.recurrence ?: Recurrence.NONE) }
    var category by remember(initial?.id) { mutableStateOf(initial?.category ?: ReminderCategory.PERSONAL) }
    val context = LocalContext.current
    val defaultNote = stringResource(R.string.personal_reminder_default)
    val icons = listOf("✨", "🚗", "🎬", "💧", "💊", "🏠", "💳", "🛠️", "📅", "🛒")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(30.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(if (initial == null) R.string.new_reminder else R.string.edit_reminder)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(title, { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.reminder_title_hint)) }, singleLine = true, shape = RoundedCornerShape(18.dp))
                OutlinedTextField(note, { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.optional_note)) }, singleLine = true, shape = RoundedCornerShape(18.dp))
                Text(stringResource(R.string.icon))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    icons.forEach { item ->
                        FilterChip(
                            selected = icon == item,
                            onClick = { icon = item },
                            label = { AppEmoji(item, size = 24.dp) },
                            shape = RoundedCornerShape(16.dp),
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply { timeInMillis = date }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                date = Calendar.getInstance().apply {
                                    timeInMillis = date
                                    set(year, month, day)
                                }.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("${stringResource(R.string.choose_date)}: ${formatDate(date)}")
                }
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply { timeInMillis = date }
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                date = Calendar.getInstance().apply {
                                    timeInMillis = date
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true,
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("${stringResource(R.string.choose_time)}: ${formatTime(date)}")
                }
                Text(stringResource(R.string.repeat))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Recurrence.entries.forEach { item ->
                        FilterChip(selected = recurrence == item, onClick = { recurrence = item }, label = { Text(stringResource(item.labelResource())) }, shape = RoundedCornerShape(16.dp))
                    }
                }
                Text(stringResource(R.string.category))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReminderCategory.entries.forEach { item ->
                        FilterChip(selected = category == item, onClick = { category = item }, label = { Text(stringResource(item.labelResource())) }, shape = RoundedCornerShape(16.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    val anchorDay = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).dayOfMonth
                    onSave(
                        Reminder(
                            id = initial?.id ?: System.currentTimeMillis(),
                            title = title.trim(),
                            note = note.trim().ifBlank { defaultNote },
                            date = date,
                            icon = icon,
                            done = initial?.done ?: false,
                            recurrence = recurrence,
                            category = category,
                            recurrenceAnchorDay = anchorDay,
                        ),
                    )
                },
            ) { Text(stringResource(if (initial == null) R.string.save else R.string.update)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

private fun formatDate(time: Long): String = DateTimeFormatter
    .ofPattern("d MMMM yyyy", Locale("ar"))
    .format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()))

private fun formatTime(time: Long): String = DateTimeFormatter
    .ofPattern("HH:mm", Locale("ar"))
    .format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()))
