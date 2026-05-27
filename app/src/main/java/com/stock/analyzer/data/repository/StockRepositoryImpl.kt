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
                stockDao.deleteAll()
                stockDao.insertStocks(entities)
            } catch (_: Exception) {
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
            parseKlineResponse(raw)
        } catch (_: Exception) {
            emptyList()
        }
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

    private fun StockEntity.toDomain() = Stock(
        code = code, name = name, open = open, high = high,
        low = low, close = close, preClose = preClose,
        volume = volume, amount = amount,
        change = change, changePercent = changePercent,
        timestamp = timestamp
    )
}
