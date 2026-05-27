package com.stock.analyzer.domain.usecase

import com.stock.analyzer.data.model.KlineData
import com.stock.analyzer.domain.repository.StockRepository
import javax.inject.Inject

class GetKlineDataUseCase @Inject constructor(
    private val repository: StockRepository
) {
    suspend operator fun invoke(
        code: String,
        period: String = "day",
        count: Int = 200
    ): List<KlineData> = repository.getKlineData(code, period, count)
}
