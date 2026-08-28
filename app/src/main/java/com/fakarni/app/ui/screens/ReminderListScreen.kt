package com.fakarni.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.data.Reminder
import com.fakarni.app.data.ReminderCategory
import com.fakarni.app.ui.components.ReminderCard
import com.fakarni.app.ui.labelResource

@Composable
fun ReminderListScreen(
    reminders: List<Reminder>,
    contentPadding: PaddingValues,
    onToggle: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
    onSnooze: (Reminder) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<ReminderCategory?>(null) }
    var pendingDelete by remember { mutableStateOf<Reminder?>(null) }
    val visible = reminders.filter {
        (query.isBlank() || it.title.contains(query, ignoreCase = true) || it.note.contains(query, ignoreCase = true)) &&
            (selectedCategory == null || it.category == selectedCategory)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding()).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = contentPadding.calculateBottomPadding() + 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(stringResource(R.string.my_reminders), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.reminders_subtitle), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.search_reminders)) },
                shape = RoundedCornerShape(20.dp),
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedCategory == null, onClick = { selectedCategory = null }, label = { Text(stringResource(R.string.category_all)) }, shape = RoundedCornerShape(16.dp))
                ReminderCategory.entries.forEach { category ->
                    FilterChip(selected = selectedCategory == category, onClick = { selectedCategory = category }, label = { Text(stringResource(category.labelResource())) }, shape = RoundedCornerShape(16.dp))
                }
            }
        }
        items(visible, key = { it.id }) { reminder ->
            ReminderCard(reminder, onToggle, onEdit, { pendingDelete = it }, onSnooze)
        }
        if (visible.isEmpty()) item { Text(stringResource(R.string.no_matching_reminders), modifier = Modifier.fillMaxWidth().padding(24.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f)) }
    }

    pendingDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(R.string.delete_reminder_title)) },
            text = { Text(stringResource(R.string.delete_reminder_message, reminder.title)) },
            confirmButton = {
                Button(
                    onClick = { onDelete(reminder); pendingDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
