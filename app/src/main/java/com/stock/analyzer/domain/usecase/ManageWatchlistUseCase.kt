package com.stock.analyzer.domain.usecase

import com.stock.analyzer.data.local.entity.WatchlistEntity
import com.stock.analyzer.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageWatchlistUseCase @Inject constructor(
    private val repository: WatchlistRepository
) {
    fun getAll(): Flow<List<WatchlistEntity>> = repository.getAllWatchlist()

    fun isInWatchlist(code: String): Flow<Boolean> = repository.isInWatchlist(code)

    suspend fun add(code: String, name: String) = repository.addToWatchlist(code, name)

    suspend fun remove(code: String) = repository.removeFromWatchlist(code)
}
