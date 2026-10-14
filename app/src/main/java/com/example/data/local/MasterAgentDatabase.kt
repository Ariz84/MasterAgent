package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.TradeJournalDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.TradeJournalEntity

@Database(
    entities = [
        ChatMessageEntity::class,
        TradeJournalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MasterAgentDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun tradeJournalDao(): TradeJournalDao

    companion object {
        @Volatile
        private var INSTANCE: MasterAgentDatabase? = null

        fun getDatabase(context: Context): MasterAgentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MasterAgentDatabase::class.java,
                    "master_agent_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
