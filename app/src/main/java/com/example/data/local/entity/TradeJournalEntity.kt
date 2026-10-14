package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_journal")
data class TradeJournalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val pair: String, // e.g. "EUR/USD", "GBP/USD", "XAU/USD"
    val direction: String, // "BUY" or "SELL"
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val lotSize: Double,
    val riskAmountUsd: Double,
    val status: String, // "OPEN", "WIN", "LOSS", "CANCELLED"
    val profitPnlUsd: Double = 0.0,
    val platform: String = "Forex MT5/MT4", // "Forex MT5/MT4", "Quotex Demo", "Pocket Option Demo"
    val strategyNotes: String = ""
)
