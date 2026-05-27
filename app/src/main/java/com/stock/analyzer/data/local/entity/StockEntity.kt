package com.stock.analyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey val code: String,
    val name: String,
    val open: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val close: Double = 0.0,
    val preClose: Double = 0.0,
    val volume: Long = 0,
    val amount: Double = 0.0,
    val change: Double = 0.0,
    val changePercent: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "kline_cache")
data class KlineCacheEntity(
    @PrimaryKey val id: String,
    val code: String,
    val period: String,
    val json: String,
    val timestamp: Long = System.currentTimeMillis()
)
