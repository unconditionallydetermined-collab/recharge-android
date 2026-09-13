package com.example.recharge.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.recharge.data.datastore.RechargePreferences
import com.example.recharge.data.room.EventDao
import com.example.recharge.data.room.QueueItemDao
import com.example.recharge.data.room.SessionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for syncing local data to Supabase.
 * Runs with network constraint + exponential backoff.
 * Only syncs unsynced records; never does a full scan.
 */
@HiltWorker
class SupabaseSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val supabase: SupabaseClient,
    private val prefs: RechargePreferences,
    private val queueDao: QueueItemDao,
    private val sessionDao: SessionDao,
    private val eventDao: EventDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return Result.success()

        return try {
            syncQueueState(userId)
            syncSessions(userId)
            syncEvents(userId)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Supabase sync failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun syncQueueState(userId: String) {
        val queueItems = queueDao.getAllOnce()
        val queueIndex = prefs.queueIndex.first()
        val youtubeUrl = prefs.youtubeUrl.first()
        val rechargeCompletedAt = prefs.rechargeCompletedAt.first()
        val redirectStartedAt = prefs.redirectStartedAt.first()

        // Upsert recharge_state row
        supabase.postgrest["recharge_state"].upsert(
            mapOf(
                "user_id" to userId,
                "queue_index" to queueIndex,
                "youtube_url" to youtubeUrl,
                "recharge_completed_at" to rechargeCompletedAt,
                "redirect_started_at" to redirectStartedAt,
                "updated_at" to System.currentTimeMillis()
            )
        )
    }

    private suspend fun syncSessions(userId: String) {
        val unsynced = sessionDao.getUnsynced()
        for (session in unsynced) {
            supabase.postgrest["recharge_sessions"].insert(
                mapOf(
                    "id" to session.id,
                    "user_id" to userId,
                    "started_at" to session.startedAt,
                    "ended_at" to session.endedAt,
                    "status" to session.status,
                    "destination_app_id" to session.destinationPackage
                )
            )
            sessionDao.markSynced(session.id)
        }
    }

    private suspend fun syncEvents(userId: String) {
        val unsynced = eventDao.getUnsynced()
        for (event in unsynced) {
            supabase.postgrest["recharge_events"].insert(
                mapOf(
                    "user_id" to userId,
                    "event_type" to event.eventType,
                    "app_id" to event.packageName,
                    "app_name" to event.appName,
                    "session_id" to event.sessionId,
                    "occurred_at" to event.occurredAt
                )
            )
        }
        if (unsynced.isNotEmpty()) {
            eventDao.markSynced(unsynced.map { it.id })
        }
    }

    companion object {
        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SupabaseSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "supabase_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
