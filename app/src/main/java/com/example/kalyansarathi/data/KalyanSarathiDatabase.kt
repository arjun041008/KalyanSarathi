package com.example.kalyansarathi.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [UserProfile::class, ChatMessage::class, ChatSession::class],
    version = 1,
    exportSchema = false
)
abstract class KalyanSarathiDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun chatSessionDao(): ChatSessionDao

    companion object {
        @Volatile
        private var INSTANCE: KalyanSarathiDatabase? = null

        fun getDatabase(context: Context): KalyanSarathiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KalyanSarathiDatabase::class.java,
                    "kalyan_sarathi_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}








