package com.stock.analyzer.domain.usecase

import com.stock.analyzer.data.model.Stock
import com.stock.analyzer.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStockQuoteUseCase @Inject constructor(
    private val repository: StockRepository
) {
    fun getStocks(): Flow<List<Stock>> = repository.getStocks()

    fun searchStocks(query: String): Flow<List<Stock>> = repository.searchStocks(query)

    suspend fun refresh() = repository.refreshStocks()
}
