package com.fakarni.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderConvertersTest {
    private val converters = ReminderConverters()

    @Test
    fun invalidDatabaseValuesFallBackSafely() {
        assertEquals(Recurrence.NONE, converters.toRecurrence("unexpected"))
        assertEquals(ReminderCategory.PERSONAL, converters.toCategory("unexpected"))
    }

    @Test
    fun enumValuesRoundTrip() {
        Recurrence.entries.forEach { assertEquals(it, converters.toRecurrence(converters.fromRecurrence(it))) }
        ReminderCategory.entries.forEach { assertEquals(it, converters.toCategory(converters.fromCategory(it))) }
    }
}
