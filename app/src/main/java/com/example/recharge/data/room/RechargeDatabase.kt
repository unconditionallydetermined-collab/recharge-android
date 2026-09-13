package com.example.recharge.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [QueueItemEntity::class, SessionEntity::class, EventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RechargeDatabase : RoomDatabase() {
    abstract fun queueItemDao(): QueueItemDao
    abstract fun sessionDao(): SessionDao
    abstract fun eventDao(): EventDao
}
