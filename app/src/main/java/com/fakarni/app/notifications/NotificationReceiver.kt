package com.fakarni.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.fakarni.app.FakarniApplication
import com.fakarni.app.MainActivity
import com.fakarni.app.R
import com.fakarni.app.data.Recurrence
import com.fakarni.app.data.Reminder
import com.fakarni.app.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val application = context.applicationContext as FakarniApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val id = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
                val reminder = application.reminderRepository.getById(id) ?: return@launch
                when (intent.action) {
                    ACTION_TRIGGER -> handleTrigger(context, application, reminder)
                    ACTION_SNOOZE_TRIGGER -> if (!reminder.done) showNotification(context, reminder)
                    ACTION_SNOOZE -> handleSnooze(context, application, reminder)
                    ACTION_COMPLETE -> handleComplete(context, application, reminder)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleTrigger(
        context: Context,
        application: FakarniApplication,
        reminder: Reminder,
    ) {
        if (reminder.done || (reminder.recurrence == Recurrence.NONE && reminder.lastNotifiedAt != null)) return
        showNotification(context, reminder)
        val now = System.currentTimeMillis()
        val next = RecurrenceCalculator.nextOccurrence(
            scheduledAt = reminder.date,
            recurrence = reminder.recurrence,
            anchorDay = reminder.anchorDay(),
            now = now,
        )
        val updated = reminder.copy(date = next ?: reminder.date, lastNotifiedAt = now)
        application.reminderRepository.save(updated)
        if (next != null) application.alarmScheduler.schedule(updated)
        WidgetUpdater.updateAll(context)
    }

    private fun handleSnooze(
        context: Context,
        application: FakarniApplication,
        reminder: Reminder,
    ) {
        if (reminder.done) return
        application.alarmScheduler.scheduleSnooze(
            reminder.id,
            System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1),
        )
        context.getSystemService(NotificationManager::class.java).cancel(notificationId(reminder.id))
    }

    private suspend fun handleComplete(
        context: Context,
        application: FakarniApplication,
        reminder: Reminder,
    ) {
        if (reminder.isHabit) {
            // A habit stays active; "done" from the notification logs one more step.
            application.reminderRepository.recordCompletion(reminder.id)
        } else {
            application.reminderRepository.save(reminder.copy(done = true))
            application.reminderRepository.recordCompletion(reminder.id)
            application.alarmScheduler.cancel(reminder.id)
        }
        context.getSystemService(NotificationManager::class.java).cancel(notificationId(reminder.id))
        WidgetUpdater.updateAll(context)
    }

    private fun showNotification(context: Context, reminder: Reminder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )

        val contentIntent = PendingIntent.getActivity(
            context,
            requestCode(reminder.id, ACTION_OPEN),
            Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN
                data = Uri.parse("fakarni://reminder/${reminder.id}/$ACTION_OPEN")
                putExtra(EXTRA_REMINDER_ID, reminder.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val snooze = actionPendingIntent(context, ACTION_SNOOZE, reminder.id)
        val complete = actionPendingIntent(context, ACTION_COMPLETE, reminder.id)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_fakkerni)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(reminder.title)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_menu_recent_history, context.getString(R.string.snooze_one_hour), snooze)
            .addAction(android.R.drawable.checkbox_on_background, context.getString(R.string.mark_complete), complete)
            .build()
        manager.notify(notificationId(reminder.id), notification)
    }

    private fun actionPendingIntent(context: Context, action: String, id: Long): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            this.action = action
            data = Uri.parse("fakarni://reminder/$id/$action")
            putExtra(EXTRA_REMINDER_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(id, action),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun requestCode(id: Long, action: String): Int =
        ((id xor (id ushr 32)).toInt() * 31) + action.hashCode()

    companion object {
        const val ACTION_TRIGGER = "com.fakarni.app.action.TRIGGER"
        const val ACTION_SNOOZE_TRIGGER = "com.fakarni.app.action.SNOOZE_TRIGGER"
        const val ACTION_SNOOZE = "com.fakarni.app.action.SNOOZE"
        const val ACTION_COMPLETE = "com.fakarni.app.action.COMPLETE"
        const val ACTION_OPEN = "com.fakarni.app.action.OPEN"
        const val EXTRA_REMINDER_ID = "reminder_id"
        private const val CHANNEL_ID = "fakarni_reminders"

        private fun notificationId(id: Long): Int = (id xor (id ushr 32)).toInt()
    }
}
