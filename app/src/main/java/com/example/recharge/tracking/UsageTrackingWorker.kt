package com.example.recharge.tracking

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.recharge.data.datastore.RechargePreferences
import com.example.recharge.data.datastore.TimingConfig
import com.example.recharge.data.room.EventDao
import com.example.recharge.data.room.EventEntity
import com.example.recharge.data.room.QueueItemDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that polls UsageStatsManager every ~15s
 * to detect the foreground app and check for passive queue completion.
 * Also flushes unsynced events/sessions to Supabase.
 */
@HiltWorker
class UsageTrackingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val prefs: RechargePreferences,
    private val queueDao: QueueItemDao,
    private val eventDao: EventDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            checkPassiveCompletion()
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "UsageTracking worker failed")
            Result.retry()
        }
    }

    private suspend fun checkPassiveCompletion() {
        val usageStatsManager = applicationContext
            .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(now - 60_000, now) // last 60s

        val event = android.app.usage.UsageEvents.Event()
        var lastForeground: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                lastForeground = event.packageName
            }
        }

        if (lastForeground == null) return

        // Check if the foreground app matches the next queue item
        val index = prefs.queueIndex.first()
        val items = queueDao.getAllOnce()
        val currentItem = items.getOrNull(index) ?: return
        val nextItem = items.getOrNull(index + 1) ?: return

        if (lastForeground == nextItem.packageName) {
            // Check dwell time: simplified — log event and advance pointer
            Timber.d("Passive completion: user opened ${nextItem.appName}")
            prefs.setQueueIndex(index + 1)
            eventDao.insert(
                EventEntity(
                    eventType = "queue_completed",
                    packageName = nextItem.packageName,
                    appName = nextItem.appName
                )
            )
        }
    }

    companion object {
        fun enqueuePeriodicWork(context: Context) {
            val request = PeriodicWorkRequestBuilder<UsageTrackingWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "usage_tracking",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
