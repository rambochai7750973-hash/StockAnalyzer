package com.stock.analyzer.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stock.analyzer.data.model.KlineData
import com.stock.analyzer.data.model.Stock
import com.stock.analyzer.domain.model.TimePeriod
import com.stock.analyzer.domain.usecase.GetKlineDataUseCase
import com.stock.analyzer.domain.usecase.GetStockQuoteUseCase
import com.stock.analyzer.domain.usecase.ManageWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val stock: Stock? = null,
    val klineData: List<KlineData> = emptyList(),
    val selectedPeriod: TimePeriod = TimePeriod.DAY,
    val isInWatchlist: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStockQuoteUseCase: GetStockQuoteUseCase,
    private val getKlineDataUseCase: GetKlineDataUseCase,
    private val watchlistUseCase: ManageWatchlistUseCase
) : ViewModel() {

    val stockCode: String = savedStateHandle.get<String>("code") ?: ""

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        if (stockCode.isNotBlank()) {
            loadStock()
            loadKlineData()
            observeWatchlist()
        }
    }

    fun onPeriodChanged(period: TimePeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadKlineData()
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            val stock = _uiState.value.stock ?: return@launch
            if (_uiState.value.isInWatchlist) {
                watchlistUseCase.remove(stockCode)
            } else {
                watchlistUseCase.add(stockCode, stock.name)
            }
        }
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            watchlistUseCase.isInWatchlist(stockCode).collect { inList ->
                _uiState.value = _uiState.value.copy(isInWatchlist = inList)
            }
        }
    }

    private fun loadStock() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                getStockQuoteUseCase.searchStocks(stockCode.uppercase()).collect { stocks ->
                    val found = stocks.firstOrNull()
                    _uiState.value = _uiState.value.copy(
                        stock = found,
                        isLoading = false
                    )
                    if (found != null) return@collect
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun loadKlineData() {
        viewModelScope.launch {
            try {
                val data = getKlineDataUseCase(
                    code = stockCode,
                    period = _uiState.value.selectedPeriod.apiValue
                )
                _uiState.value = _uiState.value.copy(klineData = data)
            } catch (_: Exception) { }
        }
    }
}
