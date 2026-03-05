package com.example.kalyansarathi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kalyansarathi.data.*
import com.example.kalyansarathi.network.GeminiApiService
import com.example.kalyansarathi.network.NetworkModule
import com.example.kalyansarathi.repository.KalyanSarathiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: KalyanSarathiRepository,
    private val geminiApiService: GeminiApiService = NetworkModule.geminiApiService
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _currentUserProfileId = MutableStateFlow<Long?>(null)

    init {
        loadInitialMessage()
    }

    private fun loadInitialMessage() {
        val welcomeMessage = when (_currentLanguage.value) {
            Language.ENGLISH -> "The KalyanSarathi Bot is an excellent tool to help you discover central and state government schemes that you may be eligible for. To ensure we provide you with the most relevant schemes, please fill out this form first. This will allow us to tailor the results to your specific needs."
            Language.HINDI -> "कल्याणसारथी बॉट एक उत्कृष्ट उपकरण है जो आपको केंद्रीय और राज्य सरकारी योजनाओं की खोज में मदद करता है जिनके लिए आप पात्र हो सकते हैं। सबसे प्रासंगिक योजनाएं प्रदान करने के लिए, कृपया पहले इस फॉर्म को भरें।"
        }
        
        val initialMessage = ChatMessage(
            userProfileId = 0,
            message = welcomeMessage,
            isFromUser = false,
            language = _currentLanguage.value
        )
        
        _messages.value = listOf(initialMessage)
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == Language.ENGLISH) {
            Language.HINDI
        } else {
            Language.ENGLISH
        }
        loadInitialMessage()
    }

    fun updateUserProfile(updatedProfile: UserProfile) {
        _userProfile.value = updatedProfile
    }

    fun nextStep() {
        _currentStep.value++
    }

    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value--
        }
    }

    fun saveUserProfileAndGetRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profileId = repository.insertUserProfile(_userProfile.value)
                _currentUserProfileId.value = profileId
                
                // Create chat session
                val sessionTitle = if (_currentLanguage.value == Language.ENGLISH) {
                    "Chat Session - ${_userProfile.value.state}"
                } else {
                    "चैट सत्र - ${_userProfile.value.state}"
                }
                
                val chatSession = ChatSession(
                    userProfileId = profileId,
                    title = sessionTitle,
                    language = _currentLanguage.value
                )
                repository.insertChatSession(chatSession)
                
                // Generate Gemini prompt
                val prompt = generateGeminiPrompt()
                
                // Call Gemini API
                val request = com.example.kalyansarathi.network.GeminiRequest(
                    contents = listOf(
                        com.example.kalyansarathi.network.RequestContent(
                            parts = listOf(
                                com.example.kalyansarathi.network.RequestPart(text = prompt)
                            )
                        )
                    )
                )
                
                println("=== CHATVIEWMODEL API REQUEST ===")
                println("Request: $request")
                println("=================================")
                
                val response = geminiApiService.generateContent(
                    apiKey = "AIzaSyCXB9nKAVYkXLdxOV-_9AFWoKeCQwc29X8",
                    request = request
                )
                
                println("=== CHATVIEWMODEL API RESPONSE ===")
                println("Response Code: ${response.code()}")
                println("Response Body: ${response.body()}")
                println("Response Error: ${response.errorBody()?.string()}")
                println("=================================")
                
                var apiResponseReceived = false
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.error == null) {
                        val candidates = responseBody.candidates
                        if (!candidates.isNullOrEmpty()) {
                            val firstCandidate = candidates.first()
                            val content = firstCandidate.content
                            if (content != null) {
                                val parts = content.parts
                                if (!parts.isNullOrEmpty()) {
                                    val text = parts.first().text
                                    if (!text.isNullOrEmpty()) {
                                        println("=== CHATVIEWMODEL API SUCCESS ===")
                                        println("Recommendations: $text")
                                        println("=================================")
                                        val recommendationMessage = ChatMessage(
                                            userProfileId = profileId,
                                            message = text,
                                            isFromUser = false,
                                            language = _currentLanguage.value
                                        )
                                        
                                        repository.insertMessage(recommendationMessage)
                                        _messages.value = _messages.value + recommendationMessage
                                        apiResponseReceived = true
                                        return@launch
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (!apiResponseReceived) {
                    println("=== CHATVIEWMODEL GEMINI API FAILED - NO RESPONSE ===")
                    println("API call failed or returned empty response")
                    println("This indicates a problem with the Gemini API integration")
                    println("=====================================================")
                    val errorMessage = ChatMessage(
                        userProfileId = profileId,
                        message = "❌ Error: Unable to connect to Gemini API. Please check your internet connection and try again.",
                        isFromUser = false,
                        language = _currentLanguage.value
                    )
                    _messages.value = _messages.value + errorMessage
                }
            } catch (e: Exception) {
                println("=== CHATVIEWMODEL GEMINI API EXCEPTION ===")
                println("Exception: ${e.message}")
                println("Stack Trace: ${e.stackTraceToString()}")
                println("This indicates a network or API configuration problem")
                println("=====================================================")
                val errorMessage = ChatMessage(
                    userProfileId = _currentUserProfileId.value ?: 0,
                    message = "❌ Error: ${e.message}. Please check your internet connection and API configuration.",
                    isFromUser = false,
                    language = _currentLanguage.value
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateGeminiPrompt(): String {
        val profile = _userProfile.value
        val language = _currentLanguage.value
        
        val basePrompt = "Using gender: ${profile.gender}, living area: ${profile.livingArea}, occupation: ${profile.occupation}, Caste: ${profile.caste}, Age: ${profile.age}, Minority Religion Status: ${profile.isMinorityReligion}, state: ${profile.state}, Income in Rupees: ${profile.monthlyIncome}, advise central and state government schemes in India that a user would be eligible for. Also, suggest links to where a person can apply for these schemes. Keep the text short and recommend a minimum of 5 schemes and a maximum of 8 schemes. Ensure that the language of suggestion is in ${if (language == Language.HINDI) "Hindi" else "English"}. Ensure that the scheme descriptions provide the user with step through which they can apply for the scheme in addition to a general description. Include links to sites where they can apply online. Give step by step instructions including required documents, and the exact form they have to fill."
        
        return basePrompt
    }

    fun getCurrentQuestion(): String {
        val step = _currentStep.value
        return when (step) {
            0 -> if (_currentLanguage.value == Language.ENGLISH) "Please enter your Phone number:" else "कृपया अपना फोन नंबर दर्ज करें:"
            1 -> if (_currentLanguage.value == Language.ENGLISH) "Please enter your Gender:" else "कृपया अपना लिंग दर्ज करें:"
            2 -> if (_currentLanguage.value == Language.ENGLISH) "Please choose whether your registered address is rural or urban:" else "कृपया चुनें कि आपका पंजीकृत पता ग्रामीण है या शहरी:"
            3 -> if (_currentLanguage.value == Language.ENGLISH) "Please specify your occupation:" else "कृपया अपना व्यवसाय निर्दिष्ट करें:"
            4 -> if (_currentLanguage.value == Language.ENGLISH) "Please enter your age:" else "कृपया अपनी आयु दर्ज करें:"
            5 -> if (_currentLanguage.value == Language.ENGLISH) "Please Enter your Caste:" else "कृपया अपनी जाति दर्ज करें:"
            6 -> if (_currentLanguage.value == Language.ENGLISH) "Please enter the state/Union Territory that you are a domicile of:" else "कृपया वह राज्य/केंद्र शासित प्रदेश दर्ज करें जिसके आप निवासी हैं:"
            7 -> if (_currentLanguage.value == Language.ENGLISH) "Are you from a minority religion?" else "क्या आप अल्पसंख्यक धर्म से हैं?"
            8 -> if (_currentLanguage.value == Language.ENGLISH) "What is your monthly household income?" else "आपकी मासिक घरेलू आय क्या है?"
            else -> ""
        }
    }

    fun getOptionsForCurrentStep(): List<String> {
        val step = _currentStep.value
        return when (step) {
            1 -> listOf("Male", "Female", "Other")
            2 -> listOf("Rural", "Urban")
            3 -> listOf("Daily Wage Labor/Farmer", "Entrepreneur", "Student", "Employed", "Informal sector", "Other")
            5 -> listOf("Scheduled Caste", "Scheduled Tribe", "Backward Caste", "Other Backward Caste", "Other Caste")
            6 -> listOf(
                "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat",
                "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh",
                "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
                "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal"
            )
            7 -> listOf("Yes", "No")
            8 -> listOf("<5000", "5000-10,000", "10,000-25,000", "25,000-50,000", ">50,000")
            else -> emptyList()
        }
    }

    fun handleUserInput(input: String) {
        val currentStep = _currentStep.value
        val updatedProfile = _userProfile.value.copy()
        
        when (currentStep) {
            0 -> updatedProfile.copy(phoneNumber = input)
            1 -> updatedProfile.copy(gender = when (input) {
                "Male" -> Gender.MALE
                "Female" -> Gender.FEMALE
                "Other" -> Gender.OTHER
                else -> Gender.MALE
            })
            2 -> updatedProfile.copy(livingArea = when (input) {
                "Rural" -> LivingArea.RURAL
                "Urban" -> LivingArea.URBAN
                else -> LivingArea.RURAL
            })
            3 -> updatedProfile.copy(occupation = when (input) {
                "Daily Wage Labor/Farmer" -> Occupation.DAILY_WAGE_LABOR
                "Entrepreneur" -> Occupation.ENTREPRENEUR
                "Student" -> Occupation.STUDENT
                "Employed" -> Occupation.EMPLOYED
                "Informal sector" -> Occupation.INFORMAL_SECTOR
                "Other" -> Occupation.OTHER
                else -> Occupation.DAILY_WAGE_LABOR
            })
            4 -> updatedProfile.copy(age = input.toIntOrNull() ?: 0)
            5 -> updatedProfile.copy(caste = when (input) {
                "Scheduled Caste" -> Caste.SCHEDULED_CASTE
                "Scheduled Tribe" -> Caste.SCHEDULED_TRIBE
                "Backward Caste" -> Caste.BACKWARD_CASTE
                "Other Backward Caste" -> Caste.OTHER_BACKWARD_CASTE
                "Other Caste" -> Caste.OTHER_CASTE
                else -> Caste.OTHER_CASTE
            })
            6 -> updatedProfile.copy(state = input)
            7 -> updatedProfile.copy(isMinorityReligion = input == "Yes")
            8 -> updatedProfile.copy(monthlyIncome = when (input) {
                "<5000" -> IncomeRange.LESS_THAN_5000
                "5000-10,000" -> IncomeRange.BETWEEN_5000_10000
                "10,000-25,000" -> IncomeRange.BETWEEN_10000_25000
                "25,000-50,000" -> IncomeRange.BETWEEN_25000_50000
                ">50,000" -> IncomeRange.MORE_THAN_50000
                else -> IncomeRange.LESS_THAN_5000
            })
        }
        
        _userProfile.value = updatedProfile
        
        // Add user message to chat
        val userMessage = ChatMessage(
            userProfileId = _currentUserProfileId.value ?: 0,
            message = input,
            isFromUser = true,
            language = _currentLanguage.value
        )
        _messages.value = _messages.value + userMessage
        
        // Move to next step or finish
        if (currentStep < 8) {
            nextStep()
        } else {
            // All steps completed, get recommendations
            saveUserProfileAndGetRecommendations()
        }
    }
}
