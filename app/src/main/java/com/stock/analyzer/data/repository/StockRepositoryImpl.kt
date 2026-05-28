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
                val allCodes = ALL_STOCK_CODES
                val batchSize = 30
                val allEntities = mutableListOf<StockEntity>()
                allCodes.chunked(batchSize).forEach { batch ->
                    try {
                        val raw = sinaApi.getQuotes(batch.joinToString(","))
                        val entities = parseSinaResponse(raw)
                        allEntities.addAll(entities)
                    } catch (_: Exception) { }
                }
                if (allEntities.isNotEmpty()) {
                    stockDao.deleteAll()
                    stockDao.insertStocks(allEntities)
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
        return MOCK_STOCK_DATA.map { (code, name, price) ->
            val change = (Math.random() - 0.5) * price * 0.06
            val close = price + change
            StockEntity(
                code = code, name = name,
                open = price - (Math.random() - 0.5) * price * 0.02,
                close = close,
                high = maxOf(close, price) + Math.random() * price * 0.015,
                low = minOf(close, price) - Math.random() * price * 0.015,
                preClose = price,
                volume = (1 + Math.random() * 50).toLong() * 1000000,
                amount = (1 + Math.random() * 50).toDouble() * 1e8,
                change = close - price,
                changePercent = if (price != 0.0) (close - price) / price * 100 else 0.0,
                timestamp = now
            )
        }
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

    companion object {
        val ALL_STOCK_CODES = listOf(
            "sh600519", "sz000001", "sh600036", "sz300750", "sh601318",
            "sh600900", "sz002415", "sh600276", "sz000858", "sh601166",
            "sh600887", "sh600030", "sh600585", "sz002714", "sz300059",
            "sh600809", "sh600104", "sh600690", "sz000651", "sh600028",
            "sh600019", "sh600031", "sh600048", "sz002304", "sz002475",
            "sh600309", "sh600703", "sh600436", "sz000568", "sz000333",
            "sh600941", "sz002230", "sh600760", "sz002459", "sz300274",
            "sh600438", "sh600010", "sh600050", "sz000063", "sh600895",
            "sz000100", "sh600196", "sz002129", "sh600570", "sh600362",
            "sz000002", "sz002236", "sh600893", "sh600011", "sz300124",
            "sh600089", "sh600150", "sh600660", "sz000538", "sz000625",
            "sh600022", "sh600029", "sz002142", "sh600837", "sz000725",
            "sh600085", "sz000157", "sh600547", "sz002007", "sh600406",
            "sh600690", "sz000301", "sh600036", "sh600958", "sz002601",
            "sz300015", "sh600588", "sz002271", "sh600298", "sz000596",
            "sz002001", "sh600176", "sz000858", "sz300433", "sh600183"
        ).distinct()

        val MOCK_STOCK_DATA = listOf(
            Triple("SH600519", "贵州茅台", 1895.0),
            Triple("SZ000001", "平安银行", 11.75),
            Triple("SH600036", "招商银行", 36.30),
            Triple("SZ300750", "宁德时代", 224.0),
            Triple("SH601318", "中国平安", 45.20),
            Triple("SH600900", "长江电力", 26.20),
            Triple("SZ002415", "海康威视", 35.30),
            Triple("SH600276", "恒瑞医药", 48.50),
            Triple("SZ000858", "五粮液", 146.5),
            Triple("SH601166", "兴业银行", 18.15),
            Triple("SH600887", "伊利股份", 28.50),
            Triple("SH600030", "中信证券", 22.30),
            Triple("SH600585", "海螺水泥", 32.80),
            Triple("SZ002714", "牧原股份", 68.50),
            Triple("SZ300059", "东方财富", 24.60),
            Triple("SH600809", "山西汾酒", 248.0),
            Triple("SH600104", "上汽集团", 19.80),
            Triple("SH600690", "海尔智家", 28.60),
            Triple("SZ000651", "格力电器", 42.30),
            Triple("SH600028", "中国石化", 6.85),
            Triple("SH600019", "宝钢股份", 7.20),
            Triple("SH600031", "三一重工", 32.50),
            Triple("SH600048", "保利发展", 15.80),
            Triple("SZ002304", "洋河股份", 142.0),
            Triple("SZ002475", "立讯精密", 38.50),
            Triple("SH600309", "万华化学", 92.50),
            Triple("SH600703", "三安光电", 24.30),
            Triple("SH600436", "片仔癀", 298.0),
            Triple("SZ000568", "泸州老窖", 198.0),
            Triple("SZ000333", "美的集团", 65.80),
            Triple("SH600941", "中国移动", 102.0),
            Triple("SZ002230", "科大讯飞", 62.30),
            Triple("SH600760", "中航沈飞", 62.80),
            Triple("SZ002459", "晶澳科技", 56.50),
            Triple("SZ300274", "阳光电源", 112.0),
            Triple("SH600438", "通威股份", 42.80),
            Triple("SH600010", "包钢股份", 2.35),
            Triple("SH600050", "中国联通", 5.20),
            Triple("SZ000063", "中兴通讯", 38.50),
            Triple("SH600895", "张江高科", 22.80),
            Triple("SZ000100", "TCL科技", 6.80),
            Triple("SH600196", "复星医药", 35.60),
            Triple("SZ002129", "中环股份", 45.80),
            Triple("SH600570", "恒生电子", 58.50),
            Triple("SH600362", "江西铜业", 22.50),
            Triple("SZ000002", "万科A", 14.50),
            Triple("SZ002236", "大华股份", 22.80),
            Triple("SH600893", "航发动力", 45.80),
            Triple("SH600011", "华能国际", 8.50),
            Triple("SZ300124", "汇川技术", 72.50),
            Triple("SH600089", "特变电工", 22.60),
            Triple("SH600150", "中国船舶", 35.80),
            Triple("SH600660", "福耀玻璃", 41.50),
            Triple("SZ000538", "云南白药", 68.50),
            Triple("SZ000625", "长安汽车", 18.80),
            Triple("SH600022", "山东钢铁", 1.85),
            Triple("SH600029", "南方航空", 6.80),
            Triple("SZ002142", "宁波银行", 28.50),
            Triple("SH600837", "海通证券", 12.30),
            Triple("SZ000725", "京东方A", 4.80),
            Triple("SH600085", "同仁堂", 55.60),
            Triple("SZ000157", "中联重科", 8.90),
            Triple("SH600547", "山东黄金", 28.80),
            Triple("SZ002007", "华兰生物", 28.50),
            Triple("SH600406", "国电南瑞", 28.60),
            Triple("SZ000301", "东方盛虹", 15.80),
            Triple("SH600958", "东方证券", 12.80),
            Triple("SZ002601", "龙佰集团", 22.50),
            Triple("SZ300015", "爱尔眼科", 35.80),
            Triple("SH600588", "用友网络", 25.60),
            Triple("SZ002271", "东方雨虹", 32.50),
            Triple("SH600298", "安琪酵母", 38.50),
            Triple("SZ000596", "古井贡酒", 268.0),
            Triple("SZ002001", "新和成", 22.60),
            Triple("SH600176", "中国巨石", 15.80),
            Triple("SZ300433", "蓝思科技", 18.50),
            Triple("SH600183", "生益科技", 22.30),
        )
    }
}
