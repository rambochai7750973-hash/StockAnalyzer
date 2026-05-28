package com.stock.analyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stock.analyzer.data.local.entity.StockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks ORDER BY changePercent DESC")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE code LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'")
    fun searchStocks(query: String): Flow<List<StockEntity>>

    @Query("SELECT COUNT(*) FROM stocks")
    suspend fun getCount(): Int

    @Query("SELECT * FROM stocks WHERE code = :code")
    suspend fun getStockByCode(code: String): StockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)

    @Query("DELETE FROM stocks")
    suspend fun deleteAll()
}
