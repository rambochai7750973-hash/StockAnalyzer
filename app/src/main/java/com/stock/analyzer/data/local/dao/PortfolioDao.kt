package com.stock.analyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stock.analyzer.data.local.entity.PortfolioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio")
    fun getAllHoldings(): Flow<List<PortfolioEntity>>

    @Query("SELECT * FROM portfolio WHERE code = :code")
    suspend fun getHolding(code: String): PortfolioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHolding(holding: PortfolioEntity)

    @Query("DELETE FROM portfolio WHERE code = :code")
    suspend fun removeHolding(code: String)

    @Query("SELECT COUNT(*) FROM portfolio")
    suspend fun getCount(): Int
}
