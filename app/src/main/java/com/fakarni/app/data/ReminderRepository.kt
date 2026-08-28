package com.fakarni.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class ReminderRepository(
    private val context: Context,
    private val dao: ReminderDao,
) {
    private val initializationMutex = Mutex()
    @Volatile private var initialized = false

    val reminders: Flow<List<Reminder>> = dao.observeAll()
    val completionEvents: Flow<List<CompletionEvent>> = dao.observeCompletionEvents()

    suspend fun initialize() = initializationMutex.withLock {
        if (initialized) return
        migrateLegacyData()
        initialized = true
    }

    suspend fun getById(id: Long): Reminder? {
        initialize()
        return dao.getById(id)
    }

    suspend fun getActive(): List<Reminder> {
        initialize()
        return dao.getActive()
    }

    suspend fun save(reminder: Reminder) {
        initialize()
        dao.upsert(reminder)
    }

    suspend fun delete(reminder: Reminder) {
        initialize()
        dao.delete(reminder)
    }

    suspend fun recordCompletion(reminderId: Long, completedAt: Long = System.currentTimeMillis()) {
        initialize()
        dao.insertCompletion(CompletionEvent(reminderId = reminderId, completedAt = completedAt))
    }

    private suspend fun migrateLegacyData() {
        val prefs = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_MIGRATED, false)) return

        if (dao.count() == 0) {
            val raw = prefs.getString("items", null)
            val reminders = if (raw.isNullOrBlank()) starterReminders() else parseLegacy(raw)
            dao.insertAll(reminders)
        }
        prefs.edit().putBoolean(KEY_MIGRATED, true).apply()
    }

    private fun parseLegacy(raw: String): List<Reminder> = runCatching {
        val array = JSONArray(raw)
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val date = item.getLong("date")
            Reminder(
                id = item.getLong("id"),
                title = item.getString("title"),
                note = item.optString("note"),
                date = date,
                icon = item.optString("icon", "✨"),
                done = item.optBoolean("done"),
                recurrence = parseRecurrence(item.optString("recurrence")),
                category = parseCategory(item.optString("category")),
                recurrenceAnchorDay = Instant.ofEpochMilli(date)
                    .atZone(ZoneId.systemDefault()).dayOfMonth,
            )
        }
    }.getOrDefault(emptyList())

    private fun starterReminders(): List<Reminder> {
        val now = System.currentTimeMillis()
        return listOf(
            Reminder(1, "تغيير زيت السيارة", "كل ٣ أشهر", now + TimeUnit.DAYS.toMillis(2), "🚗", category = ReminderCategory.CAR),
            Reminder(2, "مراجعة اشتراك Netflix", "تأكد من التجديد أو الإلغاء", now + TimeUnit.DAYS.toMillis(5), "🎬", category = ReminderCategory.SUBSCRIPTIONS),
            Reminder(3, "اشرب ماء", "كوب ماء", now + TimeUnit.HOURS.toMillis(1), "💧", category = ReminderCategory.HEALTH),
        ).map { it.copy(recurrenceAnchorDay = it.anchorDay()) }
    }

    private fun parseRecurrence(value: String): Recurrence = when (value) {
        "يومياً", "يوميًا", "DAILY" -> Recurrence.DAILY
        "أسبوعياً", "أسبوعيًا", "WEEKLY" -> Recurrence.WEEKLY
        "شهرياً", "شهريًا", "MONTHLY" -> Recurrence.MONTHLY
        else -> Recurrence.NONE
    }

    private fun parseCategory(value: String): ReminderCategory = when (value) {
        "السيارة", "CAR" -> ReminderCategory.CAR
        "الصحة", "HEALTH" -> ReminderCategory.HEALTH
        "الاشتراكات", "SUBSCRIPTIONS" -> ReminderCategory.SUBSCRIPTIONS
        "المنزل", "HOME" -> ReminderCategory.HOME
        "المال", "MONEY" -> ReminderCategory.MONEY
        else -> ReminderCategory.PERSONAL
    }

    private companion object {
        const val KEY_MIGRATED = "migrated_to_room_v1"
    }
}
