package com.stock.analyzer.domain.model

data class StockQuote(
    val code: String,
    val name: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val preClose: Double,
    val volume: Long,
    val amount: Double
)

enum class TimePeriod(val label: String, val apiValue: String) {
    DAY("日线", "day"),
    WEEK("周线", "week"),
    MONTH("月线", "month")
}
