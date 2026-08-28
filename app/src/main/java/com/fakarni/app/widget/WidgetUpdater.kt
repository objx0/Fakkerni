package com.fakarni.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.fakarni.app.FakarniApplication
import com.fakarni.app.MainActivity
import com.fakarni.app.R
import com.fakarni.app.data.Reminder

object WidgetUpdater {
    suspend fun updateAll(context: Context) {
        val application = context.applicationContext as FakarniApplication
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, FakkerniWidget::class.java))
        update(context, manager, ids, application.reminderRepository.getActive())
    }

    fun update(context: Context, manager: AppWidgetManager, ids: IntArray, reminders: List<Reminder>) {
        val upcoming = reminders.sortedBy { it.date }.take(3)
        ids.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_fakkerni).apply {
                setTextViewText(R.id.widget_title, context.getString(R.string.app_name))
                setTextViewText(
                    R.id.widget_subtitle,
                    context.getString(if (upcoming.isEmpty()) R.string.widget_empty else R.string.widget_upcoming),
                )
                val lines = upcoming.joinToString("\n") { "${it.icon}  ${it.title}" }
                setTextViewText(R.id.widget_reminders, lines.ifBlank { context.getString(R.string.widget_add_prompt) })
                val openApp = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                setOnClickPendingIntent(R.id.widget_root, openApp)
            }
            manager.updateAppWidget(widgetId, views)
        }
    }
}
