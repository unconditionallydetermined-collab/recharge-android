package com.example.recharge.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- Queue Item ---
@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val position: Int,
    val packageName: String,
    val appName: String,
    val iconUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

// --- Session ---
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String, // UUID
    val startedAt: Long,
    val endedAt: Long?,
    val status: String, // "completed" | "abandoned"
    val destinationPackage: String?,
    val synced: Boolean = false
)

// --- Event ---
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String, // "redirect" | "recharge_started" | "recharge_completed" | "queue_completed" | etc.
    val packageName: String? = null,
    val appName: String? = null,
    val sessionId: String? = null,
    val occurredAt: Long = System.currentTimeMillis(),
    val metadataJson: String? = null,
    val synced: Boolean = false
)
