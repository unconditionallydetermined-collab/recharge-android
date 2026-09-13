package com.example.recharge.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recharge_prefs")

@Singleton
class RechargePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val QUEUE_INDEX = intPreferencesKey("queue_index")
        val REDIRECT_STARTED_AT = longPreferencesKey("redirect_started_at")
        val ACTIVE_APP_PACKAGE = stringPreferencesKey("active_app_package")
        val RECHARGE_COMPLETED_AT = longPreferencesKey("recharge_completed_at")
        val QUOTES_EDITED_AT = longPreferencesKey("quotes_edited_at")
        val YOUTUBE_URL = stringPreferencesKey("youtube_url")
        val QUOTE_COUNT = intPreferencesKey("quote_count")
        // Quote texts stored as quote_text_0, quote_text_1, ...
        fun quoteTextKey(index: Int) = stringPreferencesKey("quote_text_$index")
    }

    val queueIndex: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.QUEUE_INDEX] ?: 0 }

    val redirectStartedAt: Flow<Long> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.REDIRECT_STARTED_AT] ?: 0L }

    val rechargeCompletedAt: Flow<Long> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.RECHARGE_COMPLETED_AT] ?: 0L }

    val quotesEditedAt: Flow<Long> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.QUOTES_EDITED_AT] ?: 0L }

    val youtubeUrl: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.YOUTUBE_URL] ?: "" }

    val quoteCount: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.QUOTE_COUNT] ?: 3 }

    suspend fun getQuoteTexts(): List<String> {
        val prefs = context.dataStore.data.first()
        val count = prefs[Keys.QUOTE_COUNT] ?: 3
        return (0 until count).map { i ->
            prefs[Keys.quoteTextKey(i)] ?: defaultQuotes.getOrElse(i) { "" }
        }
    }

    suspend fun setQueueIndex(index: Int) {
        context.dataStore.edit { it[Keys.QUEUE_INDEX] = index }
    }

    suspend fun setRedirectStartedAt(timestamp: Long) {
        context.dataStore.edit { it[Keys.REDIRECT_STARTED_AT] = timestamp }
    }

    suspend fun setRechargeCompletedAt(timestamp: Long) {
        context.dataStore.edit { it[Keys.RECHARGE_COMPLETED_AT] = timestamp }
    }

    suspend fun setQuotesEditedAt(timestamp: Long) {
        context.dataStore.edit { it[Keys.QUOTES_EDITED_AT] = timestamp }
    }

    suspend fun setYoutubeUrl(url: String) {
        context.dataStore.edit { it[Keys.YOUTUBE_URL] = url }
    }

    suspend fun saveQuotes(quotes: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.QUOTE_COUNT] = quotes.size
            quotes.forEachIndexed { i, text ->
                prefs[Keys.quoteTextKey(i)] = text
            }
        }
    }

    companion object {
        val defaultQuotes = listOf(
            "Step away from digital noise. Unclench your jaw, drop your shoulders, and take three restorative breaths.",
            "Your attention is your most valuable resource. Choose where it goes.",
            "Stillness is not the absence of action. It is the foundation of intentional action."
        )
    }
}

/** Timing constants — single source of truth */
object TimingConfig {
    const val RECHARGE_HIDDEN_MS = 90L * 60 * 1000       // 90 min
    const val REDIRECT_ACTIVE_MS = 90L * 60 * 1000       // 90 min
    const val REDIRECT_LOCK_MS = 20L * 60 * 1000         // 20 min
    const val QUOTE_EDIT_LOCK_MS = 72L * 60 * 60 * 1000  // 72 hours
    const val MIN_DWELL_MS = 5L * 60 * 1000              // 5 min
}
