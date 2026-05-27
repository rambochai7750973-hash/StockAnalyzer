package com.stock.analyzer.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stock.analyzer.data.model.Stock
import com.stock.analyzer.domain.usecase.GetStockQuoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketUiState(
    val stocks: List<Stock> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val getStockQuoteUseCase: GetStockQuoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        loadStocks()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            loadStocks()
        } else {
            viewModelScope.launch {
                getStockQuoteUseCase.searchStocks(query).collect { stocks ->
                    _uiState.value = _uiState.value.copy(stocks = stocks)
                }
            }
        }
    }

    fun loadStocks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                getStockQuoteUseCase.refresh()
                getStockQuoteUseCase.getStocks().collect { stocks ->
                    _uiState.value = _uiState.value.copy(
                        stocks = stocks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }
}
