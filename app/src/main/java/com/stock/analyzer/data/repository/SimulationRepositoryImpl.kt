package com.stock.analyzer.data.repository

import com.stock.analyzer.data.local.dao.AccountDao
import com.stock.analyzer.data.local.dao.PortfolioDao
import com.stock.analyzer.data.local.dao.TradeDao
import com.stock.analyzer.data.local.entity.AccountEntity
import com.stock.analyzer.data.local.entity.PortfolioEntity
import com.stock.analyzer.data.local.entity.TradeEntity
import com.stock.analyzer.data.model.Stock
import com.stock.analyzer.domain.model.AccountInfo
import com.stock.analyzer.domain.model.PortfolioStock
import com.stock.analyzer.domain.model.TradeRecord
import com.stock.analyzer.domain.repository.SimulationRepository
import com.stock.analyzer.domain.repository.StockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimulationRepositoryImpl @Inject constructor(
    private val portfolioDao: PortfolioDao,
    private val tradeDao: TradeDao,
    private val accountDao: AccountDao,
    private val stockRepository: StockRepository
) : SimulationRepository {

    override fun getAllHoldings(): Flow<List<PortfolioStock>> {
        return combine(
            portfolioDao.getAllHoldings(),
            stockRepository.getStocks()
        ) { holdings, stocks ->
            val priceMap = stocks.associate { it.code to it.close }
            holdings.map { holding ->
                val price = priceMap[holding.code] ?: 0.0
                val marketValue = price * holding.shares
                PortfolioStock(
                    code = holding.code,
                    name = holding.name,
                    shares = holding.shares,
                    avgCost = holding.avgCost,
                    totalInvested = holding.totalInvested,
                    currentPrice = price,
                    marketValue = marketValue,
                    profitLoss = marketValue - holding.totalInvested,
                    profitLossPercent = if (holding.totalInvested > 0)
                        (marketValue - holding.totalInvested) / holding.totalInvested * 100 else 0.0
                )
            }.sortedByDescending { it.marketValue }
        }
    }

    override fun getAllTrades(): Flow<List<TradeRecord>> {
        return tradeDao.getAllTrades().map { trades ->
            trades.map { it.toDomain() }
        }
    }

    override fun getAccount(): Flow<AccountInfo> {
        return combine(
            accountDao.getAccount(),
            getAllHoldings()
        ) { account, holdings ->
            val balance = account?.balance ?: 100000.0
            val totalInvested = holdings.sumOf { it.totalInvested }
            val marketValue = holdings.sumOf { it.marketValue }
            AccountInfo(
                balance = balance,
                totalInvested = totalInvested,
                marketValue = marketValue,
                totalProfitLoss = marketValue + balance - (totalInvested + (account?.balance ?: 100000.0))
            )
        }
    }

    override suspend fun buyStock(
        code: String, name: String, price: Double, shares: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val total = price * shares
            val account = accountDao.getAccountOnce() ?: AccountEntity(balance = 100000.0)
            if (account.balance < total) {
                return@withContext Result.failure(Exception("余额不足，需要 ${String.format("%.2f", total)}，可用 ${String.format("%.2f", account.balance)}"))
            }
            accountDao.saveAccount(account.copy(balance = account.balance - total))
            val existing = portfolioDao.getHolding(code)
            if (existing != null) {
                val newShares = existing.shares + shares
                val newCost = existing.totalInvested + total
                portfolioDao.upsertHolding(existing.copy(
                    shares = newShares,
                    avgCost = newCost / newShares,
                    totalInvested = newCost
                ))
            } else {
                portfolioDao.upsertHolding(PortfolioEntity(
                    code = code, name = name,
                    shares = shares, avgCost = price,
                    totalInvested = total
                ))
            }
            tradeDao.insertTrade(TradeEntity(
                code = code, name = name,
                type = "买入", price = price,
                shares = shares, total = total
            ))
            Result.success("买入成功 ${name} ${shares}股")
        } catch (e: Exception) {
            Result.failure(Exception("买入失败: ${e.message}"))
        }
    }

    override suspend fun sellStock(
        code: String, price: Double, shares: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val holding = portfolioDao.getHolding(code)
                ?: return@withContext Result.failure(Exception("未持有该股票"))
            if (holding.shares < shares) {
                return@withContext Result.failure(Exception("持仓不足，可用 ${holding.shares}股"))
            }
            val total = price * shares
            val account = accountDao.getAccountOnce() ?: AccountEntity(balance = 100000.0)
            accountDao.saveAccount(account.copy(balance = account.balance + total))
            val remaining = holding.shares - shares
            if (remaining == 0) {
                portfolioDao.removeHolding(code)
            } else {
                val soldRatio = shares.toDouble() / holding.shares
                val costSold = holding.totalInvested * soldRatio
                portfolioDao.upsertHolding(holding.copy(
                    shares = remaining,
                    totalInvested = holding.totalInvested - costSold,
                    avgCost = (holding.totalInvested - costSold) / remaining
                ))
            }
            tradeDao.insertTrade(TradeEntity(
                code = code, name = holding.name,
                type = "卖出", price = price,
                shares = shares, total = total
            ))
            Result.success("卖出成功 ${holding.name} ${shares}股")
        } catch (e: Exception) {
            Result.failure(Exception("卖出失败: ${e.message}"))
        }
    }

    override suspend fun addFunds(amount: Double) = withContext(Dispatchers.IO) {
        val account = accountDao.getAccountOnce() ?: AccountEntity(balance = 100000.0)
        accountDao.saveAccount(account.copy(balance = account.balance + amount))
    }

    private fun TradeEntity.toDomain() = TradeRecord(
        id = id, code = code, name = name,
        type = type, price = price, shares = shares,
        total = total, timestamp = timestamp
    )
}
