package com.stock.analyzer.ui.kline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stock.analyzer.data.model.KlineData
import com.stock.analyzer.ui.theme.KlineDown
import com.stock.analyzer.ui.theme.KlineUp
import com.stock.analyzer.ui.theme.Ma10
import com.stock.analyzer.ui.theme.Ma20
import com.stock.analyzer.ui.theme.Ma5
import com.stock.analyzer.ui.theme.Ma60
import com.stock.analyzer.ui.kline.indicators.IndicatorEngine
import com.stock.analyzer.ui.kline.indicators.MaResult
import com.stock.analyzer.ui.kline.indicators.MacdResult
import kotlin.math.abs

data class KlineConfig(
    val visibleCount: Int = 40,
    val candleWidth: Float = 10f,
    val candleGap: Float = 4f,
    val chartHeightRatio: Float = 0.7f // K线占图表高度的比例
)

@Composable
fun KlineChart(
    data: List<KlineData>,
    showMa: Boolean = true,
    showMacd: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    var scrollOffset by remember { mutableFloatStateOf(0f) }
    var visibleCount by remember { mutableIntStateOf(40) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var config by remember {
        mutableStateOf(KlineConfig())
    }

    val maResult = remember(data) {
        if (showMa) IndicatorEngine.calculateMA(data) else null
    }
    val macdResult = remember(data) {
        if (showMacd) IndicatorEngine.calculateMACD(data) else null
    }

    val density = LocalDensity.current

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    val newCount = (visibleCount / zoom).toInt().coerceIn(10, data.size)
                    visibleCount = newCount
                    config = config.copy(visibleCount = newCount)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { selectedIndex = -1 },
                    onHorizontalDrag = { _, dragAmount ->
                        scrollOffset += dragAmount
                        val totalWidth = config.candleWidth + config.candleGap
                        val indexOffset = (scrollOffset / totalWidth).toInt()
                        selectedIndex = (data.size - visibleCount + indexOffset)
                            .coerceIn(0, data.size - 1)
                    }
                )
            }
    ) {
        val width = size.width
        val chartHeight = size.height * config.chartHeightRatio
        val indicatorHeight = size.height * (1 - config.chartHeightRatio) - 20f
        val startIndex = (data.size - visibleCount).coerceAtLeast(0)

        val visibleData = data.subList(startIndex, data.size)

        // 计算价格范围
        val maxPrice = visibleData.maxOf { it.high }
        val minPrice = visibleData.minOf { it.low }
        val priceRange = (maxPrice - minPrice).coerceAtLeast(1.0)

        // 绘制K线
        drawCandlesticks(
            data = visibleData,
            startIndex = startIndex,
            maxPrice = maxPrice,
            minPrice = minPrice,
            priceRange = priceRange,
            chartHeight = chartHeight,
            config = config,
            selectedIndex = selectedIndex,
            totalWidth = width
        )

        // 绘制均线
        maResult?.let { ma ->
            drawMALine(ma.ma5, startIndex, visibleCount, maxPrice, minPrice, priceRange, chartHeight, config, Ma5)
            drawMALine(ma.ma10, startIndex, visibleCount, maxPrice, minPrice, priceRange, chartHeight, config, Ma10)
            drawMALine(ma.ma20, startIndex, visibleCount, maxPrice, minPrice, priceRange, chartHeight, config, Ma20)
            drawMALine(ma.ma60, startIndex, visibleCount, maxPrice, minPrice, priceRange, chartHeight, config, Ma60)
        }

        // 绘制指标
        macdResult?.let { macd ->
            drawMACD(
                macd = macd,
                startIndex = startIndex,
                visibleCount = visibleCount,
                indicatorHeight = indicatorHeight,
                chartHeight = chartHeight,
                config = config
            )
        }

        // 绘制十字光标
        if (selectedIndex in data.indices) {
            drawCrosshair(
                selectedIndex = selectedIndex,
                startIndex = startIndex,
                data = data,
                maxPrice = maxPrice,
                minPrice = minPrice,
                priceRange = priceRange,
                chartHeight = chartHeight,
                config = config
            )
        }
    }
}

private fun DrawScope.drawCandlesticks(
    data: List<KlineData>,
    startIndex: Int,
    maxPrice: Double,
    minPrice: Double,
    priceRange: Double,
    chartHeight: Float,
    config: KlineConfig,
    selectedIndex: Int,
    totalWidth: Float
) {
    val step = config.candleWidth + config.candleGap
    val startX = (totalWidth - data.size * step) / 2f + config.candleGap

    data.forEachIndexed { idx, kline ->
        val x = startX + idx * step
        val openY = priceToY(kline.open, maxPrice, minPrice, priceRange, chartHeight)
        val closeY = priceToY(kline.close, maxPrice, minPrice, priceRange, chartHeight)
        val highY = priceToY(kline.high, maxPrice, minPrice, priceRange, chartHeight)
        val lowY = priceToY(kline.low, maxPrice, minPrice, priceRange, chartHeight)
        val isUp = kline.close >= kline.open
        val color = if (isUp) KlineUp else KlineDown

        // 影线
        drawLine(
            color = color,
            start = Offset(x + config.candleWidth / 2, highY),
            end = Offset(x + config.candleWidth / 2, lowY),
            strokeWidth = 1f
        )

        // 实体
        if (abs(closeY - openY) > 1f) {
            drawRect(
                color = color,
                topLeft = Offset(x, if (isUp) closeY else openY),
                size = androidx.compose.ui.geometry.Size(config.candleWidth, abs(closeY - openY))
            )
        } else {
            drawRect(
                color = color,
                topLeft = Offset(x, openY),
                size = androidx.compose.ui.geometry.Size(config.candleWidth, 1.5f)
            )
        }
    }
}

