package com.example.recharge.settings

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recharge.data.datastore.RechargePreferences
import com.example.recharge.data.datastore.TimingConfig
import com.example.recharge.data.room.QueueItemDao
import com.example.recharge.data.room.QueueItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class InstalledApp(val name: String, val packageName: String)

data class SettingsCombineData(
    val items: List<QueueItemEntity>,
    val index: Int,
    val locked: Boolean,
    val lockExpiresAt: Long,
    val url: String
)

data class SettingsUiState(
    val queueItems: List<QueueItemEntity> = emptyList(),
    val queueIndex: Int = 0,
    val isQuoteEditLocked: Boolean = false,
    val quoteLockExpiresAt: Long = 0L,
    val firstQuoteText: String = "You have power over your mind - not outside events. Realize this, and you will find strength.",

    val hasUsageStatsPermission: Boolean = false,
    val hasForegroundService: Boolean = true,
    val hasBatteryExemption: Boolean = false,
    val showAppScanner: Boolean = false,
    val installedApps: List<InstalledApp> = emptyList(),
    // Quote editor fields
    val editableQuotes: List<String> = listOf("", "", "", "", ""),
    val isEditingQuotes: Boolean = false,
    // Update state
    val updateMessage: String? = null,
    
    // YouTube
    val youtubeUrl: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: RechargePreferences,
    private val queueDao: QueueItemDao,
    private val updateManager: com.example.recharge.updater.UpdateManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                queueDao.getAll(),
                prefs.queueIndex,
                prefs.quotesEditedAt,
                prefs.youtubeUrl
            ) { items, index, editedAt, url ->
                // Return a data structure since we have more than 3 elements
                SettingsCombineData(items, index, false, 0L, url)
            }.collect { data ->
                _state.update {
                    it.copy(
                        queueItems = data.items,
                        queueIndex = data.index,
                        isQuoteEditLocked = data.locked,
                        quoteLockExpiresAt = data.lockExpiresAt,
                        youtubeUrl = data.url,
                        hasUsageStatsPermission = checkUsageStatsPermission(),
                        hasBatteryExemption = checkBatteryExemption()
                    )
                }
            }
        }
    }

    fun deleteQueueItem(item: QueueItemEntity) {
        viewModelScope.launch {
            queueDao.delete(item)
        }
    }

    fun showAppScanner() {
        viewModelScope.launch {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                .map { InstalledApp(pm.getApplicationLabel(it).toString(), it.packageName) }
                .sortedBy { it.name }
            _state.update { it.copy(showAppScanner = true, installedApps = apps) }
        }
    }

    fun hideAppScanner() {
        _state.update { it.copy(showAppScanner = false) }
    }

    fun addToQueue(app: InstalledApp) {
        viewModelScope.launch {
            val current = queueDao.getAllOnce()
            val nextPosition = current.size
            queueDao.insert(
                QueueItemEntity(
                    position = nextPosition,
                    packageName = app.packageName,
                    appName = app.name
                )
            )
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val items = queueDao.getAllOnce().toMutableList()
            if (fromIndex < 0 || fromIndex >= items.size || toIndex < 0 || toIndex >= items.size) return@launch
            val moved = items.removeAt(fromIndex)
            items.add(toIndex, moved)
            // Update positions
            items.forEachIndexed { index, item ->
                queueDao.update(item.copy(position = index, synced = false))
            }
        }
    }

    fun startEditingQuotes() {
        _state.update { it.copy(isEditingQuotes = true) }
    }

    fun updateEditableQuote(index: Int, text: String) {
        _state.update {
            val mutable = it.editableQuotes.toMutableList()
            if (index in mutable.indices) mutable[index] = text
            it.copy(editableQuotes = mutable)
        }
    }

    fun addEditableQuote() {
        _state.update {
            val quotes = it.editableQuotes.toMutableList()
            quotes.add("")
            it.copy(editableQuotes = quotes)
        }
    }

    fun removeEditableQuote(index: Int) {
        _state.update {
            val quotes = it.editableQuotes.toMutableList()
            if (index in quotes.indices && quotes.size > 1) {
                quotes.removeAt(index)
            }
            it.copy(editableQuotes = quotes)
        }
    }

    fun saveQuotes() {
        viewModelScope.launch {
            val quotes = _state.value.editableQuotes.filter { it.isNotBlank() }
            if (quotes.isEmpty()) return@launch
            prefs.saveQuotes(quotes)
            prefs.setQuotesEditedAt(System.currentTimeMillis())
            _state.update {
                it.copy(
                    isEditingQuotes = false,
                    firstQuoteText = quotes.first(),
                    isQuoteEditLocked = false,
                    quoteLockExpiresAt = 0L
                )
            }
        }
    }

    fun updateYoutubeUrlInput(url: String) {
        _state.update { it.copy(youtubeUrl = url) }
    }

    fun saveYoutubeUrl() {
        viewModelScope.launch {
            prefs.setYoutubeUrl(_state.value.youtubeUrl)
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    private fun checkBatteryExemption(): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } catch (e: Exception) {
            false
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _state.update { it.copy(updateMessage = "Checking for updates...") }
            val result = updateManager.checkForUpdates()
            val message = when (result) {
                is com.example.recharge.updater.UpdateManager.UpdateResult.Downloading -> "Update found! Downloading in background..."
                is com.example.recharge.updater.UpdateManager.UpdateResult.NoUpdate -> "You are on the latest version."
                is com.example.recharge.updater.UpdateManager.UpdateResult.Error -> result.message
            }
            _state.update { it.copy(updateMessage = message) }
        }
    }

    fun clearUpdateMessage() {
        _state.update { it.copy(updateMessage = null) }
    }
}
