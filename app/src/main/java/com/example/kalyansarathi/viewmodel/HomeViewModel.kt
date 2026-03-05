package com.example.kalyansarathi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kalyansarathi.data.*
import com.example.kalyansarathi.repository.KalyanSarathiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: KalyanSarathiRepository
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == Language.ENGLISH) {
            Language.HINDI
        } else {
            Language.ENGLISH
        }
    }

    fun getGreetingMessage(): String {
        return when (_currentLanguage.value) {
            Language.ENGLISH -> "Welcome to KalyanSarathi! Discover government schemes you're eligible for."
            Language.HINDI -> "कल्याणसारथी में आपका स्वागत है! अपने लिए उपलब्ध सरकारी योजनाओं की खोज करें।"
        }
    }

    fun getStartButtonText(): String {
        return when (_currentLanguage.value) {
            Language.ENGLISH -> "Start Chat"
            Language.HINDI -> "चैट शुरू करें"
        }
    }

    fun getHistoryButtonText(): String {
        return when (_currentLanguage.value) {
            Language.ENGLISH -> "Chat History"
            Language.HINDI -> "चैट इतिहास"
        }
    }
}








