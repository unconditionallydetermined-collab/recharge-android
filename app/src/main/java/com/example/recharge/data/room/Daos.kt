package com.example.recharge.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueItemDao {
    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    fun getAll(): Flow<List<QueueItemEntity>>

    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    suspend fun getAllOnce(): List<QueueItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: QueueItemEntity): Long

    @Update
    suspend fun update(item: QueueItemEntity)

    @Delete
    suspend fun delete(item: QueueItemEntity)

    @Query("DELETE FROM queue_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM queue_items")
    suspend fun count(): Int

    @Query("UPDATE queue_items SET synced = 0")
    suspend fun markAllUnsynced()

    @Query("SELECT * FROM queue_items WHERE synced = 0")
    suspend fun getUnsynced(): List<QueueItemEntity>
}

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE synced = 0")
    suspend fun getUnsynced(): List<SessionEntity>

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC LIMIT 10")
    fun getRecent(): Flow<List<SessionEntity>>

    @Query("UPDATE sessions SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Query("SELECT * FROM events WHERE synced = 0 ORDER BY occurredAt ASC LIMIT 50")
    suspend fun getUnsynced(): List<EventEntity>

    @Query("UPDATE events SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT * FROM events ORDER BY occurredAt DESC LIMIT 20")
    fun getRecent(): Flow<List<EventEntity>>
}
