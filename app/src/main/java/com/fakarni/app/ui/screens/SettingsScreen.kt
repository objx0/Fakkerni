package com.fakarni.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakarni.app.R
import com.fakarni.app.ui.components.FakIcons
import com.fakarni.app.ui.components.GlassIconButton
import com.fakarni.app.ui.components.IconTile
import com.fakarni.app.ui.components.glass
import com.fakarni.app.ui.theme.LocalGlass
import com.fakarni.app.ui.theme.ThemeMode

/** Settings in the concept's grouped layout: appearance, language, alerts, about. */
@Composable
fun SettingsScreen(
    onBack: (() -> Unit)? = null,
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
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "1.0"
    }
    var showLicenses by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(contentPadding).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (onBack != null) GlassIconButton(onClick = onBack, contentDescription = stringResource(R.string.back), size = 44.dp) {
                Icon(FakIcons.Back, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.settings_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        GroupLabel(stringResource(R.string.appearance))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeTile(stringResource(R.string.theme_system), ThemeMode.SYSTEM, themeMode == ThemeMode.SYSTEM, Modifier.weight(1f)) { onThemeModeChanged(ThemeMode.SYSTEM) }
            ThemeTile(stringResource(R.string.theme_light), ThemeMode.LIGHT, themeMode == ThemeMode.LIGHT, Modifier.weight(1f)) { onThemeModeChanged(ThemeMode.LIGHT) }
            ThemeTile(stringResource(R.string.theme_dark), ThemeMode.DARK, themeMode == ThemeMode.DARK, Modifier.weight(1f)) { onThemeModeChanged(ThemeMode.DARK) }
        }

        GroupLabel(stringResource(R.string.language))
        LanguageSegmented(language, onLanguageChanged)

        GroupLabel(stringResource(R.string.notifications))
        GroupCard {
            SettingRow(
                icon = FakIcons.Bell,
                gradient = listOf(Color(0xFFF06292), Color(0xFFB0185A)),
                title = stringResource(R.string.notifications),
                subtitle = stringResource(R.string.notifications_description),
                divider = true,
            ) { StatusOrAction(notificationsEnabled, requestNotificationPermission) }
            SettingRow(
                icon = FakIcons.Alarm,
                gradient = listOf(Color(0xFF8E74FF), Color(0xFF4B2FE0)),
                title = stringResource(R.string.exact_alarms_title),
                subtitle = stringResource(R.string.exact_alarms_subtitle),
                divider = false,
            ) { StatusOrAction(canScheduleExactly, openExactAlarmSettings) }
        }

        GroupLabel(stringResource(R.string.about))
        GroupCard {
            SettingRow(
                iconContent = {
                    Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp))) {
                        androidx.compose.foundation.Image(painterResource(R.drawable.ic_launcher_background), contentDescription = null, modifier = Modifier.size(34.dp))
                        androidx.compose.foundation.Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(34.dp))
                    }
                },
                title = stringResource(R.string.app_name),
                divider = true,
            ) { Text(stringResource(R.string.version_label, version), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            SettingRow(
                icon = FakIcons.Book,
                gradient = listOf(Color(0xFF26A69A), Color(0xFF0E7490)),
                title = stringResource(R.string.fonts_and_licenses),
                divider = false,
                onClick = { showLicenses = true },
            ) { Icon(FakIcons.Forward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
        }
        Spacer(Modifier.height(12.dp))
    }

    if (showLicenses) {
        val license = remember {
            runCatching { context.assets.open("licenses/ibmplexsansarabic-OFL.txt").bufferedReader().use { it.readText() } }.getOrDefault("")
        }
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = { Text(stringResource(R.string.fonts_and_licenses)) },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.fonts_used), style = MaterialTheme.typography.bodyMedium)
                    Text(license, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = { TextButton(onClick = { showLicenses = false }) { Text(stringResource(R.string.close)) } },
            shape = RoundedCornerShape(32.dp),
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(text, modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun GroupCard(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().glass(RoundedCornerShape(26.dp), strong = true, elevation = 12.dp)) { content() }
}

@Composable
private fun SettingRow(
    title: String,
    divider: Boolean,
    icon: ImageVector? = null,
    gradient: List<Color> = emptyList(),
    iconContent: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 60.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            iconContent != null -> iconContent()
            icon != null -> IconTile(gradient, size = 34.dp) { Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
    if (divider) HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .07f))
}

@Composable
private fun StatusOrAction(enabled: Boolean, onEnable: () -> Unit) {
    val glass = LocalGlass.current
    if (enabled) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(FakIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
            Text(stringResource(R.string.enabled_short), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
        }
    } else {
        Box(
            Modifier.height(44.dp).clip(RoundedCornerShape(22.dp)).clickable(role = Role.Button, onClick = onEnable),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.height(34.dp).clip(RoundedCornerShape(17.dp)).background(glass.ink).padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.enable_short), style = MaterialTheme.typography.labelLarge, color = glass.onInk)
            }
        }
    }
}

