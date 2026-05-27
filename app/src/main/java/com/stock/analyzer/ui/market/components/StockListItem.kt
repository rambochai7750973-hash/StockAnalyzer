package com.stock.analyzer.ui.market.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stock.analyzer.data.model.Stock
import com.stock.analyzer.ui.theme.GreenUp
import com.stock.analyzer.ui.theme.RedDown

@Composable
fun StockListItem(
    stock: Stock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stock.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = stock.code,
                    color = androidx.compose.ui.graphics.Color.Gray,
                    fontSize = 12.sp
                )
            }
            Text(
                text = String.format("%.2f", stock.close),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val color = when {
                stock.changePercent > 0 -> GreenUp
                stock.changePercent < 0 -> RedDown
                else -> androidx.compose.ui.graphics.Color.Gray
            }
            Text(
                text = "${String.format("%.2f", stock.change)}",
                color = color,
                fontSize = 13.sp
            )
            Text(
                text = "${String.format("%.2f", stock.changePercent)}%",
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "量:${formatVolume(stock.volume)}",
                color = androidx.compose.ui.graphics.Color.Gray,
                fontSize = 12.sp
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

private fun formatVolume(volume: Long): String {
    return when {
        volume >= 10000 -> String.format("%.1f万", volume / 10000.0)
        else -> volume.toString()
    }
}
