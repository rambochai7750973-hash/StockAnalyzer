package com.stock.analyzer.domain.repository

import com.stock.analyzer.data.model.KlineData
import com.stock.analyzer.data.model.Stock
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getStocks(): Flow<List<Stock>>
    fun searchStocks(query: String): Flow<List<Stock>>
    suspend fun refreshStocks()
    suspend fun getStockByCode(code: String): Stock?
    suspend fun getKlineData(code: String, period: String, count: Int): List<KlineData>
}
