package com.stock.analyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio")
data class PortfolioEntity(
    @PrimaryKey val code: String,
    val name: String,
    val shares: Int,
    val avgCost: Double,
    val totalInvested: Double
)

@Entity(tableName = "trade_history")
data class TradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val type: String,
    val price: Double,
    val shares: Int,
    val total: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 100000.0
)
