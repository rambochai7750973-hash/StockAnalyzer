package com.stock.analyzer.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stock.analyzer.data.local.entity.WatchlistEntity
import com.stock.analyzer.domain.usecase.ManageWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchlistUiState(
    val items: List<WatchlistEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistUseCase: ManageWatchlistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init { loadWatchlist() }

    fun removeFromWatchlist(code: String) {
        viewModelScope.launch { watchlistUseCase.remove(code) }
    }

    private fun loadWatchlist() {
        viewModelScope.launch {
            watchlistUseCase.getAll().collect { entities ->
                _uiState.value = WatchlistUiState(items = entities)
            }
        }
    }
}
