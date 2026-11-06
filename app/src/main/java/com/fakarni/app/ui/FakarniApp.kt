package com.fakarni.app.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fakarni.app.R
import com.fakarni.app.ui.components.FakIcons
import com.fakarni.app.data.Reminder
import com.fakarni.app.ui.components.GlassBackground
import com.fakarni.app.ui.components.glass
import com.fakarni.app.ui.screens.HabitsScreen
import com.fakarni.app.ui.screens.HomeScreen
import com.fakarni.app.ui.screens.ReminderListScreen
import com.fakarni.app.ui.screens.SettingsScreen
import com.fakarni.app.ui.theme.FakarniTheme
import com.fakarni.app.ui.theme.LocalGlass
import com.fakarni.app.ui.theme.ThemeMode

private const val TAB_TODAY = 0
private const val TAB_HABITS = 1
private const val TAB_ALL = 2
private const val TAB_SETTINGS = 3

@Composable
fun FakarniApp(
    viewModel: FakarniViewModel,
    notificationsEnabled: Boolean,
    showNotificationPermissionPrompt: Boolean,
    dismissNotificationPermissionPrompt: () -> Unit,
    requestNotificationPermission: () -> Unit,
    canScheduleExactly: Boolean,
    openExactAlarmSettings: () -> Unit,
    language: String,
    onLanguageChanged: (String) -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val completionEvents by viewModel.completionEvents.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var tabBackStack by rememberSaveable { mutableStateOf(listOf(0)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addAsHabit by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
    var pendingDelete by remember { mutableStateOf<Reminder?>(null) }
    var themeMode by rememberSaveable { mutableStateOf(ThemeMode.valueOf(preferences.getString("theme_mode", "SYSTEM") ?: "SYSTEM")) }
    val tabStateHolder = rememberSaveableStateHolder()
    val view = LocalView.current

    fun selectTab(tab: Int) {
        if (tab == selectedTab) return
        selectedTab = tab
        tabBackStack = tabBackStack.filterNot { it == tab } + tab
    }

    BackHandler(enabled = tabBackStack.size > 1) {
        tabBackStack = tabBackStack.dropLast(1)
        selectedTab = tabBackStack.last()
    }

    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDark = themeMode == ThemeMode.DARK || (themeMode == ThemeMode.SYSTEM && systemDark)
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }

    FakarniTheme(themeMode, language != "en") {
        GlassBackground {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val screenPadding = PaddingValues(top = statusBarPadding, bottom = navigationBarPadding + 96.dp)
            Crossfade(
                targetState = selectedTab,
                modifier = Modifier.fillMaxSize(),
                animationSpec = tween(180),
                label = "main-navigation",
            ) { tab ->
                tabStateHolder.SaveableStateProvider(tab) {
                    when (tab) {
                        TAB_TODAY -> HomeScreen(
                            reminders = reminders,
                            completionEvents = completionEvents,
                            contentPadding = screenPadding,
                            onAdd = { addAsHabit = false; showAddDialog = true },
                            onToggle = viewModel::toggleDone,
                            onEdit = { editingReminder = it },
                            onSnooze = viewModel::snooze,
                            onOpenSettings = { selectTab(TAB_SETTINGS) },
                            onOpenHabits = { selectTab(TAB_HABITS) },
                        )
                        TAB_HABITS -> HabitsScreen(
                            reminders = reminders,
                            completionEvents = completionEvents,
                            contentPadding = screenPadding,
                            onLog = viewModel::logHabit,
                            onUndo = viewModel::undoHabit,
                            onEdit = { editingReminder = it },
                            onAddHabit = { addAsHabit = true; showAddDialog = true },
                        )
                        TAB_ALL -> ReminderListScreen(reminders, completionEvents, screenPadding, viewModel::toggleDone) { editingReminder = it }
                        TAB_SETTINGS -> SettingsScreen(
                            onBack = {
                                tabBackStack = tabBackStack.dropLast(1).ifEmpty { listOf(TAB_TODAY) }
                                selectedTab = tabBackStack.last()
                            },
                            contentPadding = screenPadding,
                            themeMode = themeMode,
                            onThemeModeChanged = {
                                themeMode = it
                                preferences.edit().putString("theme_mode", it.name).apply()
                            },
                            language = language,
                            onLanguageChanged = onLanguageChanged,
                            notificationsEnabled = notificationsEnabled,
                            requestNotificationPermission = requestNotificationPermission,
                            canScheduleExactly = canScheduleExactly,
                            openExactAlarmSettings = openExactAlarmSettings,
                        )
                    }
                }
            }
            // Content fades out before it reaches the dock, so the bar never sits on busy text.
            val fade = LocalGlass.current.base
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(navigationBarPadding + 120.dp)
                    .background(Brush.verticalGradient(listOf(fade.copy(alpha = 0f), fade.copy(alpha = .78f), fade.copy(alpha = .94f)))),
            )
            FloatingDock(
                selectedTab = selectedTab,
                onTabSelected = ::selectTab,
                onAdd = { addAsHabit = selectedTab == TAB_HABITS; showAddDialog = true },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        if (showAddDialog) {
            AddReminderDialog(startAsHabit = addAsHabit, onDismiss = { showAddDialog = false }) {
                viewModel.save(it)
                showAddDialog = false
            }
        }
        editingReminder?.let { reminder ->
            AddReminderDialog(
                initial = reminder,
                onDismiss = { editingReminder = null },
                onDelete = {
                    editingReminder = null
                    pendingDelete = reminder
                },
            ) {
                viewModel.save(it)
                editingReminder = null
            }
        }
        pendingDelete?.let { reminder ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                icon = { Icon(FakIcons.Trash, contentDescription = null) },
                title = { Text(stringResource(R.string.delete_reminder_title)) },
                text = { Text(stringResource(R.string.delete_reminder_message, reminder.title)) },
                confirmButton = {
                    Button(
                        onClick = {
                            pendingDelete = null
                            viewModel.delete(reminder)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                    ) { Text(stringResource(R.string.delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) }
                },
                shape = RoundedCornerShape(32.dp),
                containerColor = MaterialTheme.colorScheme.surface,
            )
        }
        if (showNotificationPermissionPrompt) {
            AlertDialog(
                onDismissRequest = dismissNotificationPermissionPrompt,
                icon = { Icon(FakIcons.Bell, contentDescription = null) },
                title = { Text(stringResource(R.string.notification_permission_title)) },
                text = { Text(stringResource(R.string.notification_permission_message)) },
                confirmButton = {
                    Button(onClick = requestNotificationPermission) { Text(stringResource(R.string.enable_notifications)) }
                },
                dismissButton = {
                    TextButton(onClick = dismissNotificationPermissionPrompt) { Text(stringResource(R.string.not_now)) }
                },
                shape = RoundedCornerShape(32.dp),
                containerColor = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

/** Floating glass tab bar with the add action split off into its own ink orb. */
@Composable
private fun FloatingDock(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glass = LocalGlass.current
    Row(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp).height(66.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f).fillMaxHeight().dockGlass(RoundedCornerShape(33.dp)).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DockItem(stringResource(R.string.today), FakIcons.Today, selectedTab == TAB_TODAY) { onTabSelected(TAB_TODAY) }
            DockItem(stringResource(R.string.nav_habits), FakIcons.Flame, selectedTab == TAB_HABITS) { onTabSelected(TAB_HABITS) }
            DockItem(stringResource(R.string.category_all), FakIcons.List, selectedTab == TAB_ALL) { onTabSelected(TAB_ALL) }
        }
        Box(
            Modifier
                .size(66.dp)
                .shadow(16.dp, CircleShape, ambientColor = glass.shadow, spotColor = glass.shadow)
                .clip(CircleShape)
                .background(glass.ink, CircleShape)
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.add_reminder), onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(FakIcons.Plus, contentDescription = stringResource(R.string.add_reminder), tint = glass.onInk, modifier = Modifier.size(28.dp))
        }
    }
}

/**
 * Near-opaque glass for the dock. Without a real backdrop blur, a see-through
 * bar shows sharp text behind it, so this keeps only a hint of tint.
 */
@Composable
private fun Modifier.dockGlass(shape: RoundedCornerShape): Modifier {
    val glass = LocalGlass.current
    val surface = MaterialTheme.colorScheme.surface
    return this
        .shadow(18.dp, shape, ambientColor = glass.shadow.copy(alpha = if (glass.isDark) .5f else .22f), spotColor = glass.shadow.copy(alpha = if (glass.isDark) .5f else .22f))
        .clip(shape)
        .background(Brush.verticalGradient(listOf(surface.copy(alpha = .97f), surface.copy(alpha = .92f))), shape)
        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = .05f), Color.Transparent)), shape)
        .border(1.dp, Brush.verticalGradient(listOf(glass.highlight, glass.edge.copy(alpha = glass.edge.alpha * .4f))), shape)
}

@Composable
private fun RowScope.DockItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val lens by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .14f) else Color.Transparent,
        animationSpec = tween(180),
        label = "dock-lens",
    )
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(27.dp))
            .background(lens)
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = tint)
        Text(label, maxLines = 1, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}
