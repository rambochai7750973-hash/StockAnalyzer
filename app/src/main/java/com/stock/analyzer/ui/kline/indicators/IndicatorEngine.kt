package com.stock.analyzer.ui.kline.indicators

import com.stock.analyzer.data.model.KlineData

data class MaResult(
    val ma5: List<Double?>,
    val ma10: List<Double?>,
    val ma20: List<Double?>,
    val ma60: List<Double?>
)

data class MacdResult(
    val dif: List<Double>,
    val dea: List<Double>,
    val macd: List<Double>
)

data class KdjResult(
    val k: List<Double>,
    val d: List<Double>,
    val j: List<Double>
)

data class RsiResult(
    val rsi6: List<Double>,
    val rsi12: List<Double>,
    val rsi24: List<Double>
)

data class BollResult(
    val mid: List<Double>,
    val upper: List<Double>,
    val lower: List<Double>
)

object IndicatorEngine {

    fun calculateMA(data: List<KlineData>): MaResult {
        val closes = data.map { it.close }
        return MaResult(
            ma5 = ma(closes, 5),
            ma10 = ma(closes, 10),
            ma20 = ma(closes, 20),
            ma60 = ma(closes, 60)
        )
    }

    fun calculateMACD(
        data: List<KlineData>,
        fast: Int = 12,
        slow: Int = 26,
        signal: Int = 9
    ): MacdResult {
        val closes = data.map { it.close }
        val emaFast = ema(closes, fast)
        val emaSlow = ema(closes, slow)
        val dif = emaFast.zip(emaSlow) { a, b -> a - b }
        val dea = ema(dif, signal)
        val macd = dif.zip(dea) { d, e -> (d - e) * 2 }
        return MacdResult(
            dif = dif,
            dea = dea,
            macd = macd
        )
    }

    fun calculateKDJ(
        data: List<KlineData>,
        n: Int = 9,
        k1: Int = 3,
        d1: Int = 3
    ): KdjResult {
        val rsv = mutableListOf<Double>()
        for (i in data.indices) {
            val start = maxOf(0, i - n + 1)
            val high = data.subList(start, i + 1).maxOf { it.high }
            val low = data.subList(start, i + 1).minOf { it.low }
            val close = data[i].close
            val rsvVal = if (high != low) (close - low) / (high - low) * 100 else 50.0
            rsv.add(rsvVal)
        }
        val k = ema(rsv, k1, 50.0)
        val d = ema(k, d1, 50.0)
        val j = k.zip(d) { kv, dv -> 3 * kv - 2 * dv }
        return KdjResult(k = k, d = d, j = j)
    }

    fun calculateRSI(data: List<KlineData>): RsiResult {
        val closes = data.map { it.close }
        return RsiResult(
            rsi6 = rsi(closes, 6),
            rsi12 = rsi(closes, 12),
            rsi24 = rsi(closes, 24)
        )
    }

    fun calculateBOLL(
        data: List<KlineData>,
        period: Int = 20,
        multiplier: Double = 2.0
    ): BollResult {
        val closes = data.map { it.close }
        val mid = ma(closes, period)
        val upper = mutableListOf<Double>()
        val lower = mutableListOf<Double>()
        for (i in closes.indices) {
            val mv = mid[i]
            if (mv == null) {
                upper.add(0.0)
                lower.add(0.0)
            } else {
                val start = maxOf(0, i - period + 1)
                val window = closes.subList(start, i + 1)
                val std = stddev(window, mv)
                upper.add(mv + multiplier * std)
                lower.add(mv - multiplier * std)
            }
        }
        return BollResult(mid = mid.map { it ?: 0.0 }, upper = upper, lower = lower)
    }

    private fun ma(data: List<Double>, period: Int): List<Double?> {
        return data.indices.map { i ->
            if (i < period - 1) null
            else data.subList(i - period + 1, i + 1).average()
        }
    }

    private fun ema(data: List<Double>, period: Int, initValue: Double? = null): List<Double> {
        val result = mutableListOf<Double>()
        val k = 2.0 / (period + 1)
        for (i in data.indices) {
            val prev = if (i == 0) (initValue ?: data[i]) else result[i - 1]
            result.add(data[i] * k + prev * (1 - k))
        }
        return result
    }

    private fun rsi(data: List<Double>, period: Int): List<Double> {
        val result = mutableListOf<Double>()
        for (i in data.indices) {
            if (i < period) {
                result.add(50.0)
            } else {
                var gain = 0.0
                var loss = 0.0
                for (j in (i - period + 1)..i) {
                    val diff = data[j] - data[j - 1]
                    if (diff > 0) gain += diff else loss -= diff
                }
                val rs = if (loss == 0.0) 100.0 else gain / loss
                result.add(100.0 - 100.0 / (1 + rs))
            }
        }
        return result
    }

    private fun stddev(data: List<Double>, mean: Double): Double {
        if (data.size < 2) return 0.0
        val variance = data.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }
}
