package com.stock.analyzer.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.stock.analyzer.ui.detail.components.QuoteHeader
import com.stock.analyzer.ui.kline.KlineChart
import com.stock.analyzer.ui.kline.components.TimePeriodSelector
import com.stock.analyzer.ui.theme.GfRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    stockCode: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.stock?.name ?: stockCode,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleWatchlist) {
                        Icon(
                            if (uiState.isInWatchlist) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (uiState.isInWatchlist) "取消自选" else "添加自选",
                            tint = if (uiState.isInWatchlist) Color(0xFFFFC107) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GfRed
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(padding)
            )
        } else if (uiState.error != null) {
            Text(
                text = uiState.error ?: "加载失败",
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                uiState.stock?.let { stock ->
                    QuoteHeader(stock = stock)
                }

                TimePeriodSelector(
                    selected = uiState.selectedPeriod,
                    onPeriodSelected = viewModel::onPeriodChanged,
                    modifier = Modifier.padding(top = 8.dp)
                )

                KlineChart(
                    data = uiState.klineData,
                    showMa = true,
                    showMacd = true,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
