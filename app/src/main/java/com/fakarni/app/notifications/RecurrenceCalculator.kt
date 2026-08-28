package com.fakarni.app.notifications

import com.fakarni.app.data.Recurrence
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

object RecurrenceCalculator {
    fun nextOccurrence(
        scheduledAt: Long,
        recurrence: Recurrence,
        anchorDay: Int,
        now: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Long? {
        if (recurrence == Recurrence.NONE) return null

        var next = Instant.ofEpochMilli(scheduledAt).atZone(zoneId)
        do {
            next = when (recurrence) {
                Recurrence.DAILY -> next.plusDays(1)
                Recurrence.WEEKLY -> next.plusWeeks(1)
                Recurrence.MONTHLY -> nextMonth(next, anchorDay)
                Recurrence.NONE -> return null
            }
        } while (next.toInstant().toEpochMilli() <= now)
        return next.toInstant().toEpochMilli()
    }

    private fun nextMonth(current: ZonedDateTime, anchorDay: Int): ZonedDateTime {
        val targetMonth = YearMonth.from(current).plusMonths(1)
        val day = anchorDay.coerceIn(1, targetMonth.lengthOfMonth())
        return current
            .withYear(targetMonth.year)
            .withMonth(targetMonth.monthValue)
            .withDayOfMonth(day)
    }
}
