# KalyanSarathi - Government Scheme Recommendation App

## Overview
KalyanSarathi is an Android application that creates a bilingual interface (English and Hindi) to advise users on government schemes they are eligible for. The application uses Google's Gemini API to provide personalized recommendations based on user profile information.

## Features

### Core Functionality
- **Bilingual Support**: Full support for English and Hindi languages
- **Voice Interaction**: Text-to-Speech (TTS) and Speech-to-Text (STT) capabilities
- **Personalized Recommendations**: AI-powered scheme suggestions based on user profile
- **Chat History**: Save and manage previous chat sessions
- **User-Friendly Interface**: Intuitive design with Material Design 3

### User Experience Flow
1. **Home Screen**: Welcome message with language toggle and navigation options
2. **Chat Interface**: Step-by-step form to collect user information
3. **Recommendations**: AI-generated government scheme suggestions
4. **History Management**: View and delete previous chat sessions

## Technical Architecture

### Technologies Used
- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit
- **Room Database**: Local data persistence
- **Retrofit**: Network communication with Gemini API
- **Navigation Compose**: Screen navigation
- **Material Design 3**: UI components and theming

### Project Structure
```
app/src/main/java/com/example/kalyansarathi/
├── data/                    # Data models and database
│   ├── UserProfile.kt
│   ├── ChatMessage.kt
│   ├── KalyanSarathiDatabase.kt
│   └── Dao.kt
├── network/                 # API services
│   ├── GeminiApiService.kt
│   └── NetworkModule.kt
├── repository/              # Data repository
│   └── KalyanSarathiRepository.kt
├── viewmodel/               # ViewModels
│   ├── HomeViewModel.kt
│   ├── ChatViewModel.kt
│   └── HistoryViewModel.kt
├── ui/screens/              # UI screens
│   ├── HomeScreen.kt
│   ├── ChatScreen.kt
│   └── ChatHistoryScreen.kt
├── navigation/              # Navigation setup
│   └── KalyanSarathiNavigation.kt
├── utils/                   # Utility classes
│   ├── TextToSpeechManager.kt
│   └── SpeechToTextManager.kt
├── MainActivity.kt
└── KalyanSarathiApplication.kt
```

## User Profile Data Collection

The application collects the following information:
1. Phone number
2. Gender (Male, Female, Other)
3. Living area (Rural, Urban)
4. Occupation (Daily Wage Labor/Farmer, Entrepreneur, Student, Employed, Informal sector, Other)
5. Age
6. Caste (Scheduled Caste, Scheduled Tribe, Backward Caste, Other Backward Caste, Other Caste)
7. State/Union Territory
8. Minority religion status (Yes/No)
9. Monthly household income (<5000, 5000-10,000, 10,000-25,000, 25,000-50,000, >50,000)

## Gemini API Integration

The application uses Google's Gemini API with the following prompt structure:
```
Using gender, living area, occupation, Caste, Age, Minority Religion Status, state, Income in Rupees, advise central and state government schemes in India that a user would be eligible for. Also, suggest links to where a person can apply for these schemes. Keep the text short and recommend a minimum of 5 schemes and a maximum of 8 schemes. Ensure that the language of suggestion is in the initial language selected. Ensure that the scheme descriptions provide the user with step through which they can apply for the scheme in addition to a general description. Include links to sites where they can apply online. Give step by step instructions including required documents, and the exact form they have to fill.
```

## Permissions Required

- `RECORD_AUDIO`: For speech-to-text functionality
- `INTERNET`: For API communication
- `ACCESS_NETWORK_STATE`: For network state monitoring

## Setup Instructions

1. **Clone the repository**
2. **Open in Android Studio**
3. **Sync project with Gradle files**
4. **Build and run on device/emulator**

## API Configuration

The Gemini API key is configured in `NetworkModule.kt`. For production use, consider:
- Moving the API key to environment variables
- Implementing proper API key management
- Adding rate limiting and error handling

## Future Enhancements

- Offline mode support
- Enhanced voice recognition accuracy
- More language support
- Push notifications for new schemes
- User account management
- Social sharing features

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.








