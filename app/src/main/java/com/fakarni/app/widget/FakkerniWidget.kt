package com.fakarni.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.fakarni.app.FakarniApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FakkerniWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pendingResult = goAsync()
        val application = context.applicationContext as FakarniApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                application.reminderRepository.initialize()
                WidgetUpdater.update(context, manager, ids, application.reminderRepository.getActive())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
