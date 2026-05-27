package com.stock.analyzer.data.remote.dto

data class SinaQuoteDto(
    val code: String,
    val name: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val preClose: Double,
    val volume: Long,
    val amount: Double,
    val buyLevels: List<Level>,
    val sellLevels: List<Level>
) {
    data class Level(
        val price: Double,
        val volume: Long
    )
}

data class KlineItemDto(
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val amount: Double
)
