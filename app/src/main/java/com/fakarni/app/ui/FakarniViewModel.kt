package com.fakarni.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fakarni.app.FakarniApplication
import com.fakarni.app.data.Reminder
import com.fakarni.app.widget.WidgetUpdater
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FakarniViewModel(private val application: FakarniApplication) : ViewModel() {
    val reminders: StateFlow<List<Reminder>> = application.reminderRepository.reminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val completionEvents = application.reminderRepository.completionEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            application.reminderRepository.initialize()
            application.alarmScheduler.rescheduleAll()
        }
    }

    fun save(reminder: Reminder) = viewModelScope.launch {
        application.alarmScheduler.cancel(reminder.id)
        val updated = reminder.copy(recurrenceAnchorDay = reminder.anchorDay(), lastNotifiedAt = null)
        application.reminderRepository.save(updated)
        application.alarmScheduler.schedule(updated)
        WidgetUpdater.updateAll(application)
    }

    fun toggleDone(reminder: Reminder) = viewModelScope.launch {
        val updated = reminder.copy(
            done = !reminder.done,
            lastNotifiedAt = if (reminder.done) null else reminder.lastNotifiedAt,
        )
        application.reminderRepository.save(updated)
        if (updated.done) application.reminderRepository.recordCompletion(reminder.id)
        if (updated.done) application.alarmScheduler.cancel(updated.id)
        else application.alarmScheduler.schedule(updated)
        WidgetUpdater.updateAll(application)
    }

    fun snooze(reminder: Reminder) = viewModelScope.launch {
        if (!reminder.done) application.alarmScheduler.scheduleSnooze(reminder.id, System.currentTimeMillis() + 3_600_000L)
    }

    fun delete(reminder: Reminder) = viewModelScope.launch {
        application.alarmScheduler.cancel(reminder.id)
        application.reminderRepository.delete(reminder)
        WidgetUpdater.updateAll(application)
    }

    class Factory(private val application: FakarniApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(FakarniViewModel::class.java))
            return FakarniViewModel(application) as T
        }
    }
}
