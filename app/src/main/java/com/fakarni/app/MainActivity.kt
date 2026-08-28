package com.fakarni.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fakarni.app.ui.FakarniApp
import com.fakarni.app.ui.FakarniViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val container get() = application as FakarniApplication
    private var canScheduleExactly by mutableStateOf(false)
    private var notificationsEnabled by mutableStateOf(true)
    private var showNotificationPermissionPrompt by mutableStateOf(false)
    private var language by mutableStateOf("ar")
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        refreshNotificationStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = getSharedPreferences("settings", MODE_PRIVATE).getString("language", "ar") ?: "ar"
        refreshNotificationStatus()
        showNotificationPermissionPrompt = shouldIntroduceNotificationPermission()
        refreshExactAlarmStatus()
        setContent {
            val viewModel: FakarniViewModel = viewModel(factory = FakarniViewModel.Factory(container))
            FakarniApp(
                viewModel = viewModel,
                notificationsEnabled = notificationsEnabled,
                showNotificationPermissionPrompt = showNotificationPermissionPrompt,
                dismissNotificationPermissionPrompt = ::dismissNotificationPermissionPrompt,
                requestNotificationPermission = ::requestNotificationPermission,
                canScheduleExactly = canScheduleExactly,
                openExactAlarmSettings = ::openExactAlarmSettings,
                language = language,
                onLanguageChanged = ::changeLanguage,
            )
        }
    }

    private fun changeLanguage(value: String) {
        if (value == language) return
        getSharedPreferences("settings", MODE_PRIVATE).edit().putString("language", value).apply()
        recreate()
    }

    override fun attachBaseContext(newBase: Context) {
        val selected = newBase.getSharedPreferences("settings", MODE_PRIVATE).getString("language", "ar") ?: "ar"
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(Locale(selected))
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onResume() {
        super.onResume()
        refreshNotificationStatus()
        refreshExactAlarmStatus()
    }

    private fun shouldIntroduceNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationsEnabled &&
            !getPreferences(MODE_PRIVATE).getBoolean(NOTIFICATION_PROMPT_SEEN, false)

    private fun dismissNotificationPermissionPrompt() {
        markNotificationPromptSeen()
        showNotificationPermissionPrompt = false
    }

    private fun requestNotificationPermission() {
        markNotificationPromptSeen()
        showNotificationPermissionPrompt = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            (!getPreferences(MODE_PRIVATE).getBoolean(NOTIFICATION_PERMISSION_REQUESTED, false) ||
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))
        ) {
            getPreferences(MODE_PRIVATE).edit().putBoolean(NOTIFICATION_PERMISSION_REQUESTED, true).apply()
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (!notificationsEnabled) {
            openNotificationSettings()
        }
    }

    private fun refreshNotificationStatus() {
        notificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled() &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
    }

    private fun markNotificationPromptSeen() {
        getPreferences(MODE_PRIVATE).edit().putBoolean(NOTIFICATION_PROMPT_SEEN, true).apply()
    }

    private fun openNotificationSettings() {
        runCatching {
            startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            })
        }
    }

    private fun refreshExactAlarmStatus() {
        val wasAllowed = canScheduleExactly
        canScheduleExactly = container.alarmScheduler.canScheduleExactAlarms()
        if (!wasAllowed && canScheduleExactly) {
            lifecycleScope.launch(Dispatchers.IO) { container.alarmScheduler.rescheduleAll() }
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactly) {
            runCatching { startActivity(container.alarmScheduler.exactAlarmSettingsIntent()) }
        }
    }

    private companion object {
        const val NOTIFICATION_PROMPT_SEEN = "notification_prompt_seen"
        const val NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
    }
}
