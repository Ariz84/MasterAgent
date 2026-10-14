package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isUser: Boolean,
    val text: String,
    val agentType: String = "MASTER",
    val agentName: String = "Master Agent",
    val statusTag: String? = null,
    val actionType: String? = null, // "WHATSAPP", "SMS", "EMAIL", "TRADING_CALC", "CODE_SNIPPET"
    val actionPayload: String? = null
)
