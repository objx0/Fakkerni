package com.fakarni.app

import android.app.Application
import com.fakarni.app.data.FakarniDatabase
import com.fakarni.app.data.ReminderRepository
import com.fakarni.app.notifications.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FakarniApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { FakarniDatabase.getInstance(this) }
    val reminderRepository by lazy { ReminderRepository(this, database.reminderDao()) }
    val alarmScheduler by lazy { AlarmScheduler(this, reminderRepository) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            reminderRepository.initialize()
            alarmScheduler.rescheduleAll()
        }
    }
}
