package com.fakarni.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Reminder::class, CompletionEvent::class], version = 4, exportSchema = true)
@TypeConverters(ReminderConverters::class)
abstract class FakarniDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var instance: FakarniDatabase? = null

        fun getInstance(context: Context): FakarniDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FakarniDatabase::class.java,
                    "fakarni.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS completion_events (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, reminderId INTEGER NOT NULL, completedAt INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN isHabit INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE reminders ADD COLUMN dailyGoal INTEGER NOT NULL DEFAULT 1")
                // Recurring health reminders were already treated as habits by the streak logic.
                db.execSQL("UPDATE reminders SET isHabit = 1 WHERE category = 'HEALTH' AND recurrence != 'NONE'")
            }
        }

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN leadMinutes INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
