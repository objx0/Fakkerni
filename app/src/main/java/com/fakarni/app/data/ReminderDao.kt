package com.fakarni.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY done ASC, date ASC")
    fun observeAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE done = 0 ORDER BY date ASC")
    suspend fun getActive(): List<Reminder>

    @Query("SELECT COUNT(*) FROM reminders")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: Reminder)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(reminders: List<Reminder>)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Insert
    suspend fun insertCompletion(event: CompletionEvent)

    @Query(
        "DELETE FROM completion_events WHERE id = (SELECT id FROM completion_events " +
            "WHERE reminderId = :reminderId AND completedAt >= :since ORDER BY completedAt DESC LIMIT 1)",
    )
    suspend fun deleteLatestCompletion(reminderId: Long, since: Long)

    @Query("SELECT * FROM completion_events ORDER BY completedAt DESC")
    fun observeCompletionEvents(): Flow<List<CompletionEvent>>
}
