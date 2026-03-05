package com.example.kalyansarathi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userProfileId: Long,
    val message: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val language: Language = Language.ENGLISH
)

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userProfileId: Long,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: Language = Language.ENGLISH
)








