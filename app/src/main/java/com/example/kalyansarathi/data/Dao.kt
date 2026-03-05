package com.example.kalyansarathi.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles ORDER BY timestamp DESC")
    fun getAllUserProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getUserProfileById(id: Long): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile): Long

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    @Delete
    suspend fun deleteUserProfile(userProfile: UserProfile)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE userProfileId = :userProfileId ORDER BY timestamp ASC")
    fun getMessagesByUserProfile(userProfileId: Long): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE userProfileId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesBySessionId(sessionId: Long): List<ChatMessage>

    @Insert
    suspend fun insertMessage(message: ChatMessage): Long

    @Delete
    suspend fun deleteMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE userProfileId = :userProfileId")
    suspend fun deleteMessagesByUserProfile(userProfileId: Long)
}

@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllChatSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE id = :id")
    suspend fun getChatSessionById(id: Long): ChatSession?

    @Insert
    suspend fun insertChatSession(session: ChatSession): Long

    @Delete
    suspend fun deleteChatSession(session: ChatSession)

    @Query("DELETE FROM chat_sessions WHERE userProfileId = :userProfileId")
    suspend fun deleteSessionsByUserProfile(userProfileId: Long)
}








