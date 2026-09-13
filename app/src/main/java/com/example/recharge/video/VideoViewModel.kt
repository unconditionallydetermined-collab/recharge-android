package com.example.recharge.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recharge.data.datastore.RechargePreferences
import com.example.recharge.data.room.EventDao
import com.example.recharge.data.room.EventEntity
import com.example.recharge.data.room.QueueItemDao
import com.example.recharge.data.room.SessionDao
import com.example.recharge.data.room.SessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class VideoUiState(
    val youtubeUrl: String = "",
    val urlInput: String = "",
    val urlError: String? = null,
    val isPlaying: Boolean = true,
    val nextAppName: String = "Notion",
    val sessionId: String = UUID.randomUUID().toString()
)

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val prefs: RechargePreferences,
    private val sessionDao: SessionDao,
    private val eventDao: EventDao,
    private val queueDao: QueueItemDao
) : ViewModel() {

    private val _state = MutableStateFlow(VideoUiState())
    val state: StateFlow<VideoUiState> = _state.asStateFlow()

    private val sessionStarted = System.currentTimeMillis()

    private val predefinedVideos = listOf(
        "https://www.youtube.com/watch?v=inpok4MKVLM", // Example breathing/meditation
        "https://www.youtube.com/watch?v=ZToicYcHIOU", // Example relaxation
        "https://www.youtube.com/watch?v=txQ6t4yPIM0"  // Example yoga nidra
    )

    init {
        viewModelScope.launch {
            combine(prefs.youtubeUrl, prefs.queueIndex) { url, index ->
                val items = queueDao.getAllOnce()
                val finalUrl = if (url.isBlank()) predefinedVideos.random() else url
                Pair(finalUrl, items.getOrNull(index)?.appName ?: "app")
            }.collect { (url, nextApp) ->
                _state.update { it.copy(youtubeUrl = url, nextAppName = nextApp) }
            }
        }
    }

    fun onUrlInput(url: String) {
        _state.update { it.copy(urlInput = url, urlError = null) }
    }

    fun saveUrl() {
        val url = _state.value.urlInput.trim()
        if (!isValidYouTubeUrl(url)) {
            _state.update { it.copy(urlError = "Please enter a valid YouTube URL") }
            return
        }
        viewModelScope.launch {
            prefs.setYoutubeUrl(url)
            _state.update { it.copy(youtubeUrl = url, urlError = null) }
        }
    }

    fun togglePlayPause() {
        _state.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun completeSession() {
        viewModelScope.launch {
            val sessionId = _state.value.sessionId
            val now = System.currentTimeMillis()
            val nextApp = _state.value.nextAppName

            // Insert session
            sessionDao.insert(
                SessionEntity(
                    id = sessionId,
                    startedAt = sessionStarted,
                    endedAt = now,
                    status = "completed",
                    destinationPackage = nextApp
                )
            )

            // Log event
            eventDao.insert(EventEntity(
                eventType = "recharge_completed",
                appName = nextApp,
                sessionId = sessionId
            ))

            // Set cooldown
            prefs.setRechargeCompletedAt(now)
        }
    }

    private fun isValidYouTubeUrl(url: String): Boolean {
        val regex = Regex("(?:youtu\\.be/|youtube\\.com/(?:watch\\?v=|embed/|v/))([a-zA-Z0-9_-]{11})")
        return regex.containsMatchIn(url)
    }
}
