package com.example.recharge.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recharge.data.datastore.RechargePreferences
import com.example.recharge.data.datastore.TimingConfig
import com.example.recharge.data.room.EventEntity
import com.example.recharge.data.room.EventDao
import com.example.recharge.data.room.QueueItemDao
import com.example.recharge.data.room.QueueItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class HomeUiState(
    val queueItems: List<QueueItemEntity> = emptyList(),
    val queueIndex: Int = 0,
    val isQueueEmpty: Boolean = true,
    val showRecharge: Boolean = true,
    val showRedirect: Boolean = true,
    val nextAppName: String = "Notion",
    val foregroundApp: String? = null,
    val serviceActive: Boolean = false,
    val serviceElapsedMinutes: Int = 24
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: RechargePreferences,
    private val queueDao: QueueItemDao,
    private val eventDao: EventDao
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        observeState()
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                queueDao.getAll(),
                prefs.queueIndex,
                prefs.rechargeCompletedAt,
                prefs.redirectStartedAt
            ) { items, index, rechargeAt, redirectAt ->
                val now = System.currentTimeMillis()

                // Visibility rules
                val rechargeHidden = rechargeAt > 0 && (now - rechargeAt) < TimingConfig.RECHARGE_HIDDEN_MS
                val redirectActive = redirectAt > 0 && (now - redirectAt) < TimingConfig.REDIRECT_ACTIVE_MS
                val redirectLocked = redirectAt > 0 &&
                    (now - redirectAt) >= TimingConfig.REDIRECT_ACTIVE_MS &&
                    (now - redirectAt) < (TimingConfig.REDIRECT_ACTIVE_MS + TimingConfig.REDIRECT_LOCK_MS)

                val nextApp = items.getOrNull(index)?.appName ?: "app"

                HomeUiState(
                    queueItems = items,
                    queueIndex = index,
                    isQueueEmpty = items.isEmpty(),
                    showRecharge = !rechargeHidden,
                    showRedirect = !redirectLocked,
                    nextAppName = nextApp,
                    serviceActive = true // shown when queue is active
                )
            }.collect { newState ->
                _state.update { newState }
            }
        }
    }

    fun launchRedirect() {
        viewModelScope.launch {
            val items = queueDao.getAllOnce()
            val index = prefs.queueIndex.first()
            val item = items.getOrNull(index) ?: return@launch

            // Launch app
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(item.packageName)
                    ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                if (intent != null) {
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to launch ${item.packageName}")
            }

            // Log event + record timestamp
            prefs.setRedirectStartedAt(System.currentTimeMillis())
            eventDao.insert(
                EventEntity(
                    eventType = "redirect",
                    packageName = item.packageName,
                    appName = item.appName
                )
            )
        }
    }
}
