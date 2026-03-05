package com.example.kalyansarathi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kalyansarathi.data.*
import com.example.kalyansarathi.repository.KalyanSarathiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: KalyanSarathiRepository
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    val chatSessions: StateFlow<List<ChatSession>> = repository.getAllChatSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == Language.ENGLISH) {
            Language.HINDI
        } else {
            Language.ENGLISH
        }
    }

    fun deleteChatSession(session: ChatSession) {
        viewModelScope.launch {
            repository.deleteChatSession(session)
            // Also delete associated messages
            repository.deleteMessagesByUserProfile(session.userProfileId)
        }
    }
}








