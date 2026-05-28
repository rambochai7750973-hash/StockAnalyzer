package com.stock.analyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.stock.analyzer.data.local.entity.TradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trade_history ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trade_history WHERE code = :code ORDER BY timestamp DESC")
    fun getTradesByCode(code: String): Flow<List<TradeEntity>>

    @Insert
    suspend fun insertTrade(trade: TradeEntity)
}
