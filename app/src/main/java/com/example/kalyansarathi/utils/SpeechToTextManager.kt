package com.example.kalyansarathi.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class SpeechToTextManager(private val context: Context) : RecognitionListener {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening
    
    private val _recognitionResult = MutableStateFlow<String?>(null)
    val recognitionResult: StateFlow<String?> = _recognitionResult
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun startListening(language: String = "en") {
        if (!hasPermission()) {
            _error.value = "Microphone permission required"
            return
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(this)
        
        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        speechRecognizer?.startListening(intent)
        _isListening.value = true
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }
    
    fun cancelListening() {
        speechRecognizer?.cancel()
        _isListening.value = false
    }
    
    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onReadyForSpeech(params: android.os.Bundle?) {
        _error.value = null
    }
    
    override fun onBeginningOfSpeech() {
        // Speech started
    }
    
    override fun onRmsChanged(rmsdB: Float) {
        // Volume level changed
    }
    
    override fun onBufferReceived(buffer: ByteArray?) {
        // Buffer received
    }
    
    override fun onEndOfSpeech() {
        _isListening.value = false
    }
    
    override fun onError(error: Int) {
        _isListening.value = false
        _error.value = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error occurred"
        }
    }
    
    override fun onResults(results: android.os.Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _recognitionResult.value = matches[0]
        }
        _isListening.value = false
    }
    
    override fun onPartialResults(partialResults: android.os.Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _recognitionResult.value = matches[0]
        }
    }
    
    override fun onEvent(eventType: Int, params: android.os.Bundle?) {
        // Event occurred
    }
    
    fun destroy() {
        speechRecognizer?.destroy()
    }
}








