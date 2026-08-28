package com.fakarni.app.ui

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import com.fakarni.app.R
import com.fakarni.app.data.Reminder
import com.fakarni.app.ui.screens.HomeScreen
import com.fakarni.app.ui.screens.ReminderListScreen
import com.fakarni.app.ui.screens.SettingsScreen
import com.fakarni.app.ui.theme.FakarniTheme
import com.fakarni.app.ui.theme.ThemeMode

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
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
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
        WindowCompat.getInsetsController(window, view).apply {
        val isDark = themeMode == ThemeMode.DARK || (themeMode == ThemeMode.SYSTEM && (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES)
        isAppearanceLightStatusBars = !isDark
        isAppearanceLightNavigationBars = !isDark
        }
    }

    FakarniTheme(themeMode, language != "en") {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val screenPadding = PaddingValues(
                top = statusBarPadding,
                bottom = navigationBarPadding + 96.dp,
            )
            Crossfade(
                targetState = selectedTab,
                modifier = Modifier.fillMaxSize(),
                animationSpec = tween(180),
                label = "main-navigation",
            ) { tab ->
                tabStateHolder.SaveableStateProvider(tab) {
                    when (tab) {
                        0 -> HomeScreen(reminders, screenPadding, { showAddDialog = true }, viewModel::toggleDone, { selectTab(2) }, viewModel::snooze, viewModel::delete, completionEvents)
                        1 -> ReminderListScreen(reminders, screenPadding, viewModel::toggleDone, viewModel::delete, { editingReminder = it }, viewModel::snooze)
                        2 -> SettingsScreen(
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
            FloatingDock(
                selectedTab = selectedTab,
                onTabSelected = ::selectTab,
                onAdd = { showAddDialog = true },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        if (showAddDialog) {
            AddReminderDialog(onDismiss = { showAddDialog = false }) {
                viewModel.save(it)
                showAddDialog = false
            }
        }
        editingReminder?.let { reminder ->
            AddReminderDialog(initial = reminder, onDismiss = { editingReminder = null }) {
                viewModel.save(it)
                editingReminder = null
            }
        }
        if (showNotificationPermissionPrompt) {
            AlertDialog(
                onDismissRequest = dismissNotificationPermissionPrompt,
                icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
                title = { Text(stringResource(R.string.notification_permission_title)) },
                text = { Text(stringResource(R.string.notification_permission_message)) },
                confirmButton = {
                    Button(onClick = requestNotificationPermission) {
                        Text(stringResource(R.string.enable_notifications))
                    }
                },
                dismissButton = {
                    TextButton(onClick = dismissNotificationPermissionPrompt) {
                        Text(stringResource(R.string.not_now))
                    }
                },
                shape = RoundedCornerShape(28.dp),
            )
        }
    }
}

@Composable
private fun FloatingDock(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val destinations = listOf(
            Triple(R.string.nav_home, Icons.Default.Home, 0),
            Triple(R.string.nav_reminders, Icons.AutoMirrored.Filled.EventNote, 1),
            Triple(R.string.nav_settings, Icons.Default.Settings, 2),
        )
        Surface(
            modifier = Modifier.weight(1f).height(68.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .18f)),
        ) {
            Row(Modifier.fillMaxWidth().padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
                destinations.forEach { (label, icon, index) ->
                    DockItem(
                        label = stringResource(label),
                        icon = icon,
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                    )
                }
            }
        }
        Surface(
            onClick = onAdd,
            modifier = Modifier.size(68.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 12.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, stringResource(R.string.add), tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.DockItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val animatedWeight by animateFloatAsState(
        targetValue = if (selected) 1.35f else .825f,
        animationSpec = tween(220),
        label = "dock-item-width",
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .14f) else Color.Transparent,
        animationSpec = tween(180),
        label = "dock-item-indicator",
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .58f),
        animationSpec = tween(180),
        label = "dock-item-icon",
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.weight(animatedWeight).fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        color = indicatorColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = iconColor,
            )
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(120)) + expandHorizontally(tween(180)),
                exit = fadeOut(tween(80)) + shrinkHorizontally(tween(160)),
            ) {
                Row {
                    Spacer(Modifier.width(6.dp))
                    Text(label, maxLines = 1, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}
