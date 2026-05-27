package com.stock.analyzer.util

object Constants {
    const val SINA_BASE_URL = "https://hq.sinajs.cn/"
    const val TENCENT_BASE_URL = "https://qt.gtimg.cn/"
    const val DB_NAME = "stock_analyzer.db"
    const val CACHE_EXPIRY_HOURS = 2

    val SH_CODES = listOf("600519", "600036", "601318", "600900", "601166")
    val SZ_CODES = listOf("000001", "300750", "002415", "000858", "002475")
}
