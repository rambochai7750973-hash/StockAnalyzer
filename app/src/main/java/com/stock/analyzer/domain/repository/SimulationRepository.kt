package com.stock.analyzer.domain.repository

import com.stock.analyzer.domain.model.AccountInfo
import com.stock.analyzer.domain.model.PortfolioStock
import com.stock.analyzer.domain.model.TradeRecord
import kotlinx.coroutines.flow.Flow

interface SimulationRepository {
    fun getAllHoldings(): Flow<List<PortfolioStock>>
    fun getAllTrades(): Flow<List<TradeRecord>>
    fun getAccount(): Flow<AccountInfo>
    suspend fun buyStock(code: String, name: String, price: Double, shares: Int): Result<String>
    suspend fun sellStock(code: String, price: Double, shares: Int): Result<String>
    suspend fun addFunds(amount: Double)
}
