package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TradeJournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeJournalDao {
    @Query("SELECT * FROM trade_journal ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeJournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeJournalEntity): Long

    @Update
    suspend fun updateTrade(trade: TradeJournalEntity)

    @Delete
    suspend fun deleteTrade(trade: TradeJournalEntity)

    @Query("SELECT COUNT(*) FROM trade_journal")
    suspend fun getTradeCount(): Int

    @Query("SELECT SUM(profitPnlUsd) FROM trade_journal WHERE status = 'WIN' OR status = 'LOSS'")
    suspend fun getTotalPnl(): Double?
}
