package com.fakarni.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.fakarni.app.data.Recurrence
import com.fakarni.app.data.Reminder
import com.fakarni.app.data.ReminderRepository

class AlarmScheduler(
    private val context: Context,
    private val repository: ReminderRepository,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    @RequiresApi(Build.VERSION_CODES.S)
    fun exactAlarmSettingsIntent(): Intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }

    fun schedule(reminder: Reminder) {
        if (reminder.done || (reminder.recurrence == Recurrence.NONE && reminder.lastNotifiedAt != null)) {
            cancel(reminder.id)
            return
        }
        val triggerAt = reminder.date.coerceAtLeast(System.currentTimeMillis() + MINIMUM_DELAY_MS)
        val pendingIntent = triggerPendingIntent(reminder.id)
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun scheduleSnooze(reminderId: Long, triggerAt: Long) {
        val intent = baseIntent(NotificationReceiver.ACTION_SNOOZE_TRIGGER, reminderId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(reminderId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(reminderId: Long) {
        alarmManager.cancel(triggerPendingIntent(reminderId))
        val snooze = PendingIntent.getBroadcast(
            context,
            requestCode(reminderId),
            baseIntent(NotificationReceiver.ACTION_SNOOZE_TRIGGER, reminderId),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        snooze?.let(alarmManager::cancel)
    }

    suspend fun rescheduleAll() {
        repository.getActive().forEach { reminder ->
            when {
                reminder.recurrence == Recurrence.NONE -> schedule(reminder)
                reminder.date > System.currentTimeMillis() -> schedule(reminder)
                else -> {
                    val next = RecurrenceCalculator.nextOccurrence(
                        scheduledAt = reminder.date,
                        recurrence = reminder.recurrence,
                        anchorDay = reminder.anchorDay(),
                    ) ?: return@forEach
                    val updated = reminder.copy(date = next)
                    repository.save(updated)
                    schedule(updated)
                }
            }
        }
    }

    private fun triggerPendingIntent(reminderId: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode(reminderId),
            baseIntent(NotificationReceiver.ACTION_TRIGGER, reminderId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun baseIntent(action: String, reminderId: Long) =
        Intent(context, NotificationReceiver::class.java).apply {
            this.action = action
            data = Uri.parse("fakarni://reminder/$reminderId/$action")
            putExtra(NotificationReceiver.EXTRA_REMINDER_ID, reminderId)
        }

    private fun requestCode(id: Long): Int = (id xor (id ushr 32)).toInt()

    private companion object {
        const val MINIMUM_DELAY_MS = 1_000L
    }
}
