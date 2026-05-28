package com.stock.analyzer.ui.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stock.analyzer.domain.model.PortfolioStock
import com.stock.analyzer.domain.model.TradeRecord
import com.stock.analyzer.ui.theme.GfGold
import com.stock.analyzer.ui.theme.GfRed
import com.stock.analyzer.ui.theme.GreenUp
import com.stock.analyzer.ui.theme.RedDown
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(
    onStockClick: (String) -> Unit,
    viewModel: SimulationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        TopAppBar(
            title = { Text("模拟交易", fontWeight = FontWeight.Bold, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = GfRed)
        )

        AccountSummaryCard(state, viewModel::addFunds)

        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = Color.White,
            contentColor = GfRed,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                    color = GfRed
                )
            }
        ) {
            Tab(selected = state.selectedTab == 0, onClick = { viewModel.selectTab(0) },
                text = { Text("持仓 (${state.holdings.size})", fontWeight = if (state.selectedTab == 0) FontWeight.Bold else FontWeight.Normal) })
            Tab(selected = state.selectedTab == 1, onClick = { viewModel.selectTab(1) },
                text = { Text("交易记录", fontWeight = if (state.selectedTab == 1) FontWeight.Bold else FontWeight.Normal) })
        }

        when (state.selectedTab) {
            0 -> HoldingsList(state.holdings, viewModel, onStockClick)
            1 -> TradesList(state.trades)
        }
    }

    state.message?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            title = { Text("提示") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessage() }) { Text("确定") }
            }
        )
    }

    if (state.showBuyDialog) {
        TradeDialog(
            title = "买入 ${state.buyCode}",
            price = state.buyPrice,
            shares = state.buyShares,
            onSharesChange = viewModel::updateBuyShares,
            onConfirm = viewModel::executeBuy,
            onDismiss = viewModel::hideDialogs,
            isLoading = state.isLoading,
            isBuy = true
        )
    }

    if (state.showSellDialog) {
        TradeDialog(
            title = "卖出 ${state.sellCode}",
            price = state.sellPrice,
            shares = state.sellShares,
            onSharesChange = viewModel::updateSellShares,
            onConfirm = viewModel::executeSell,
            onDismiss = viewModel::hideDialogs,
            isLoading = state.isLoading,
            isBuy = false
        )
    }
}

@Composable
private fun AccountSummaryCard(state: SimulationUiState, onAddFunds: () -> Unit) {
    val account = state.account
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("总资产", fontSize = 13.sp, color = Color.Gray)
                    Text(
                        text = String.format("%.2f", account.balance + account.marketValue),
                        fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                }
                Button(
                    onClick = onAddFunds,
                    colors = ButtonDefaults.buttonColors(containerColor = GfGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("充值", fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                InfoItem("可用", String.format("%.2f", account.balance), Color.Gray)
                InfoItem("持仓市值", String.format("%.2f", account.marketValue), Color(0xFF1A1C1E))
                val plColor = when {
                    account.totalProfitLoss > 0 -> GreenUp
                    account.totalProfitLoss < 0 -> RedDown
                    else -> Color.Gray
                }
                InfoItem("总盈亏", String.format("%.2f", account.totalProfitLoss), plColor)
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun HoldingsList(
    holdings: List<PortfolioStock>,
    viewModel: SimulationViewModel,
    onStockClick: (String) -> Unit
) {
    if (holdings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无持仓\n在行情页点击股票买入", color = Color.Gray, fontSize = 14.sp,
                textAlign = TextAlign.Center, lineHeight = 22.sp)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        items(holdings, key = { it.code }) { holding ->
            HoldingItem(holding, viewModel, onStockClick)
        }
    }
}

@Composable
private fun HoldingItem(
    holding: PortfolioStock,
    viewModel: SimulationViewModel,
    onStockClick: (String) -> Unit
) {
    val plColor = when {
        holding.profitLoss > 0 -> GreenUp
        holding.profitLoss < 0 -> RedDown
        else -> Color.Gray
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onStockClick(holding.code) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(holding.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(holding.code, color = Color.Gray, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("%.2f", holding.currentPrice),
                        fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                    Text(
                        text = "${String.format("%.2f", holding.profitLossPercent)}%",
                        color = plColor, fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("持仓 ${holding.shares}股", fontSize = 13.sp, color = Color.Gray)
                Text(
                    text = "盈亏 ${String.format("%.2f", holding.profitLoss)}",
                    fontSize = 13.sp, color = plColor, fontWeight = FontWeight.Medium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val priceStr = String.format("%.2f", holding.currentPrice)
                Button(
                    onClick = { viewModel.showBuyDialog(holding.code, priceStr) },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDown),
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("买入", color = Color.White, fontSize = 13.sp)
                }
                Button(
                    onClick = { viewModel.showSellDialog(holding.code, priceStr) },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenUp),
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("卖出", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun TradesList(trades: List<TradeRecord>) {
    if (trades.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无交易记录", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        items(trades, key = { it.id }) { trade ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(trade.name, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    Text(trade.code, color = Color.Gray, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${trade.type} ${trade.shares}股",
                        color = if (trade.type == "买入") RedDown else GreenUp,
                        fontWeight = FontWeight.Bold, fontSize = 14.sp
                    )
                    Text(
                        text = "${String.format("%.2f", trade.price)} × ${trade.shares}",
                        color = Color.Gray, fontSize = 12.sp
                    )
                }
                Text(
                    text = dateFormat.format(Date(trade.timestamp)),
                    color = Color.Gray, fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun TradeDialog(
    title: String,
    price: String,
    shares: String,
    onSharesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    isBuy: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("当前价格: ${price}", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = shares,
                    onValueChange = { if (it.all { c -> c.isDigit() }) onSharesChange(it) },
                    label = { Text("数量（股）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val total = (price.toDoubleOrNull() ?: 0.0) * (shares.toIntOrNull() ?: 0)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "金额: ${String.format("%.2f", total)}",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading && shares.toIntOrNull()?.let { it > 0 } == true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBuy) RedDown else GreenUp
                )
            ) { Text(if (isBuy) "确认买入" else "确认卖出", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
