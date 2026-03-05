package com.example.kalyansarathi.repository

import com.example.kalyansarathi.data.*
import kotlinx.coroutines.flow.Flow

class KalyanSarathiRepository(
    private val userProfileDao: UserProfileDao,
    private val chatMessageDao: ChatMessageDao,
    private val chatSessionDao: ChatSessionDao
) {
    // User Profile operations
    suspend fun insertUserProfile(userProfile: UserProfile): Long {
        return userProfileDao.insertUserProfile(userProfile)
    }

    suspend fun getUserProfileById(id: Long): UserProfile? {
        return userProfileDao.getUserProfileById(id)
    }

    fun getAllUserProfiles(): Flow<List<UserProfile>> {
        return userProfileDao.getAllUserProfiles()
    }

    // Chat Message operations
    suspend fun insertMessage(message: ChatMessage): Long {
        return chatMessageDao.insertMessage(message)
    }

    suspend fun insertChatMessage(message: ChatMessage): Long {
        return chatMessageDao.insertMessage(message)
    }

    fun getMessagesByUserProfile(userProfileId: Long): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesByUserProfile(userProfileId)
    }

    suspend fun getChatMessagesBySessionId(sessionId: Long): List<ChatMessage> {
        return chatMessageDao.getMessagesBySessionId(sessionId)
    }

    suspend fun deleteMessagesByUserProfile(userProfileId: Long) {
        chatMessageDao.deleteMessagesByUserProfile(userProfileId)
    }

    // Chat Session operations
    suspend fun insertChatSession(session: ChatSession): Long {
        return chatSessionDao.insertChatSession(session)
    }

    fun getAllChatSessions(): Flow<List<ChatSession>> {
        return chatSessionDao.getAllChatSessions()
    }

    suspend fun deleteChatSession(session: ChatSession) {
        chatSessionDao.deleteChatSession(session)
    }
}
