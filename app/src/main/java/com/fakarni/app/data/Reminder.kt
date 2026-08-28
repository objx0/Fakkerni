package com.fakarni.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId

enum class Recurrence {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
}

enum class ReminderCategory {
    PERSONAL,
    CAR,
    HEALTH,
    SUBSCRIPTIONS,
    HOME,
    MONEY,
}

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey val id: Long,
    val title: String,
    val note: String,
    val date: Long,
    val icon: String,
    val done: Boolean = false,
    val recurrence: Recurrence = Recurrence.NONE,
    val category: ReminderCategory = ReminderCategory.PERSONAL,
    val recurrenceAnchorDay: Int? = null,
    val lastNotifiedAt: Long? = null,
) {
    fun anchorDay(zoneId: ZoneId = ZoneId.systemDefault()): Int =
        recurrenceAnchorDay
            ?: Instant.ofEpochMilli(date).atZone(zoneId).dayOfMonth
}

@Entity(tableName = "completion_events")
data class CompletionEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reminderId: Long,
    val completedAt: Long,
)
