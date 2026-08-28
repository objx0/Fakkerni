package com.fakarni.app.notifications

import com.fakarni.app.data.Recurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class RecurrenceCalculatorTest {
    private val zone = ZoneId.of("Africa/Tripoli")

    @Test
    fun noneHasNoNextOccurrence() {
        assertNull(RecurrenceCalculator.nextOccurrence(1_000L, Recurrence.NONE, 1, 2_000L, zone))
    }

    @Test
    fun dailySkipsMissedOccurrencesAndKeepsLocalTime() {
        val scheduled = at(2026, 8, 10, 9, 30)
        val now = at(2026, 8, 12, 12, 0)

        val result = RecurrenceCalculator.nextOccurrence(scheduled, Recurrence.DAILY, 10, now, zone)

        assertEquals(at(2026, 8, 13, 9, 30), result)
    }

    @Test
    fun monthlyClampsToLeapDayAtMonthEnd() {
        val scheduled = at(2028, 1, 31, 8, 0)

        val result = RecurrenceCalculator.nextOccurrence(scheduled, Recurrence.MONTHLY, 31, scheduled, zone)

        assertEquals(at(2028, 2, 29, 8, 0), result)
    }

    @Test
    fun monthlyRestoresAnchorDayWhenNextMonthAllowsIt() {
        val scheduled = at(2028, 2, 29, 8, 0)

        val result = RecurrenceCalculator.nextOccurrence(scheduled, Recurrence.MONTHLY, 31, scheduled, zone)

        assertEquals(at(2028, 3, 31, 8, 0), result)
    }

    @Test
    fun weeklySkipsForwardUntilFuture() {
        val scheduled = at(2026, 8, 1, 14, 15)
        val now = at(2026, 8, 20, 8, 0)

        val result = RecurrenceCalculator.nextOccurrence(scheduled, Recurrence.WEEKLY, 1, now, zone)

        assertEquals(at(2026, 8, 22, 14, 15), result)
    }

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant().toEpochMilli()
}
