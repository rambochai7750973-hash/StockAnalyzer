package com.stock.analyzer.ui.navigation

sealed class Screen(val route: String) {
    data object Market : Screen("market")
    data object Watchlist : Screen("watchlist")
    data object Detail : Screen("detail/{code}") {
        fun createRoute(code: String) = "detail/$code"
    }
}
