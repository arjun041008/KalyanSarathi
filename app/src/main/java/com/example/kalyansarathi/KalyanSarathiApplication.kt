package com.example.kalyansarathi

import android.app.Application
import com.example.kalyansarathi.data.KalyanSarathiDatabase
import com.example.kalyansarathi.repository.KalyanSarathiRepository

class KalyanSarathiApplication : Application() {
    
    val database by lazy { KalyanSarathiDatabase.getDatabase(this) }
    val repository by lazy { 
        KalyanSarathiRepository(
            database.userProfileDao(),
            database.chatMessageDao(),
            database.chatSessionDao()
        )
    }
}








