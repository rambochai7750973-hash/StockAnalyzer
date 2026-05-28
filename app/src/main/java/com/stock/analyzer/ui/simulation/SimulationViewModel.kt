package com.stock.analyzer.ui.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stock.analyzer.domain.model.AccountInfo
import com.stock.analyzer.domain.model.PortfolioStock
import com.stock.analyzer.domain.model.TradeRecord
import com.stock.analyzer.domain.repository.SimulationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SimulationUiState(
    val holdings: List<PortfolioStock> = emptyList(),
    val trades: List<TradeRecord> = emptyList(),
    val account: AccountInfo = AccountInfo(balance = 100000.0),
    val selectedTab: Int = 0,
    val showBuyDialog: Boolean = false,
    val showSellDialog: Boolean = false,
    val buyCode: String = "",
    val sellCode: String = "",
    val buyPrice: String = "",
    val buyShares: String = "",
    val sellPrice: String = "",
    val sellShares: String = "",
    val message: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val repository: SimulationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulationUiState())
    val uiState: StateFlow<SimulationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllHoldings().collect { holdings ->
                _uiState.value = _uiState.value.copy(holdings = holdings)
            }
        }
        viewModelScope.launch {
            repository.getAllTrades().collect { trades ->
                _uiState.value = _uiState.value.copy(trades = trades)
            }
        }
        viewModelScope.launch {
            repository.getAccount().collect { account ->
                _uiState.value = _uiState.value.copy(account = account)
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun showBuyDialog(code: String, price: String) {
        _uiState.value = _uiState.value.copy(
            showBuyDialog = true, buyCode = code,
            buyPrice = price, buyShares = "100", message = null
        )
    }

    fun showSellDialog(code: String, price: String) {
        _uiState.value = _uiState.value.copy(
            showSellDialog = true, sellCode = code,
            sellPrice = price, sellShares = "", message = null
        )
    }

    fun hideDialogs() {
        _uiState.value = _uiState.value.copy(
            showBuyDialog = false, showSellDialog = false,
            buyCode = "", sellCode = ""
        )
    }

    fun updateBuyShares(shares: String) {
        _uiState.value = _uiState.value.copy(buyShares = shares)
    }

    fun updateSellShares(shares: String) {
        _uiState.value = _uiState.value.copy(sellShares = shares)
    }

    fun executeBuy() {
        viewModelScope.launch {
            val state = _uiState.value
            val price = state.buyPrice.toDoubleOrNull() ?: return@launch
            val shares = state.buyShares.toIntOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val holding = state.holdings.find { it.code == state.buyCode }
            val name = holding?.name ?: state.buyCode
            val result = repository.buyStock(state.buyCode, name, price, shares)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = result.getOrElse { it.message },
                showBuyDialog = result.isSuccess.not()
            )
        }
    }

    fun executeSell() {
        viewModelScope.launch {
            val state = _uiState.value
            val price = state.sellPrice.toDoubleOrNull() ?: return@launch
            val shares = state.sellShares.toIntOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = repository.sellStock(state.sellCode, price, shares)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = result.getOrElse { it.message },
                showSellDialog = result.isSuccess.not()
            )
        }
    }

    fun addFunds() {
        viewModelScope.launch {
            repository.addFunds(50000.0)
            _uiState.value = _uiState.value.copy(message = "充值成功 +50,000")
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
