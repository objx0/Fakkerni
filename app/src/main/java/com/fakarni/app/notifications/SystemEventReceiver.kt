package com.fakarni.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fakarni.app.FakarniApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val supported = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
        if (intent.action !in supported) return

        val pendingResult = goAsync()
        val application = context.applicationContext as FakarniApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                application.reminderRepository.initialize()
                application.alarmScheduler.rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
