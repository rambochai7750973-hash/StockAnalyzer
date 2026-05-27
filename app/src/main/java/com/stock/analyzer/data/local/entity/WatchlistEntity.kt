package com.stock.analyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val code: String,
    val name: String = "",
    val groupName: String = "默认",
    val addedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