private fun DrawScope.drawMALine(
    maValues: List<Double?>,
    startIndex: Int,
    visibleCount: Int,
    maxPrice: Double,
    minPrice: Double,
    priceRange: Double,
    chartHeight: Float,
    config: KlineConfig,
    color: Color
) {
    val step = config.candleWidth + config.candleGap
    val path = Path()
    var started = false

    for (i in 0 until visibleCount) {
        val dataIdx = startIndex + i
        val value = maValues.getOrNull(dataIdx) ?: continue
        val x = (config.candleGap + i * step) + config.candleWidth / 2
        val y = priceToY(value, maxPrice, minPrice, priceRange, chartHeight)

        if (!started) {
            path.moveTo(x, y)
            started = true
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(path, color = color, style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawMACD(
    macd: MacdResult,
    startIndex: Int,
    visibleCount: Int,
    indicatorHeight: Float,
    chartHeight: Float,
    config: KlineConfig
) {
    val step = config.candleWidth + config.candleGap
    val top = chartHeight + 10f
    val allValues = macd.macd + macd.dif + macd.dea
    val maxV = allValues.maxOf { abs(it) }.coerceAtLeast(1.0)
    val scale = indicatorHeight / 2 / maxV

    // 柱状图
    for (i in 0 until visibleCount) {
        val dataIdx = startIndex + i
        val value = macd.macd.getOrNull(dataIdx) ?: continue
        val x = config.candleGap + i * step + config.candleWidth / 2
        val barHeight = (value * scale).toFloat()
        val color = if (barHeight >= 0) KlineUp else KlineDown
        drawRect(
            color = color,
            topLeft = Offset(x - config.candleWidth / 3, top + indicatorHeight / 2 - barHeight),
            size = androidx.compose.ui.geometry.Size(config.candleWidth / 1.5f, abs(barHeight))
        )
    }

    // DIF线
    val difPath = Path()
    var difStarted = false
    for (i in 0 until visibleCount) {
        val dataIdx = startIndex + i
        val value = macd.dif.getOrNull(dataIdx) ?: continue
        val x = config.candleGap + i * step + config.candleWidth / 2
        val y = top + indicatorHeight / 2 - (value * scale).toFloat()
        if (!difStarted) { difPath.moveTo(x, y); difStarted = true }
        else difPath.lineTo(x, y)
    }
    drawPath(difPath, color = Color(0xFF2196F3), style = Stroke(1.5f))

    // DEA线
    val deaPath = Path()
    var deaStarted = false
    for (i in 0 until visibleCount) {
        val dataIdx = startIndex + i
        val value = macd.dea.getOrNull(dataIdx) ?: continue
        val x = config.candleGap + i * step + config.candleWidth / 2
        val y = top + indicatorHeight / 2 - (value * scale).toFloat()
        if (!deaStarted) { deaPath.moveTo(x, y); deaStarted = true }
        else deaPath.lineTo(x, y)
    }
    drawPath(deaPath, color = Color(0xFFFF9800), style = Stroke(1.5f))
}

private fun DrawScope.drawCrosshair(
    selectedIndex: Int,
    startIndex: Int,
    data: List<KlineData>,
    maxPrice: Double,
    minPrice: Double,
    priceRange: Double,
    chartHeight: Float,
    config: KlineConfig
) {
    val step = config.candleWidth + config.candleGap
    val idx = selectedIndex - startIndex
    if (idx !in data.indices) return

    val kline = data[selectedIndex]
    val x = config.candleGap + idx * step + config.candleWidth / 2
    val y = priceToY(kline.close, maxPrice, minPrice, priceRange, chartHeight)

    // 水平线
    drawLine(
        color = Color.Gray,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 1f
    )

    // 垂直线
    drawLine(
        color = Color.Gray,
        start = Offset(x, 0f),
        end = Offset(x, size.height),
        strokeWidth = 1f
    )

    // 价格标签
    drawContext.canvas.nativeCanvas.drawText(
        String.format("%.2f", kline.close),
        size.width - 80f,
        y - 8f,
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            isAntiAlias = true
        }
    )
}

private fun priceToY(
    price: Double,
    maxPrice: Double,
    minPrice: Double,
    priceRange: Double,
    chartHeight: Float
): Float {
    return ((maxPrice - price) / priceRange * chartHeight).toFloat()
}
