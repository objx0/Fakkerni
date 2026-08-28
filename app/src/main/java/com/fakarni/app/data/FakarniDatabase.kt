package com.fakarni.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Reminder::class, CompletionEvent::class], version = 2, exportSchema = true)
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
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS completion_events (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, reminderId INTEGER NOT NULL, completedAt INTEGER NOT NULL)")
            }
        }
    }
}
