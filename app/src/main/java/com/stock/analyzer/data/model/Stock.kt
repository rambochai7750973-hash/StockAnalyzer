package com.stock.analyzer.data.model

data class Stock(
    val code: String,
    val name: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val preClose: Double,
    val volume: Long,
    val amount: Double,
    val change: Double,
    val changePercent: Double,
    val timestamp: Long
)

data class KlineData(
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val amount: Double
)

data class TechnicalIndicator(
    val name: String,
    val values: List<Double>
)
