package com.example.kalyansarathi

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kalyansarathi.data.*
import com.example.kalyansarathi.network.NetworkModule
import com.example.kalyansarathi.repository.KalyanSarathiRepository
import com.example.kalyansarathi.ui.theme.KalyanSarathiTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.*
import java.util.regex.Pattern

// Markdown parsing utility functions
fun parseMarkdownToAnnotatedString(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var currentText = text
        
        // First, process links [text](url) - do this first to avoid conflicts
        val linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        val linkMatcher = linkPattern.matcher(currentText)
        
        if (linkMatcher.find()) {
            // Found links, process them
            var lastEnd = 0
            linkMatcher.reset() // Reset to start from beginning
            
            while (linkMatcher.find()) {
                // Add text before the link
                if (linkMatcher.start() > lastEnd) {
                    val textBeforeLink = currentText.substring(lastEnd, linkMatcher.start())
                    processBoldText(textBeforeLink)
                }
                
                // Add link text with clickable annotation
                val linkText = linkMatcher.group(1)
                val linkUrl = linkMatcher.group(2)
                
                pushStringAnnotation(
                    tag = "URL",
                    annotation = linkUrl ?: ""
                )
                withStyle(style = SpanStyle(
                    color = androidx.compose.ui.graphics.Color.Blue,
                    textDecoration = TextDecoration.Underline
                )) {
                    append(linkText ?: "")
                }
                pop()
                
                lastEnd = linkMatcher.end()
            }
            
            // Add remaining text after last link
            if (lastEnd < currentText.length) {
                val remainingText = currentText.substring(lastEnd)
                processBoldText(remainingText)
            }
        } else {
            // No links found, just process bold text
            processBoldText(currentText)
        }
    }
}

// Helper function to process bold text
fun androidx.compose.ui.text.AnnotatedString.Builder.processBoldText(text: String) {
    val boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*|__(.*?)__")
    val boldMatcher = boldPattern.matcher(text)
    
    var lastEnd = 0
    while (boldMatcher.find()) {
        // Add text before the bold section
        if (boldMatcher.start() > lastEnd) {
            append(text.substring(lastEnd, boldMatcher.start()))
        }
        
        // Add bold text
        val boldText = boldMatcher.group(1) ?: boldMatcher.group(2)
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(boldText ?: "")
        }
        
        lastEnd = boldMatcher.end()
    }
    
    // Add remaining text
    if (lastEnd < text.length) {
        append(text.substring(lastEnd))
    }
}

// Hindi state names mapping
val hindiStateNames = mapOf(
    "Andhra Pradesh" to "आंध्र प्रदेश",
    "Arunachal Pradesh" to "अरुणाचल प्रदेश",
    "Assam" to "असम",
    "Bihar" to "बिहार",
    "Chhattisgarh" to "छत्तीसगढ़",
    "Goa" to "गोवा",
    "Gujarat" to "गुजरात",
    "Haryana" to "हरियाणा",
    "Himachal Pradesh" to "हिमाचल प्रदेश",
    "Jharkhand" to "झारखंड",
    "Karnataka" to "कर्नाटक",
    "Kerala" to "केरल",
    "Madhya Pradesh" to "मध्य प्रदेश",
    "Maharashtra" to "महाराष्ट्र",
    "Manipur" to "मणिपुर",
    "Meghalaya" to "मेघालय",
    "Mizoram" to "मिजोरम",
    "Nagaland" to "नागालैंड",
    "Odisha" to "ओडिशा",
    "Punjab" to "पंजाब",
    "Rajasthan" to "राजस्थान",
    "Sikkim" to "सिक्किम",
    "Tamil Nadu" to "तमिलनाडु",
    "Telangana" to "तेलंगाना",
    "Tripura" to "त्रिपुरा",
    "Uttar Pradesh" to "उत्तर प्रदेश",
    "Uttarakhand" to "उत्तराखंड",
    "West Bengal" to "पश्चिम बंगाल",
    "Delhi" to "दिल्ली",
    "Jammu and Kashmir" to "जम्मू और कश्मीर",
    "Ladakh" to "लद्दाख",
    "Puducherry" to "पुडुचेरी",
    "Chandigarh" to "चंडीगढ़",
    "Dadra and Nagar Haveli and Daman and Diu" to "दादरा और नगर हवेली और दमन और दीव",
    "Lakshadweep" to "लक्षद्वीप",
    "Andaman and Nicobar Islands" to "अंडमान और निकोबार द्वीप समूह"
)

fun convertStatesToHindi(text: String): String {
    var result = text
    hindiStateNames.forEach { (english, hindi) ->
        result = result.replace(english, hindi)
    }
    return result
}

fun convertHindiToEnglishState(hindiState: String): String {
    // Create reverse mapping from Hindi to English
    val englishStateNames = hindiStateNames.entries.associate { (english, hindi) -> hindi to english }
    return englishStateNames[hindiState] ?: hindiState
}

