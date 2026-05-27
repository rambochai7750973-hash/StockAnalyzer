package com.stock.analyzer.data.repository

import com.stock.analyzer.data.local.dao.WatchlistDao
import com.stock.analyzer.data.local.entity.WatchlistEntity
import com.stock.analyzer.domain.repository.WatchlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao
) : WatchlistRepository {

    override fun getAllWatchlist(): Flow<List<WatchlistEntity>> {
        return watchlistDao.getAllWatchlist()
    }

    override fun isInWatchlist(code: String): Flow<Boolean> {
        return watchlistDao.isInWatchlist(code)
    }

    override suspend fun addToWatchlist(code: String, name: String) {
        withContext(Dispatchers.IO) {
            watchlistDao.addToWatchlist(
                WatchlistEntity(code = code, name = name)
            )
        }
    }

    override suspend fun removeFromWatchlist(code: String) {
        withContext(Dispatchers.IO) {
            watchlistDao.removeFromWatchlist(code)
        }
    }
}
