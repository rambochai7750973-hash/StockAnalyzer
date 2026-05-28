package com.stock.analyzer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.stock.analyzer.data.local.dao.AccountDao
import com.stock.analyzer.data.local.dao.PortfolioDao
import com.stock.analyzer.data.local.dao.StockDao
import com.stock.analyzer.data.local.dao.TradeDao
import com.stock.analyzer.data.local.dao.WatchlistDao
import com.stock.analyzer.data.local.entity.AccountEntity
import com.stock.analyzer.data.local.entity.KlineCacheEntity
import com.stock.analyzer.data.local.entity.PortfolioEntity
import com.stock.analyzer.data.local.entity.StockEntity
import com.stock.analyzer.data.local.entity.TradeEntity
import com.stock.analyzer.data.local.entity.WatchlistEntity

@Database(
    entities = [StockEntity::class, WatchlistEntity::class, KlineCacheEntity::class,
        PortfolioEntity::class, TradeEntity::class, AccountEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun tradeDao(): TradeDao
    abstract fun accountDao(): AccountDao
}
