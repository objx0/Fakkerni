package com.fakarni.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    language: String,
    onLanguageChanged: (String) -> Unit,
    notificationsEnabled: Boolean,
    requestNotificationPermission: () -> Unit,
    canScheduleExactly: Boolean,
    openExactAlarmSettings: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(22.dp)) {
                Text(stringResource(R.string.appearance), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.dark_mode_description), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f), modifier = Modifier.padding(top = 5.dp))
                HorizontalDivider(Modifier.padding(vertical = 14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.theme), Modifier.weight(1f))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        androidx.compose.material3.FilterChip(selected = themeMode == mode, onClick = { onThemeModeChanged(mode) }, label = { Text(stringResource(mode.labelResource())) })
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 14.dp))
            Text(stringResource(R.string.language), fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.FilterChip(selected = language == "ar", onClick = { onLanguageChanged("ar") }, label = { Text("العربية") })
                androidx.compose.material3.FilterChip(selected = language == "en", onClick = { onLanguageChanged("en") }, label = { Text("English") })
            }
            HorizontalDivider(Modifier.padding(vertical = 14.dp))
                Text(stringResource(R.string.notifications), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.notifications_description), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f), modifier = Modifier.padding(top = 5.dp))
                Text(
                    stringResource(if (notificationsEnabled) R.string.notifications_allowed else R.string.notifications_not_allowed),
                    color = if (notificationsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 10.dp),
                )
                if (!notificationsEnabled) {
                    Button(onClick = requestNotificationPermission, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Text(stringResource(R.string.enable_notifications))
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 14.dp))
                Text(
                    stringResource(if (canScheduleExactly) R.string.exact_alarm_allowed else R.string.exact_alarm_not_allowed),
                    color = if (canScheduleExactly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 10.dp),
                )
                if (!canScheduleExactly) {
                    Button(onClick = openExactAlarmSettings, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Text(stringResource(R.string.enable_exact_alarms))
                    }
                }
            }
        }
    }
}

private fun ThemeMode.labelResource(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}
