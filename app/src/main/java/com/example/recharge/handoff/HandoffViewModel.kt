package com.example.recharge.handoff

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recharge.data.datastore.RechargePreferences
import com.example.recharge.data.room.EventDao
import com.example.recharge.data.room.EventEntity
import com.example.recharge.data.room.QueueItemDao
import com.example.recharge.data.room.SessionDao
import com.example.recharge.data.room.SessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

data class HandoffUiState(
    val nextAppName: String = "app",
    val nextAppPackage: String = "",
    val isReady: Boolean = false
)

@HiltViewModel
class HandoffViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: RechargePreferences,
    private val queueDao: QueueItemDao,
    private val sessionDao: SessionDao,
    private val eventDao: EventDao
) : ViewModel() {

    private val _state = MutableStateFlow(HandoffUiState())
    val state: StateFlow<HandoffUiState> = _state.asStateFlow()

    // The session started when the user first tapped "Start Recharge" (quote start).
    // For simplicity, we use the recharge_completed_at (set at video end) as the
    // session "ended_at" and record the handoff separately.
    private val sessionId = UUID.randomUUID().toString()

    init {
        viewModelScope.launch {
            val index = prefs.queueIndex.first()
            val items = queueDao.getAllOnce()
            val nextItem = items.getOrNull(index)
            _state.update {
                it.copy(
                    nextAppName = nextItem?.appName ?: "app",
                    nextAppPackage = nextItem?.packageName ?: "",
                    isReady = nextItem != null
                )
            }
        }
    }

    /**
     * User tapped the handoff surface. Launch the next queue app, log the
     * completed session + redirect event, and advance the queue pointer.
     */
    fun performHandoff() {
        viewModelScope.launch {
            val currentState = _state.value
            val now = System.currentTimeMillis()

            // 1. Launch the next app
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(currentState.nextAppPackage)
                    ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                if (intent != null) {
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to launch ${currentState.nextAppPackage}")
            }

            // 2. Log redirect event
            eventDao.insert(
                EventEntity(
                    eventType = "redirect",
                    packageName = currentState.nextAppPackage,
                    appName = currentState.nextAppName,
                    sessionId = sessionId
                )
            )

            // 3. Record redirect started + active app for the 90-min lock cycle
            prefs.setRedirectStartedAt(now)

            // 4. Advance the queue pointer
            val index = prefs.queueIndex.first()
            val items = queueDao.getAllOnce()
            if (index < items.size - 1) {
                prefs.setQueueIndex(index + 1)
            } else {
                // Wrap around to the start of the queue
                prefs.setQueueIndex(0)
            }
        }
    }
}
