package com.example.kalyansarathi.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {
    
    private var tts: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized
    
    init {
        tts = TextToSpeech(context, this)
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _isInitialized.value = true
        }
    }
    
    fun speak(text: String, language: String = "en") {
        tts?.let { textToSpeech ->
            when (language) {
                "hi" -> textToSpeech.language = Locale("hi", "IN")
                else -> textToSpeech.language = Locale.US
            }
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    
    fun stop() {
        tts?.stop()
    }
    
    fun shutdown() {
        tts?.shutdown()
    }
}








