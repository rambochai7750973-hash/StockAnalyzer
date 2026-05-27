package com.stock.analyzer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.stock.analyzer.data.local.dao.StockDao
import com.stock.analyzer.data.local.dao.WatchlistDao
import com.stock.analyzer.data.local.entity.KlineCacheEntity
import com.stock.analyzer.data.local.entity.StockEntity
import com.stock.analyzer.data.local.entity.WatchlistEntity

@Database(
    entities = [StockEntity::class, WatchlistEntity::class, KlineCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun watchlistDao(): WatchlistDao
}
