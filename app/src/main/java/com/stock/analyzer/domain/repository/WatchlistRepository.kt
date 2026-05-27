package com.stock.analyzer.domain.repository

import com.stock.analyzer.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>
    fun isInWatchlist(code: String): Flow<Boolean>
    suspend fun addToWatchlist(code: String, name: String)
    suspend fun removeFromWatchlist(code: String)
}
