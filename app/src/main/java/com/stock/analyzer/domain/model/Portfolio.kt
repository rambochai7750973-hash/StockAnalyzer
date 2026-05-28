package com.stock.analyzer.domain.model

data class PortfolioStock(
    val code: String,
    val name: String,
    val shares: Int,
    val avgCost: Double,
    val currentPrice: Double = 0.0,
    val totalInvested: Double,
    val marketValue: Double = 0.0,
    val profitLoss: Double = 0.0,
    val profitLossPercent: Double = 0.0
)

data class TradeRecord(
    val id: Long,
    val code: String,
    val name: String,
    val type: String,
    val price: Double,
    val shares: Int,
    val total: Double,
    val timestamp: Long
)

data class AccountInfo(
    val balance: Double,
    val totalInvested: Double = 0.0,
    val marketValue: Double = 0.0,
    val totalProfitLoss: Double = 0.0
)
