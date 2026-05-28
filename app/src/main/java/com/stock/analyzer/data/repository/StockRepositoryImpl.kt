package com.stock.analyzer.data.repository

import com.stock.analyzer.data.local.dao.StockDao
import com.stock.analyzer.data.local.entity.StockEntity
import com.stock.analyzer.data.model.KlineData
import com.stock.analyzer.data.model.Stock
import com.stock.analyzer.data.remote.SinaApi
import com.stock.analyzer.data.remote.TencentApi
import com.stock.analyzer.domain.repository.StockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val stockDao: StockDao,
    private val sinaApi: SinaApi,
    private val tencentApi: TencentApi
) : StockRepository {

    override fun getStocks(): Flow<List<Stock>> {
        return stockDao.getAllStocks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchStocks(query: String): Flow<List<Stock>> {
        return stockDao.searchStocks(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshStocks() {
        withContext(Dispatchers.IO) {
            try {
                val codes = listOf(
                    "sh600519", "sz000001", "sh600036", "sz300750",
                    "sh601318", "sh600900", "sz002415", "sh600276",
                    "sz000858", "sh601166"
                )
                val raw = sinaApi.getQuotes(codes.joinToString(","))
                val entities = parseSinaResponse(raw)
                if (entities.isNotEmpty()) {
                    stockDao.deleteAll()
                    stockDao.insertStocks(entities)
                } else {
                    seedMockData()
                }
            } catch (_: Exception) {
                seedMockData()
            }
        }
    }

    override suspend fun getStockByCode(code: String): Stock? {
        return stockDao.getStockByCode(code)?.toDomain()
    }

    override suspend fun getKlineData(
        code: String,
        period: String,
        count: Int
    ): List<KlineData> = withContext(Dispatchers.IO) {
        try {
            val raw = tencentApi.getKline(code)
            val parsed = parseKlineResponse(raw)
            if (parsed.isNotEmpty()) parsed else mockKlineData()
        } catch (_: Exception) {
            mockKlineData()
        }
    }

    private suspend fun seedMockData() {
        if (stockDao.getCount() > 0) return
        stockDao.insertStocks(mockStocks())
    }

    private fun parseSinaResponse(raw: String): List<StockEntity> {
        return raw.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            try {
                val match = Regex("""hq_str_(\w+)="(.*)"""").find(line)
                val (code, data) = match?.destructured ?: return@mapNotNull null
                val parts = data.split(",")
                if (parts.size < 30) return@mapNotNull null
                val open = parts[1].toDoubleOrNull() ?: 0.0
                val preClose = parts[2].toDoubleOrNull() ?: 0.0
                val close = parts[3].toDoubleOrNull() ?: 0.0
                StockEntity(
                    code = code.uppercase(),
                    name = parts[0],
                    open = open,
                    close = close,
                    high = parts[4].toDoubleOrNull() ?: 0.0,
                    low = parts[5].toDoubleOrNull() ?: 0.0,
                    preClose = preClose,
                    volume = parts[8].toLongOrNull() ?: 0,
                    amount = parts[9].toDoubleOrNull() ?: 0.0,
                    change = close - preClose,
                    changePercent = if (preClose != 0.0) (close - preClose) / preClose * 100 else 0.0
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun parseKlineResponse(raw: String): List<KlineData> {
        return try {
            val match = Regex("""""(.*)"""").find(raw)
            val data = match?.groupValues?.getOrNull(1) ?: return emptyList()
            data.split(";").filter { it.isNotBlank() }.map { item ->
                val p = item.split(",")
                if (p.size < 7) return@map null
                KlineData(
                    date = p[0],
                    open = p[1].toDoubleOrNull() ?: 0.0,
                    close = p[2].toDoubleOrNull() ?: 0.0,
                    high = p[3].toDoubleOrNull() ?: 0.0,
                    low = p[4].toDoubleOrNull() ?: 0.0,
                    volume = p[5].toLongOrNull() ?: 0,
                    amount = p[6].toDoubleOrNull() ?: 0.0
                )
            }.filterNotNull()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun mockStocks(): List<StockEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            StockEntity("SH600519", "贵州茅台", 1880.0, 1900.0, 1870.0, 1895.0, 1885.0, 3500000, 6.6e9, 10.0, 0.53, now),
            StockEntity("SZ000001", "平安银行", 11.5, 11.8, 11.4, 11.75, 11.5, 85000000, 9.8e8, 0.25, 2.17, now),
            StockEntity("SH600036", "招商银行", 36.0, 36.5, 35.8, 36.3, 36.0, 42000000, 1.5e9, 0.3, 0.83, now),
            StockEntity("SZ300750", "宁德时代", 220.0, 225.0, 218.0, 224.0, 220.0, 28000000, 6.2e9, 4.0, 1.82, now),
            StockEntity("SH601318", "中国平安", 45.0, 45.5, 44.8, 45.2, 45.0, 55000000, 2.5e9, 0.2, 0.44, now),
            StockEntity("SH600900", "长江电力", 26.0, 26.3, 25.9, 26.2, 26.0, 38000000, 9.9e8, 0.2, 0.77, now),
            StockEntity("SZ002415", "海康威视", 35.0, 35.5, 34.8, 35.3, 35.0, 22000000, 7.8e8, 0.3, 0.86, now),
            StockEntity("SH600276", "恒瑞医药", 48.0, 48.8, 47.5, 48.5, 48.0, 18000000, 8.7e8, 0.5, 1.04, now),
            StockEntity("SZ000858", "五粮液", 145.0, 147.0, 144.0, 146.5, 145.0, 15000000, 2.2e9, 1.5, 1.03, now),
            StockEntity("SH601166", "兴业银行", 18.0, 18.2, 17.9, 18.15, 18.0, 62000000, 1.1e9, 0.15, 0.83, now),
        )
    }

    private fun mockKlineData(): List<KlineData> {
        val data = mutableListOf<KlineData>()
        var price = 1800.0
        val now = System.currentTimeMillis()
        for (i in 0 until 100) {
            val change = (Math.random() - 0.5) * 40
            val open = price
            val close = price + change
            val high = maxOf(open, close) + Math.random() * 10
            val low = minOf(open, close) - Math.random() * 10
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
                .format(java.util.Date(now - (99 - i) * 86400000L))
            data.add(KlineData(date, open, high, low, close, (1 + Math.random() * 10).toLong() * 1000000, (1 + Math.random() * 10).toDouble() * 1e8))
            price = close
        }
        return data
    }

    private fun StockEntity.toDomain() = Stock(
        code = code, name = name, open = open, high = high,
        low = low, close = close, preClose = preClose,
        volume = volume, amount = amount,
        change = change, changePercent = changePercent,
        timestamp = timestamp
    )
}