/** Theme choice with a tiny preview of the app in that theme. */
@Composable
private fun ThemeTile(label: String, mode: ThemeMode, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val glass = LocalGlass.current
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .then(
                if (selected) Modifier.shadow(10.dp, shape, ambientColor = primary.copy(alpha = .3f), spotColor = primary.copy(alpha = .3f))
                    .clip(shape).background(glass.fillStrongTop.copy(alpha = 1f), shape).border(2.dp, primary, shape)
                else Modifier.clip(shape).background(glass.fillTop, shape).border(1.dp, glass.edge, shape),
            )
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val preview = RoundedCornerShape(14.dp)
        Row(Modifier.fillMaxWidth().height(74.dp).clip(preview)) {
            when (mode) {
                ThemeMode.SYSTEM -> {
                    MiniScreen(Modifier.weight(1f), Brush.verticalGradient(listOf(Color(0xFFF4F3FB), Color(0xFFF4F3FB))), Color(0xFF1B1530), Color.White)
                    MiniScreen(Modifier.weight(1f), Brush.verticalGradient(listOf(Color(0xFF121214), Color(0xFF121214))), Color(0xFFF4F4F5), Color(0xFF26262B))
                }
                ThemeMode.LIGHT -> MiniScreen(Modifier.weight(1f), Brush.linearGradient(listOf(Color(0xFFE7DDFF), Color(0xFFFFE6D6))), Color(0xFF1B1530), Color.White.copy(alpha = .85f))
                ThemeMode.DARK -> MiniScreen(Modifier.weight(1f), Brush.linearGradient(listOf(Color(0xFF2A2A30), Color(0xFF121214))), Color(0xFFF4F4F5), Color.White.copy(alpha = .12f))
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold),
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MiniScreen(modifier: Modifier, background: Brush, ink: Color, card: Color) {
    Column(modifier.fillMaxSize().background(background).padding(horizontal = 6.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.fillMaxWidth(.7f).height(6.dp).clip(CircleShape).background(ink))
        Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(6.dp)).background(card))
        Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(6.dp)).background(card))
    }
}

@Composable
private fun LanguageSegmented(language: String, onSelect: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val glass = LocalGlass.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(colors.onSurface.copy(alpha = .06f)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf("ar" to "العربية", "en" to "English").forEach { (code, label) ->
            val isSelected = (if (language == "en") "en" else "ar") == code
            val shape = RoundedCornerShape(20.dp)
            Box(
                Modifier.weight(1f).height(44.dp)
                    .then(if (isSelected) Modifier.shadow(4.dp, shape, ambientColor = glass.shadow, spotColor = glass.shadow).clip(shape).background(glass.fillStrongTop.copy(alpha = 1f), shape) else Modifier.clip(shape))
                    .semantics { this.selected = isSelected }
                    .clickable(role = Role.RadioButton) { onSelect(code) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                    color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                )
            }
        }
    }
}
