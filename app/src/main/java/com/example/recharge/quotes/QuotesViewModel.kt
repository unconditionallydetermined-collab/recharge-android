package com.example.recharge.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recharge.data.datastore.RechargePreferences
import com.example.recharge.data.datastore.TimingConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuotesUiState(
    val quotes: List<String> = RechargePreferences.defaultQuotes,
    val currentIndex: Int = 0,
    val totalCount: Int = 3,
    val isEditLocked: Boolean = false,
    val lockExpiresAt: Long = 0L
)

@HiltViewModel
class QuotesViewModel @Inject constructor(
    private val prefs: RechargePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(QuotesUiState())
    val state: StateFlow<QuotesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val savedQuotes = prefs.getQuoteTexts()
            prefs.quoteCount.collect { count ->
                _state.update {
                    it.copy(
                        quotes = if (savedQuotes.isNotEmpty()) savedQuotes else RechargePreferences.defaultQuotes,
                        totalCount = count,
                        isEditLocked = false,
                        lockExpiresAt = 0
                    )
                }
            }
        }
    }

    fun nextQuote() {
        val current = _state.value.currentIndex
        val total = _state.value.totalCount
        if (current < total - 1) {
            _state.update { it.copy(currentIndex = current + 1) }
        }
    }

    fun reset() {
        _state.update { it.copy(currentIndex = 0) }
    }

    fun saveQuotes(quotes: List<String>) {
        viewModelScope.launch {
            prefs.saveQuotes(quotes)
            prefs.setQuotesEditedAt(System.currentTimeMillis())
            _state.update {
                it.copy(quotes = quotes, totalCount = quotes.size, isEditLocked = false)
            }
        }
    }
}
