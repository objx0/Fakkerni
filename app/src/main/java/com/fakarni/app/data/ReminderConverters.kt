package com.fakarni.app.data

import androidx.room.TypeConverter

class ReminderConverters {
    @TypeConverter
    fun fromRecurrence(value: Recurrence): String = value.name

    @TypeConverter
    fun toRecurrence(value: String): Recurrence =
        runCatching { Recurrence.valueOf(value) }.getOrDefault(Recurrence.NONE)

    @TypeConverter
    fun fromCategory(value: ReminderCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): ReminderCategory =
        runCatching { ReminderCategory.valueOf(value) }.getOrDefault(ReminderCategory.PERSONAL)
}
