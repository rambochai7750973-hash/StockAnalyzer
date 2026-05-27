package com.stock.analyzer.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stock.analyzer.data.model.Stock
import com.stock.analyzer.ui.theme.GreenUp
import com.stock.analyzer.ui.theme.RedDown

@Composable
fun QuoteHeader(
    stock: Stock,
    modifier: Modifier = Modifier
) {
    val color = when {
        stock.changePercent > 0 -> GreenUp
        stock.changePercent < 0 -> RedDown
        else -> androidx.compose.ui.graphics.Color.Gray
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(androidx.compose.ui.graphics.Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stock.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = stock.code,
                    color = androidx.compose.ui.graphics.Color.Gray,
                    fontSize = 14.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.2f", stock.close),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = color
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${String.format("%.2f", stock.change)}",
                        fontSize = 14.sp,
                        color = color
                    )
                    Text(
                        text = "${String.format("%.2f", stock.changePercent)}%",
                        fontSize = 14.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuoteItem("开盘", stock.open)
            QuoteItem("最高", stock.high)
            QuoteItem("最低", stock.low)
            QuoteItem("昨收", stock.preClose)
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuoteItem("成交量", stock.volume.toDouble(), isVolume = true)
            QuoteItem("成交额", stock.amount, isVolume = true)
        }
    }
}

@Composable
private fun QuoteItem(label: String, value: Double, isVolume: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = androidx.compose.ui.graphics.Color.Gray
        )
        Text(
            text = if (isVolume && value >= 10000) {
                String.format("%.1f万", value / 10000)
            } else {
                String.format("%.2f", value)
            },
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
