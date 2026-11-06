package com.fakarni.app.ui.components

import com.fakarni.app.data.CompletionEvent
import com.fakarni.app.data.Reminder
import java.time.LocalDate
import java.time.ZoneId

/** Logs per habit on [date]. */
fun habitCountsOn(events: List<CompletionEvent>, date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Map<Long, Int> =
    events.filter { it.completedAt.toLocalDate(zone) == date }.groupingBy { it.reminderId }.eachCount()

/** Share of all habit goals met on each day, 0f..1f. */
fun habitDayScores(habits: List<Reminder>, events: List<CompletionEvent>, zone: ZoneId = ZoneId.systemDefault()): Map<LocalDate, Float> {
    if (habits.isEmpty()) return emptyMap()
    val goals = habits.associate { it.id to it.dailyGoal.coerceAtLeast(1) }
    val totalGoal = goals.values.sum().toFloat()
    return events
        .filter { it.reminderId in goals }
        .groupBy { it.completedAt.toLocalDate(zone) }
        .mapValues { (_, dayEvents) ->
            val met = dayEvents.groupingBy { it.reminderId }.eachCount()
                .entries.sumOf { (id, count) -> minOf(count, goals.getValue(id)) }
            (met / totalGoal).coerceIn(0f, 1f)
        }
}

/** A day "counts" for the streak when at least one habit reached its daily goal. */
fun keptDays(habits: List<Reminder>, events: List<CompletionEvent>, zone: ZoneId = ZoneId.systemDefault()): Set<LocalDate> {
    val goals = habits.associate { it.id to it.dailyGoal.coerceAtLeast(1) }
    return events
        .filter { it.reminderId in goals }
        .groupBy { it.completedAt.toLocalDate(zone) }
        .filterValues { dayEvents -> dayEvents.groupingBy { it.reminderId }.eachCount().any { (id, n) -> n >= goals.getValue(id) } }
        .keys
}

fun currentStreak(kept: Set<LocalDate>, today: LocalDate): Int {
    var cursor = if (today in kept) today else today.minusDays(1)
    var streak = 0
    while (cursor in kept) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}

fun longestStreak(kept: Set<LocalDate>): Int {
    var best = 0
    kept.forEach { day ->
        if (day.minusDays(1) !in kept) {
            var length = 0
            var cursor = day
            while (cursor in kept) {
                length++
                cursor = cursor.plusDays(1)
            }
            best = maxOf(best, length)
        }
    }
    return best
}