class MainActivity : ComponentActivity() {
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    
    // Helper function to split text into manageable chunks
    private fun splitTextIntoChunks(text: String): List<String> {
        val maxChunkSize = 200 // Characters per chunk for better TTS performance
        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
        val chunks = mutableListOf<String>()
        var currentChunk = ""
        
        for (sentence in sentences) {
            if (currentChunk.length + sentence.length > maxChunkSize && currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.trim())
                currentChunk = sentence
            } else {
                currentChunk += if (currentChunk.isEmpty()) sentence else " $sentence"
            }
        }
        
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.trim())
        }
        
        // If no sentences found, split by words
        if (chunks.isEmpty()) {
            val words = text.split(" ")
            var currentWordChunk = ""
            
            for (word in words) {
                if (currentWordChunk.length + word.length > maxChunkSize && currentWordChunk.isNotEmpty()) {
                    chunks.add(currentWordChunk.trim())
                    currentWordChunk = word
                } else {
                    currentWordChunk += if (currentWordChunk.isEmpty()) word else " $word"
                }
            }
            
            if (currentWordChunk.isNotEmpty()) {
                chunks.add(currentWordChunk.trim())
            }
        }
        
        return chunks.filter { it.isNotEmpty() }
    }
    
    // Helper function to speak the next chunk
    private fun speakNextChunk(chunkIndex: Int, chunks: List<String>, setIsSpeaking: (Boolean) -> Unit) {
        if (chunkIndex < chunks.size) {
            val chunk = chunks[chunkIndex]
            println("TTS: Speaking chunk $chunkIndex: ${chunk.take(50)}...")
            
            val speakResult = textToSpeech?.speak(
                chunk, 
                TextToSpeech.QUEUE_ADD, // Add to queue instead of flush
                null, 
                chunkIndex.toString()
            )
            
            if (speakResult != TextToSpeech.SUCCESS) {
                println("TTS: Failed to speak chunk $chunkIndex")
                setIsSpeaking(false)
            }
        }
    }
    
    // Function to stop TTS
    fun stopTTS(setIsSpeaking: (Boolean) -> Unit) {
        textToSpeech?.stop()
        setIsSpeaking(false)
        println("TTS: Stopped by user")
    }
    
    // TTS function with chunking for long text
    fun speakText(text: String, isSpeaking: Boolean, setIsSpeaking: (Boolean) -> Unit, currentLanguage: Language) {
        if (textToSpeech != null && !isSpeaking) {
            setIsSpeaking(true)
            
            // Clean the text for TTS - remove markdown and special characters
            val cleanText = text
                .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // Remove bold markdown
                .replace(Regex("__(.*?)__"), "$1") // Remove bold markdown
                .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1") // Remove links, keep text
                .replace(Regex("[*_`]"), "") // Remove remaining markdown characters
                .trim()
            
            println("TTS: Speaking text length: ${cleanText.length} characters")
            println("TTS: Current language: $currentLanguage")
            
            // Set language based on current language setting
            val language = if (currentLanguage == Language.HINDI) {
                Locale("hi", "IN")
            } else {
                Locale.ENGLISH
            }
            
            val result = textToSpeech?.setLanguage(language)
            println("TTS: Language set result: $result")
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                println("TTS: Language not supported, falling back to default")
                textToSpeech?.setLanguage(Locale.getDefault())
            }
            
            // Split text into chunks for better TTS handling
            val chunks = splitTextIntoChunks(cleanText)
            println("TTS: Split text into ${chunks.size} chunks")
            
            // Set up utterance progress listener
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    println("TTS Started chunk: $utteranceId")
                }
                override fun onDone(utteranceId: String?) {
                    println("TTS Completed chunk: $utteranceId")
                    // Continue with next chunk if available
                    val currentChunkIndex = utteranceId?.toIntOrNull() ?: 0
                    if (currentChunkIndex + 1 < chunks.size) {
                        speakNextChunk(currentChunkIndex + 1, chunks, setIsSpeaking)
                    } else {
                        // All chunks completed
                        setIsSpeaking(false)
                        println("TTS: All chunks completed")
                    }
                }
                override fun onError(utteranceId: String?) {
                    println("TTS Error in chunk: $utteranceId")
                    setIsSpeaking(false)
                }
            })
            
            // Start speaking the first chunk
            if (chunks.isNotEmpty()) {
                speakNextChunk(0, chunks, setIsSpeaking)
            } else {
                setIsSpeaking(false)
            }
        } else {
            println("TTS: Cannot speak - textToSpeech is null or already speaking")
            println("TTS: textToSpeech is null: ${textToSpeech == null}")
            println("TTS: isSpeaking: $isSpeaking")
        }
    }
    
    // STT function
    fun startSpeechRecognition(isListening: Boolean, setIsListening: (Boolean) -> Unit, currentLanguage: Language, speechLauncher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        if (!isListening) {
            setIsListening(true)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (currentLanguage == Language.HINDI) "hi-IN" else "en-US")
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (currentLanguage == Language.ENGLISH) "Speak your answer" else "अपना उत्तर बोलें")
            }
            speechLauncher.launch(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize TTS
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                println("TTS: Initialization successful")
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("TTS: Default language not supported")
                } else {
                    println("TTS: Default language set successfully")
                }
            } else {
                println("TTS: Initialization failed with status: $status")
            }
        }
        
        setContent {
            KalyanSarathiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val application = application as KalyanSarathiApplication
                    KalyanSarathiApp(application.repository, this@MainActivity, textToSpeech)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalyanSarathiApp(repository: KalyanSarathiRepository, context: Context, textToSpeech: TextToSpeech?) {
    var currentLanguage by remember { mutableStateOf(Language.ENGLISH) }
    var currentStep by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var userProfile by remember { mutableStateOf(UserProfile()) }
    var textInput by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("") }
    var otherOccupationInput by remember { mutableStateOf("") }
    var otherGenderInput by remember { mutableStateOf("") }
    var otherLivingAreaInput by remember { mutableStateOf("") }
    var otherCasteInput by remember { mutableStateOf("") }
    var otherMinorityReligionInput by remember { mutableStateOf("") }
    var otherIncomeInput by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    var showHomeScreen by remember { mutableStateOf(true) }
    var chatSessions by remember { mutableStateOf<List<ChatSession>>(emptyList()) }
    var isListening by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var currentSessionId by remember { mutableStateOf<Long?>(null) }
    
    // Landscape mode detection
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // STT launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                textInput = spokenText
                selectedOption = spokenText
                otherOccupationInput = spokenText
            }
        }
        isListening = false
    }

    // Load initial message
    LaunchedEffect(currentLanguage) {
        val welcomeMessage = when (currentLanguage) {
            Language.ENGLISH -> "The KalyanSarathi Bot is an excellent tool to help you discover central and state government schemes that you may be eligible for. To ensure we provide you with the most relevant schemes, please fill out this form first. This will allow us to tailor the results to your specific needs."
            Language.HINDI -> "कल्याणसारथी बॉट एक उत्कृष्ट उपकरण है जो आपको केंद्रीय और राज्य सरकारी योजनाओं की खोज में मदद करता है जिनके लिए आप पात्र हो सकते हैं। सबसे प्रासंगिक योजनाएं प्रदान करने के लिए, कृपया पहले इस फॉर्म को भरें।"
        }
        val initialMessage = ChatMessage(
            userProfileId = 0,
            message = welcomeMessage,
            isFromUser = false,
            language = currentLanguage
        )
        messages = listOf(initialMessage)
    }

    // Load chat sessions
    LaunchedEffect(showHistory) {
        if (showHistory) {
            try {
                repository.getAllChatSessions().collect { sessions ->
                    chatSessions = sessions
                }
            } catch (e: Exception) {
                // Handle error gracefully - show empty sessions
                chatSessions = emptyList()
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Landscape mode warning
        if (isLandscape) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱",
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) {
                            "Please rotate your device to portrait mode for the best experience"
                        } else {
                            "बेहतर अनुभव के लिए कृपया अपने डिवाइस को पोर्ट्रेट मोड में घुमाएं"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) {
                            "The chatbot interface is optimized for portrait orientation"
                        } else {
                            "चैटबॉट इंटरफेस पोर्ट्रेट ओरिएंटेशन के लिए अनुकूलित है"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "KalyanSarathi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                TextButton(onClick = {
                    currentLanguage = if (currentLanguage == Language.ENGLISH) Language.HINDI else Language.ENGLISH
                }) {
                    Text("🌐")
                }
                Text(
                    text = if (currentLanguage == Language.ENGLISH) "हिंदी" else "English",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        )

        if (showHomeScreen) {
            // Home Screen
            HomeScreenContent(
                currentLanguage = currentLanguage,
                onStartChat = {
                    showHomeScreen = false
                    currentStep = 1
                    messages = emptyList()
                    userProfile = UserProfile()
                    currentSessionId = null
                    val welcomeMessage = when (currentLanguage) {
                        Language.ENGLISH -> "The KalyanSarathi Bot is an excellent tool to help you discover central and state government schemes that you may be eligible for. To ensure we provide you with the most relevant schemes, please fill out this form first. This will allow us to tailor the results to your specific needs."
                        Language.HINDI -> "कल्याणसारथी बॉट एक उत्कृष्ट उपकरण है जो आपको केंद्रीय और राज्य सरकारी योजनाओं की खोज में मदद करता है जिनके लिए आप पात्र हो सकते हैं। सबसे प्रासंगिक योजनाएं प्रदान करने के लिए, कृपया पहले इस फॉर्म को भरें।"
                    }
                    messages = listOf(ChatMessage(userProfileId = 0, message = welcomeMessage, isFromUser = false, language = currentLanguage))
                },
                onViewHistory = {
                    showHomeScreen = false
                    showHistory = true
                }
            )
        } else if (showHistory) {
            // Chat History Screen
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "Chat History" else "चैट इतिहास",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Button(onClick = {
                        showHistory = false
                        showHomeScreen = true
                    }) {
                        Text(if (currentLanguage == Language.ENGLISH) "Back" else "वापस")
                    }
                }

                if (chatSessions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📋",
                                style = MaterialTheme.typography.displayLarge,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (currentLanguage == Language.ENGLISH) {
                                    "No chat history yet"
                                } else {
                                    "अभी तक कोई चैट इतिहास नहीं है"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatSessions) { session ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                // Load chat messages for this session
                                                coroutineScope.launch {
                                                    try {
                                                        val sessionMessages = repository.getChatMessagesBySessionId(session.id)
                                                        messages = sessionMessages
                                                        currentSessionId = session.id // Set the current session ID
                                                        showHistory = false
                                                        currentStep = 10 // Go to follow-up questions
                                                    } catch (e: Exception) {
                                                        // Handle error gracefully - show empty messages or error message
                                                        messages = emptyList()
                                                        currentSessionId = session.id
                                                        showHistory = false
                                                        currentStep = 10
                                                    }
                                                }
                                            }
                                    ) {
                                        Text(
                                            text = session.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Language: ${if (session.language == Language.ENGLISH) "English" else "हिंदी"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.deleteChatSession(session)
                                                chatSessions = chatSessions.filter { it.id != session.id }
                                            }
                                        }
                                    ) {
                                        Text("🗑️", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Main Chat Interface
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    showHomeScreen = true
                    currentStep = 0
                    messages = emptyList()
                    userProfile = UserProfile()
                    currentSessionId = null
                }) {
                    Text(if (currentLanguage == Language.ENGLISH) "Back to Home" else "होम पर वापस")
                }
                Text(
                    text = if (currentLanguage == Language.ENGLISH) "Chat" else "चैट",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            // Chat Disclaimer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) {
                            "Disclaimer: Information provided may contain errors or be misleading. Please verify all details from official government sources."
                        } else {
                            "अस्वीकरण: प्रदान की गई जानकारी में त्रुटियां हो सकती हैं या भ्रामक हो सकती है। कृपया सभी विवरण आधिकारिक सरकारी स्रोतों से सत्यापित करें।"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Progress Indicator
            if (currentStep > 0 && currentStep <= 9) {
                LinearProgressIndicator(
                    progress = { currentStep / 9f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${currentStep}/9",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message, 
                        onSpeakText = { text -> (context as MainActivity).speakText(text, isSpeaking, { newValue -> isSpeaking = newValue }, currentLanguage) }, 
                        onStopTTS = { (context as MainActivity).stopTTS { newValue -> isSpeaking = newValue } }, 
                        isSpeaking = isSpeaking
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (currentLanguage == Language.ENGLISH) "Getting recommendations..." else "सुझाव प्राप्त कर रहे हैं...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = if (currentLanguage == Language.ENGLISH) "This may take up to 2 minutes" else "यह 2 मिनट तक लग सकता है",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Section
            if (currentStep > 0 && currentStep <= 9) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = getCurrentQuestion(currentStep, currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val options = getOptionsForCurrentStep(currentStep, currentLanguage)

                        if (options.isNotEmpty()) {
                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedOption,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    options.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedOption = option
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Show text input for "Other" options
                            if ((currentStep == 2 && (selectedOption == "Other" || selectedOption == "अन्य")) ||
                                (currentStep == 3 && (selectedOption == "Other" || selectedOption == "अन्य")) ||
                                (currentStep == 4 && (selectedOption == "Other" || selectedOption == "अन्य"))) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = when (currentStep) {
                                        2 -> otherGenderInput
                                        3 -> otherLivingAreaInput
                                        4 -> otherOccupationInput
                                        else -> ""
                                    },
                                    onValueChange = { newValue ->
                                        when (currentStep) {
                                            2 -> otherGenderInput = newValue
                                            3 -> otherLivingAreaInput = newValue
                                            4 -> otherOccupationInput = newValue
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = when (currentStep) {
                                                2 -> if (currentLanguage == Language.ENGLISH) "Please specify your gender" else "कृपया अपना लिंग निर्दिष्ट करें"
                                                3 -> if (currentLanguage == Language.ENGLISH) "Please specify your living area" else "कृपया अपना रहने का क्षेत्र निर्दिष्ट करें"
                                                4 -> if (currentLanguage == Language.ENGLISH) "Please specify your occupation" else "कृपया अपना व्यवसाय निर्दिष्ट करें"
                                                6 -> if (currentLanguage == Language.ENGLISH) "Please specify your caste" else "कृपया अपनी जाति निर्दिष्ट करें"
                                                8 -> if (currentLanguage == Language.ENGLISH) "Please specify your religion details" else "कृपया अपने धर्म का विवरण निर्दिष्ट करें"
                                                9 -> if (currentLanguage == Language.ENGLISH) "Please specify your income details" else "कृपया अपनी आय का विवरण निर्दिष्ट करें"
                                                else -> ""
                                            }
                                        )
                                    }
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = when (currentStep) {
                                            1 -> if (currentLanguage == Language.ENGLISH) "Enter phone number" else "फोन नंबर दर्ज करें"
                                            5 -> if (currentLanguage == Language.ENGLISH) "Enter age" else "आयु दर्ज करें"
                                            else -> ""
                                        }
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // TTS and STT buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // TTS Button - Speak current question
                            OutlinedButton(
                                onClick = {
                                    val currentQuestion = getCurrentQuestion(currentStep, currentLanguage)
                                    (context as MainActivity).speakText(currentQuestion, isSpeaking, { newValue -> isSpeaking = newValue }, currentLanguage)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSpeaking
                            ) {
                                Text("🔊")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Speak Question" else "प्रश्न सुनें",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // STT Button - Voice input
                            OutlinedButton(
                                onClick = { (context as MainActivity).startSpeechRecognition(isListening, { newValue -> isListening = newValue }, currentLanguage, speechLauncher) },
                                modifier = Modifier.weight(1f),
                                enabled = !isListening
                            ) {
                                Text("🎤")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Voice Input" else "आवाज़ इनपुट",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentStep > 1) {
                                OutlinedButton(
                                    onClick = { currentStep-- },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (currentLanguage == Language.ENGLISH) "Previous" else "पिछला")
                                }
                            }

                            Button(
                                onClick = {
                                    val inputValue = when {
                                        // Handle "Other" cases for all steps
                                        currentStep == 2 && (selectedOption == "Other" || selectedOption == "अन्य") -> {
                                            if (otherGenderInput.isNotEmpty()) otherGenderInput else selectedOption
                                        }
                                        currentStep == 3 && (selectedOption == "Other" || selectedOption == "अन्य") -> {
                                            if (otherLivingAreaInput.isNotEmpty()) otherLivingAreaInput else selectedOption
                                        }
                                        currentStep == 4 && (selectedOption == "Other" || selectedOption == "अन्य") -> {
                                            if (otherOccupationInput.isNotEmpty()) otherOccupationInput else selectedOption
                                        }
                                        // Handle dropdown options
                                        options.isNotEmpty() -> selectedOption
                                        // Handle text input
                                        else -> textInput
                                    }
                                    
                                    if (inputValue.isNotEmpty()) {
                                        userProfile = updateUserProfile(userProfile, currentStep, inputValue)

                                        val userMessage = ChatMessage(
                                            userProfileId = currentSessionId ?: 0,
                                            message = inputValue,
                                            isFromUser = true,
                                            language = currentLanguage
                                        )
                                        messages = messages + userMessage

                                        // Save user message immediately if session exists
                                        if (currentSessionId != null) {
                                            coroutineScope.launch {
                                                repository.insertChatMessage(userMessage.copy(userProfileId = currentSessionId!!))
                                            }
                                        }

                                        textInput = ""
                                        selectedOption = ""
                                        otherOccupationInput = ""
                                        otherGenderInput = ""
                                        otherLivingAreaInput = ""

                                        if (currentStep == 9) {
                                            isLoading = true
                                            getRecommendations(repository, userProfile, currentLanguage, coroutineScope, context) { recommendations ->
                                                val recommendationMessage = ChatMessage(
                                                    userProfileId = currentSessionId ?: 0,
                                                    message = recommendations,
                                                    isFromUser = false,
                                                    language = currentLanguage
                                                )
                                                messages = messages + recommendationMessage
                                                
                                                // Save chat session if not already created
                                                coroutineScope.launch {
                                                    val sessionId = if (currentSessionId == null) {
                                                        val chatSession = ChatSession(
                                                            id = 0, // Auto-generated by database
                                                            userProfileId = 0, // Default user profile ID
                                                            title = "Chat Session - ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}",
                                                            language = currentLanguage
                                                        )
                                                        val newSessionId = repository.insertChatSession(chatSession)
                                                        currentSessionId = newSessionId
                                                        
                                                        // Save all messages for this session with the correct session ID
                                                        messages.forEach { message ->
                                                            repository.insertChatMessage(message.copy(userProfileId = newSessionId))
                                                        }
                                                        newSessionId
                                                    } else {
                                                        // Session already exists, just save the new recommendation message
                                                        repository.insertChatMessage(recommendationMessage.copy(userProfileId = currentSessionId!!))
                                                        currentSessionId!!
                                                    }
                                                }
                                                
                                                isLoading = false
                                                currentStep = 10
                                            }
                                        } else {
                                            currentStep++
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = when {
                                    // Handle "Other" cases for all steps
                                    currentStep == 2 && (selectedOption == "Other" || selectedOption == "अन्य") -> {
                                        otherGenderInput.isNotEmpty()
                                    }
                                    currentStep == 3 && (selectedOption == "Other" || selectedOption == "अन्य") -> {
                                        otherLivingAreaInput.isNotEmpty()
                                    }
                                    currentStep == 4 && (selectedOption == "Other" || selectedOption == "अन्य") -> {
                                        otherOccupationInput.isNotEmpty()
                                    }
                                    // Handle dropdown options
                                    options.isNotEmpty() -> selectedOption.isNotEmpty()
                                    // Handle text input
                                    else -> textInput.isNotEmpty()
                                }
                            ) {
                                Text(
                                    if (currentStep == 9) {
                                        if (currentLanguage == Language.ENGLISH) "Get Recommendations" else "सुझाव प्राप्त करें"
                                    } else {
                                        if (currentLanguage == Language.ENGLISH) "Next" else "अगला"
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Follow-up Questions Section
            if (currentStep == 10) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) {
                                "Do you have any questions about the schemes or need clarification on any point?"
                            } else {
                                "क्या आपके पास योजनाओं के बारे में कोई प्रश्न हैं या किसी बिंदु पर स्पष्टीकरण की आवश्यकता है?"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // TTS button for the question
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val questionText = if (currentLanguage == Language.ENGLISH) {
                                        "Do you have any questions about the schemes or need clarification on any point?"
                                    } else {
                                        "क्या आपके पास योजनाओं के बारे में कोई प्रश्न हैं या किसी बिंदु पर स्पष्टीकरण की आवश्यकता है?"
                                    }
                                    (context as MainActivity).speakText(questionText, isSpeaking, { newValue -> isSpeaking = newValue }, currentLanguage)
                                },
                                modifier = Modifier.padding(bottom = 8.dp),
                                enabled = !isSpeaking
                            ) {
                                Text("🔊")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Speak Question" else "प्रश्न सुनें",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) {
                                        "Type your question here..."
                                    } else {
                                        "अपना प्रश्न यहाँ टाइप करें..."
                                    }
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // STT button for follow-up questions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { (context as MainActivity).startSpeechRecognition(isListening, { newValue -> isListening = newValue }, currentLanguage, speechLauncher) },
                                modifier = Modifier.weight(1f),
                                enabled = !isListening
                            ) {
                                Text("🎤")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Voice Input" else "आवाज़ इनपुट",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showHomeScreen = true
                                    currentStep = 0
                                    messages = emptyList()
                                    userProfile = UserProfile()
                                    currentSessionId = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (currentLanguage == Language.ENGLISH) "New Chat" else "नई चैट")
                            }

                            Button(
                                onClick = {
                                    if (textInput.isNotEmpty()) {
                                        val userQuestion = ChatMessage(
                                            userProfileId = currentSessionId ?: 0,
                                            message = textInput,
                                            isFromUser = true,
                                            language = currentLanguage
                                        )
                                        messages = messages + userQuestion

                                        // Save user question immediately if session exists
                                        if (currentSessionId != null) {
                                            coroutineScope.launch {
                                                repository.insertChatMessage(userQuestion.copy(userProfileId = currentSessionId!!))
                                            }
                                        }

                                        isLoading = true
                                        getFollowUpAnswer(repository, userProfile, textInput, currentLanguage, coroutineScope, context) { answer ->
                                            val answerMessage = ChatMessage(
                                                userProfileId = currentSessionId ?: 0,
                                                message = answer,
                                                isFromUser = false,
                                                language = currentLanguage
                                            )
                                            messages = messages + answerMessage
                                            
                                            // Save AI answer immediately if session exists
                                            if (currentSessionId != null) {
                                                coroutineScope.launch {
                                                    repository.insertChatMessage(answerMessage.copy(userProfileId = currentSessionId!!))
                                                }
                                            }
                                            
                                            isLoading = false
                                        }

                                        textInput = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = textInput.isNotEmpty()
                            ) {
                                Text(if (currentLanguage == Language.ENGLISH) "Ask Question" else "प्रश्न पूछें")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    currentLanguage: Language,
    onStartChat: () -> Unit,
    onViewHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (currentLanguage == Language.ENGLISH) {
                        "Welcome to KalyanSarathi! Discover government schemes you're eligible for."
                    } else {
                        "कल्याणसारथी में आपका स्वागत है! अपने लिए उपलब्ध सरकारी योजनाओं की खोज करें।"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = if (currentLanguage == Language.ENGLISH) {
                        "Get personalized recommendations for central and state government schemes based on your profile."
                    } else {
                        "अपनी प्रोफ़ाइल के आधार पर केंद्रीय और राज्य सरकारी योजनाओं के लिए व्यक्तिगत सुझाव प्राप्त करें।"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartChat,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (currentLanguage == Language.ENGLISH) "Start Chat" else "चैट शुरू करें",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onViewHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("📋")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentLanguage == Language.ENGLISH) "Chat History" else "चैट इतिहास",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (currentLanguage == Language.ENGLISH) "Features:" else "विशेषताएं:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val features = if (currentLanguage == Language.ENGLISH) {
                    listOf(
                        "• Bilingual support (English/Hindi)",
                        "• Personalized scheme recommendations",
                        "• Step-by-step application guidance",
                        "• Chat history management"
                    )
                } else {
                    listOf(
                        "• द्विभाषी समर्थन (अंग्रेजी/हिंदी)",
                        "• व्यक्तिगत योजना सुझाव",
                        "• चरणबद्ध आवेदन मार्गदर्शन",
                        "• चैट इतिहास प्रबंधन"
                    )
                }

                features.forEach { feature ->
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Government Affiliation Disclaimer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (currentLanguage == Language.ENGLISH) {
                        "⚠️ Disclaimer"
                    } else {
                        "⚠️ अस्वीकरण"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (currentLanguage == Language.ENGLISH) {
                        "This application is in no way affiliated with, endorsed by, or connected to any government entity or agency. KalyanSarathi is an independent service providing information about government schemes for informational purposes only."
                    } else {
                        "यह एप्लिकेशन किसी भी तरह से किसी सरकारी संस्था या एजेंसी से संबद्ध, समर्थित या जुड़ा नहीं है। कल्याणसारथी केवल सूचनात्मक उद्देश्यों के लिए सरकारी योजनाओं के बारे में जानकारी प्रदान करने वाली एक स्वतंत्र सेवा है।"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, onSpeakText: (String) -> Unit = {}, onStopTTS: () -> Unit = {}, isSpeaking: Boolean = false) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column {
                val processedText = if (message.isFromUser) {
                    message.message
                } else {
                    // Convert states to Hindi for AI responses
                    convertStatesToHindi(message.message)
                }
                
                if (message.isFromUser) {
                    // User messages - simple text display
                    Text(
                        text = processedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    // AI messages - markdown parsing with clickable links
                    val annotatedText = parseMarkdownToAnnotatedString(processedText)
                    
                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(16.dp),
                        onClick = { offset ->
                            // Handle link clicks
                            annotatedText.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                // Open URL in browser
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(annotation.item))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    println("Failed to open URL: ${annotation.item}")
                                }
                            }
                        }
                    )
                    
                    // TTS buttons for AI responses
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isSpeaking) {
                            // Stop button when speaking
                            IconButton(
                                onClick = { onStopTTS() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    text = "⏹️",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            // Play button when not speaking
                            IconButton(
                                onClick = { onSpeakText(processedText) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    text = "🔊",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getCurrentQuestion(step: Int, language: Language): String {
    return when (step) {
        1 -> if (language == Language.ENGLISH) "Please enter your Phone number:" else "कृपया अपना फोन नंबर दर्ज करें:"
        2 -> if (language == Language.ENGLISH) "Please enter your Gender:" else "कृपया अपना लिंग दर्ज करें:"
        3 -> if (language == Language.ENGLISH) "Please choose whether your registered address is rural or urban:" else "कृपया चुनें कि आपका पंजीकृत पता ग्रामीण है या शहरी:"
        4 -> if (language == Language.ENGLISH) "Please specify your occupation:" else "कृपया अपना व्यवसाय निर्दिष्ट करें:"
        5 -> if (language == Language.ENGLISH) "Please enter your age:" else "कृपया अपनी आयु दर्ज करें:"
        6 -> if (language == Language.ENGLISH) "Please Enter your Caste:" else "कृपया अपनी जाति दर्ज करें:"
        7 -> if (language == Language.ENGLISH) "Please enter the state/Union Territory that you are a domicile of:" else "कृपया वह राज्य/केंद्र शासित प्रदेश दर्ज करें जिसके आप निवासी हैं:"
        8 -> if (language == Language.ENGLISH) "Are you from a minority religion?" else "क्या आप अल्पसंख्यक धर्म से हैं?"
        9 -> if (language == Language.ENGLISH) "What is your monthly household income?" else "आपकी मासिक घरेलू आय क्या है?"
        else -> ""
    }
}

private fun getOptionsForCurrentStep(step: Int, language: Language = Language.ENGLISH): List<String> {
    return when (step) {
        2 -> if (language == Language.ENGLISH) {
            listOf("Male", "Female", "Other")
        } else {
            listOf("पुरुष", "महिला", "अन्य")
        }
        3 -> if (language == Language.ENGLISH) {
            listOf("Rural", "Urban", "Other")
        } else {
            listOf("ग्रामीण", "शहरी", "अन्य")
        }
        4 -> if (language == Language.ENGLISH) {
            listOf("Daily Wage Labor/Farmer", "Entrepreneur", "Student", "Employed", "Informal sector", "Other")
        } else {
            listOf("दिहाड़ी मजदूर/किसान", "उद्यमी", "छात्र", "नियोजित", "अनौपचारिक क्षेत्र", "अन्य")
        }
        6 -> if (language == Language.ENGLISH) {
            listOf("Scheduled Caste", "Scheduled Tribe", "Backward Caste", "Other Backward Caste", "General Category")
        } else {
            listOf("अनुसूचित जाति", "अनुसूचित जनजाति", "पिछड़ी जाति", "अन्य पिछड़ी जाति", "सामान्य श्रेणी")
        }
        7 -> if (language == Language.ENGLISH) {
            listOf(
                "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat",
                "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh",
                "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
                "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
                "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi", 
                "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
            )
        } else {
            listOf(
                "आंध्र प्रदेश", "अरुणाचल प्रदेश", "असम", "बिहार", "छत्तीसगढ़", "गोवा", "गुजरात",
                "हरियाणा", "हिमाचल प्रदेश", "झारखंड", "कर्नाटक", "केरल", "मध्य प्रदेश",
                "महाराष्ट्र", "मणिपुर", "मेघालय", "मिजोरम", "नागालैंड", "ओडिशा", "पंजाब",
                "राजस्थान", "सिक्किम", "तमिलनाडु", "तेलंगाना", "त्रिपुरा", "उत्तर प्रदेश", "उत्तराखंड", "पश्चिम बंगाल",
                "अंडमान और निकोबार द्वीप समूह", "चंडीगढ़", "दादरा और नगर हवेली और दमन और दीव", "दिल्ली", 
                "जम्मू और कश्मीर", "लद्दाख", "लक्षद्वीप", "पुडुचेरी"
            )
        }
        8 -> if (language == Language.ENGLISH) {
            listOf("Yes", "No")
        } else {
            listOf("हाँ", "नहीं")
        }
        9 -> if (language == Language.ENGLISH) {
            listOf("<5000", "5000-10,000", "10,000-25,000", "25,000-50,000", ">50,000")
        } else {
            listOf("<5000", "5000-10,000", "10,000-25,000", "25,000-50,000", ">50,000")
        }
        else -> emptyList()
    }
}

private fun updateUserProfile(profile: UserProfile, step: Int, value: String): UserProfile {
    return when (step) {
        1 -> profile.copy(phoneNumber = value)
        2 -> {
            val gender = when (value) {
                "Male", "पुरुष" -> Gender.MALE
                "Female", "महिला" -> Gender.FEMALE
                "Other", "अन्य" -> Gender.OTHER
                else -> Gender.OTHER // Treat any custom value as OTHER
            }
            profile.copy(gender = gender, customGender = if (gender == Gender.OTHER) value else "")
        }
        3 -> {
            val livingArea = when (value) {
                "Rural", "ग्रामीण" -> LivingArea.RURAL
                "Urban", "शहरी" -> LivingArea.URBAN
                else -> LivingArea.RURAL // Treat any custom value as RURAL
            }
            profile.copy(livingArea = livingArea, customLivingArea = if (value != "Rural" && value != "ग्रामीण" && value != "Urban" && value != "शहरी") value else "")
        }
        4 -> {
            val occupation = when (value) {
                "Daily Wage Labor/Farmer", "दिहाड़ी मजदूर/किसान" -> Occupation.DAILY_WAGE_LABOR
                "Entrepreneur", "उद्यमी" -> Occupation.ENTREPRENEUR
                "Student", "छात्र" -> Occupation.STUDENT
                "Employed", "नियोजित" -> Occupation.EMPLOYED
                "Informal sector", "अनौपचारिक क्षेत्र" -> Occupation.INFORMAL_SECTOR
                "Other", "अन्य" -> Occupation.OTHER
                else -> Occupation.OTHER // Treat any custom value as OTHER
            }
            profile.copy(occupation = occupation, customOccupation = if (occupation == Occupation.OTHER) value else "")
        }
        5 -> profile.copy(age = value.toIntOrNull() ?: 0)
        6 -> {
            val caste = when (value) {
                "Scheduled Caste", "अनुसूचित जाति" -> Caste.SCHEDULED_CASTE
                "Scheduled Tribe", "अनुसूचित जनजाति" -> Caste.SCHEDULED_TRIBE
                "Backward Caste", "पिछड़ी जाति" -> Caste.BACKWARD_CASTE
                "Other Backward Caste", "अन्य पिछड़ी जाति" -> Caste.OTHER_BACKWARD_CASTE
                "General Category", "सामान्य श्रेणी" -> Caste.GENERAL_CATEGORY
                else -> Caste.OTHER_BACKWARD_CASTE // Treat any custom value as OTHER_BACKWARD_CASTE
            }
            // Store custom caste value if it's not in the predefined list
            val isCustomCaste = !listOf("Scheduled Caste", "अनुसूचित जाति", "Scheduled Tribe", "अनुसूचित जनजाति", 
                "Backward Caste", "पिछड़ी जाति", "Other Backward Caste", "अन्य पिछड़ी जाति", "General Category", "सामान्य श्रेणी").contains(value)
            profile.copy(caste = caste, customCaste = if (isCustomCaste) value else "")
        }
        7 -> profile.copy(state = convertHindiToEnglishState(value))
        8 -> {
            val isMinority = value == "Yes" || value == "हाँ"
            // Store custom minority religion value if it's not Yes/No
            val isCustomMinority = !listOf("Yes", "हाँ", "No", "नहीं").contains(value)
            profile.copy(
                isMinorityReligion = isMinority, 
                customMinorityReligion = if (isCustomMinority) value else ""
            )
        }
        9 -> {
            val income = when (value) {
                "<5000" -> IncomeRange.LESS_THAN_5000
                "5000-10,000" -> IncomeRange.BETWEEN_5000_10000
                "10,000-25,000" -> IncomeRange.BETWEEN_10000_25000
                "25,000-50,000" -> IncomeRange.BETWEEN_25000_50000
                ">50,000" -> IncomeRange.MORE_THAN_50000
                else -> IncomeRange.LESS_THAN_5000 // Treat any custom value as LESS_THAN_5000
            }
            // Store custom income value if it's not in the predefined list
            val isCustomIncome = !listOf("<5000", "5000-10,000", "10,000-25,000", "25,000-50,000", ">50,000").contains(value)
            profile.copy(monthlyIncome = income, customIncome = if (isCustomIncome) value else "")
        }
        else -> profile
    }
}

// NETWORK CONNECTIVITY CHECK
private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    
    return when {
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

// GEMINI API FUNCTIONS - NO FALLBACKS, ONLY REAL API RESPONSES
private fun getRecommendations(
    repository: KalyanSarathiRepository,
    profile: UserProfile,
    language: Language,
    coroutineScope: CoroutineScope,
    context: Context,
    onResult: (String) -> Unit
) {
    // Check network connectivity first
    if (!isNetworkAvailable(context)) {
        println("=== NETWORK CONNECTIVITY CHECK FAILED ===")
        println("No network connection available")
        println("========================================")
        onResult("❌ Error: No internet connection detected. Please check your WiFi or mobile data connection.")
        return
    }
    
    println("=== NETWORK CONNECTIVITY CHECK PASSED ===")
    println("Network connection is available")
    println("======================================")
    
    val prompt = generateGeminiPrompt(profile, language)
    
    println("=== GEMINI API PROMPT ===")
    println("User Profile: ${profile}")
    println("Generated Prompt: $prompt")
    println("========================")
    
    val geminiApiService = NetworkModule.geminiApiService
    
    coroutineScope.launch {
        var apiResponseReceived = false
        
        // Add timeout warning after 30 seconds
        val timeoutWarningJob = coroutineScope.launch {
            kotlinx.coroutines.delay(30000) // 30 seconds
            if (!apiResponseReceived) {
                println("=== API TIMEOUT WARNING ===")
                println("API call is taking longer than expected (30+ seconds)")
                println("This is normal for complex requests")
                println("==========================")
            }
        }
        
        try {
            val request = com.example.kalyansarathi.network.GeminiRequest(
                contents = listOf(
                    com.example.kalyansarathi.network.RequestContent(
                        parts = listOf(
                            com.example.kalyansarathi.network.RequestPart(text = prompt)
                        )
                    )
                )
            )
            
            println("=== SENDING REQUEST ===")
            println("Request: $request")
            println("======================")
            
            val response = geminiApiService.generateContent(
                apiKey = "AIzaSyCXB9nKAVYkXLdxOV-_9AFWoKeCQwc29X8",
                request = request
            )
            
            println("=== RAW RESPONSE ===")
            println("Response Code: ${response.code()}")
            println("Response Body: ${response.body()}")
            println("Response Error: ${response.errorBody()?.string()}")
            println("===================")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.error != null) {
                        println("=== API ERROR IN RESPONSE ===")
                        println("Error: ${responseBody.error}")
                        println("=============================")
                    } else {
                        val candidates = responseBody.candidates
                        if (!candidates.isNullOrEmpty()) {
                            val firstCandidate = candidates.first()
                            val content = firstCandidate.content
                            if (content != null) {
                                val parts = content.parts
                                if (!parts.isNullOrEmpty()) {
                                    val text = parts.first().text
                                    if (!text.isNullOrEmpty()) {
                                        println("=== API RESPONSE SUCCESS ===")
                                        println("Recommendations: $text")
                                        println("===========================")
                                        apiResponseReceived = true
                                        timeoutWarningJob.cancel() // Cancel the warning job
                                        onResult(text)
                                        return@launch
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (!apiResponseReceived) {
                println("=== GEMINI API FAILED - NO RESPONSE ===")
                println("API call failed or returned empty response")
                println("This indicates a problem with the Gemini API integration")
                println("==============================================")
                timeoutWarningJob.cancel() // Cancel the warning job
                onResult("❌ Error: Unable to connect to Gemini API. Please check your internet connection and try again.")
            }
        } catch (e: Exception) {
            println("=== GEMINI API EXCEPTION ===")
            println("Exception: ${e.message}")
            println("Exception Type: ${e.javaClass.simpleName}")
            println("Stack Trace: ${e.stackTraceToString()}")
            println("This indicates a network or API configuration problem")
            println("=====================================================")
            
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> {
                    "❌ Error: Request timed out. The API is taking longer than expected. Please try again."
                }
                e.message?.contains("connection", ignoreCase = true) == true -> {
                    "❌ Error: Connection failed. Please check your internet connection and try again."
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    "❌ Error: Network error. Please check your internet connection and try again."
                }
                else -> {
                    "❌ Error: ${e.message ?: "Unknown error occurred"}. Please try again."
                }
            }
            
            onResult(errorMessage)
        }
    }
}

private fun getFollowUpAnswer(
    repository: KalyanSarathiRepository,
    profile: UserProfile,
    question: String,
    language: Language,
    coroutineScope: CoroutineScope,
    context: Context,
    onResult: (String) -> Unit
) {
    // Check network connectivity first
    if (!isNetworkAvailable(context)) {
        println("=== NETWORK CONNECTIVITY CHECK FAILED ===")
        println("No network connection available for follow-up")
        println("==========================================")
        onResult("❌ Error: No internet connection detected. Please check your WiFi or mobile data connection.")
        return
    }
    
    println("=== NETWORK CONNECTIVITY CHECK PASSED ===")
    println("Network connection is available for follow-up")
    println("==========================================")
    
    val followUpPrompt = generateFollowUpPrompt(profile, question, language)
    
    println("=== FOLLOW-UP API PROMPT ===")
    println("User Profile: ${profile}")
    println("Question: $question")
    println("Follow-up Prompt: $followUpPrompt")
    println("=============================")
    
    val geminiApiService = NetworkModule.geminiApiService
    
    coroutineScope.launch {
        var apiResponseReceived = false
        
        // Add timeout warning after 30 seconds
        val timeoutWarningJob = coroutineScope.launch {
            kotlinx.coroutines.delay(30000) // 30 seconds
            if (!apiResponseReceived) {
                println("=== FOLLOW-UP API TIMEOUT WARNING ===")
                println("Follow-up API call is taking longer than expected (30+ seconds)")
                println("This is normal for complex requests")
                println("====================================")
            }
        }
        
        try {
            val request = com.example.kalyansarathi.network.GeminiRequest(
                contents = listOf(
                    com.example.kalyansarathi.network.RequestContent(
                        parts = listOf(
                            com.example.kalyansarathi.network.RequestPart(text = followUpPrompt)
                        )
                    )
                )
            )
            
            println("=== SENDING FOLLOW-UP REQUEST ===")
            println("Request: $request")
            println("=================================")
            
            val response = geminiApiService.generateContent(
                apiKey = "AIzaSyCXB9nKAVYkXLdxOV-_9AFWoKeCQwc29X8",
                request = request
            )
            
            println("=== FOLLOW-UP RAW RESPONSE ===")
            println("Response Code: ${response.code()}")
            println("Response Body: ${response.body()}")
            println("Response Error: ${response.errorBody()?.string()}")
            println("=============================")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.error != null) {
                        println("=== FOLLOW-UP API ERROR IN RESPONSE ===")
                        println("Error: ${responseBody.error}")
                        println("======================================")
                    } else {
                        val candidates = responseBody.candidates
                        if (!candidates.isNullOrEmpty()) {
                            val firstCandidate = candidates.first()
                            val content = firstCandidate.content
                            if (content != null) {
                                val parts = content.parts
                                if (!parts.isNullOrEmpty()) {
                                    val text = parts.first().text
                                    if (!text.isNullOrEmpty()) {
                                        println("=== FOLLOW-UP API SUCCESS ===")
                                        println("Answer: $text")
                                        println("==============================")
                                        apiResponseReceived = true
                                        timeoutWarningJob.cancel() // Cancel the warning job
                                        onResult(text)
                                        return@launch
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (!apiResponseReceived) {
                println("=== GEMINI FOLLOW-UP API FAILED - NO RESPONSE ===")
                println("Follow-up API call failed or returned empty response")
                println("This indicates a problem with the Gemini API integration")
                println("=====================================================")
                timeoutWarningJob.cancel() // Cancel the warning job
                onResult("❌ Error: Unable to get follow-up answer from Gemini API. Please check your internet connection and try again.")
            }
        } catch (e: Exception) {
            println("=== GEMINI FOLLOW-UP API EXCEPTION ===")
            println("Exception: ${e.message}")
            println("Exception Type: ${e.javaClass.simpleName}")
            println("Stack Trace: ${e.stackTraceToString()}")
            println("This indicates a network or API configuration problem")
            println("=====================================================")
            
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> {
                    "❌ Error: Request timed out. The API is taking longer than expected. Please try again."
                }
                e.message?.contains("connection", ignoreCase = true) == true -> {
                    "❌ Error: Connection failed. Please check your internet connection and try again."
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    "❌ Error: Network error. Please check your internet connection and try again."
                }
                else -> {
                    "❌ Error: ${e.message ?: "Unknown error occurred"}. Please try again."
                }
            }
            
            onResult(errorMessage)
        }
    }
}

private fun generateGeminiPrompt(profile: UserProfile, language: Language): String {
    val genderText = when (profile.gender) {
        Gender.MALE -> "Male"
        Gender.FEMALE -> "Female"
        Gender.OTHER -> if (profile.customGender.isNotEmpty()) profile.customGender else "Other"
        else -> "Not specified"
    }
    
    val livingAreaText = when (profile.livingArea) {
        LivingArea.RURAL -> if (profile.customLivingArea.isNotEmpty()) profile.customLivingArea else "Rural"
        LivingArea.URBAN -> if (profile.customLivingArea.isNotEmpty()) profile.customLivingArea else "Urban"
        else -> "Not specified"
    }
    
    val occupationText = when (profile.occupation) {
        Occupation.DAILY_WAGE_LABOR -> "Daily Wage Labor/Farmer"
        Occupation.ENTREPRENEUR -> "Entrepreneur"
        Occupation.STUDENT -> "Student"
        Occupation.EMPLOYED -> "Employed"
        Occupation.INFORMAL_SECTOR -> "Informal sector"
        Occupation.OTHER -> if (profile.customOccupation.isNotEmpty()) profile.customOccupation else "Other"
        else -> "Not specified"
    }
    
    val casteText = when (profile.caste) {
        Caste.SCHEDULED_CASTE -> "Scheduled Caste"
        Caste.SCHEDULED_TRIBE -> "Scheduled Tribe"
        Caste.BACKWARD_CASTE -> "Backward Caste"
        Caste.OTHER_BACKWARD_CASTE -> "Other Backward Caste"
        Caste.OTHER_CASTE -> if (profile.customCaste.isNotEmpty()) profile.customCaste else "Other Caste"
        else -> "Not specified"
    }
    
    val incomeText = when (profile.monthlyIncome) {
        IncomeRange.LESS_THAN_5000 -> if (profile.customIncome.isNotEmpty()) profile.customIncome else "<5000"
        IncomeRange.BETWEEN_5000_10000 -> if (profile.customIncome.isNotEmpty()) profile.customIncome else "5000-10,000"
        IncomeRange.BETWEEN_10000_25000 -> if (profile.customIncome.isNotEmpty()) profile.customIncome else "10,000-25,000"
        IncomeRange.BETWEEN_25000_50000 -> if (profile.customIncome.isNotEmpty()) profile.customIncome else "25,000-50,000"
        IncomeRange.MORE_THAN_50000 -> if (profile.customIncome.isNotEmpty()) profile.customIncome else ">50,000"
        else -> "Not specified"
    }
    
    val minorityReligionText = if (profile.isMinorityReligion) {
        if (profile.customMinorityReligion.isNotEmpty()) profile.customMinorityReligion else "Yes"
    } else {
        "No"
    }
    
    return "Using gender: $genderText, living area: $livingAreaText, occupation: $occupationText, Caste: $casteText, Age: ${profile.age}, Minority Religion Status: $minorityReligionText, state: ${profile.state}, Income in Rupees: $incomeText, advise central and state government schemes in India that a user would be eligible for. Also, suggest links to where a person can apply for these schemes. Keep the text short and recommend a minimum of 5 schemes and a maximum of 8 schemes. Ensure that the language of suggestion is in ${if (language == Language.HINDI) "Hindi" else "English"}. Ensure that the scheme descriptions provide the user with step through which they can apply for the scheme in addition to a general description. Include links to sites where they can apply online. Give step by step instructions including required documents, and the exact form they have to fill. IMPORTANT: Format your response using markdown syntax - use **bold** for scheme names and important points, and [link text](URL) for website links. If responding in Hindi, use Hindi names for Indian states (e.g., उत्तर प्रदेश instead of Uttar Pradesh)."
}

private fun generateFollowUpPrompt(profile: UserProfile, question: String, language: Language): String {
    val basePrompt = generateGeminiPrompt(profile, language)
    
    return if (language == Language.ENGLISH) {
        """
        $basePrompt
        
        The user has a follow-up question: "$question"
        
        Please provide a CONCISE and DIRECT answer to this specific question, keeping in mind their profile information.
        Keep your response brief (maximum 3-4 sentences) and focus on the most relevant information.
        Be helpful and provide specific guidance related to their situation.
        IMPORTANT: Format your response using markdown syntax - use **bold** for important points and [link text](URL) for website links.
        """.trimIndent()
    } else {
        """
        $basePrompt
        
        उपयोगकर्ता का एक अनुवर्ती प्रश्न है: "$question"
        
        कृपया इस विशिष्ट प्रश्न का संक्षिप्त और सीधा उत्तर दें, उनकी प्रोफ़ाइल जानकारी को ध्यान में रखते हुए।
        अपना उत्तर संक्षिप्त रखें (अधिकतम 3-4 वाक्य) और सबसे प्रासंगिक जानकारी पर ध्यान केंद्रित करें।
        सहायक बनें और उनकी स्थिति से संबंधित विशिष्ट मार्गदर्शन प्रदान करें।
        महत्वपूर्ण: अपने उत्तर को मार्कडाउन सिंटैक्स का उपयोग करके प्रारूपित करें - महत्वपूर्ण बिंदुओं के लिए **बोल्ड** और वेबसाइट लिंक के लिए [लिंक टेक्स्ट](URL) का उपयोग करें।
        """.trimIndent()
    }
}
