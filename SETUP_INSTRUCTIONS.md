# KalyanSarathi - Government Scheme Discovery Bot

KalyanSarathi is an Android application that helps users discover central and state government schemes they may be eligible for. The app provides personalized recommendations based on user profile information.

## Features

- **Bilingual Support**: Available in English and Hindi
- **Voice Input/Output**: Speech-to-text and text-to-speech capabilities
- **Personalized Recommendations**: AI-powered scheme recommendations based on user profile
- **Chat History**: Save and manage previous chat sessions
- **User Profile Management**: Comprehensive user information collection

## Setup Instructions

### 1. API Key Configuration

To use the Gemini AI features, you need to set up your API key:

1. Get a Gemini API key from [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Replace `YOUR_GEMINI_API_KEY_HERE` in the following files with your actual API key:
   - `app/src/main/java/com/example/kalyansarathi/network/NetworkModule.kt`
   - `app/src/main/java/com/example/kalyansarathi/viewmodel/ChatViewModel.kt`

### 2. Build and Run

1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Build and run the application

## App Structure

### Data Layer
- **Room Database**: Local storage for user profiles, chat messages, and sessions
- **Repository Pattern**: Centralized data access layer
- **Network Module**: Retrofit-based API client for Gemini integration

### UI Layer
- **Jetpack Compose**: Modern declarative UI framework
- **Navigation**: Single-activity navigation between screens
- **Material Design 3**: Modern design system

### Features
- **Home Screen**: App introduction and navigation
- **Chat Screen**: Interactive form and AI chat interface
- **History Screen**: View and manage chat sessions

## Permissions

The app requires the following permissions:
- `RECORD_AUDIO`: For voice input functionality
- `INTERNET`: For API calls to Gemini
- `ACCESS_NETWORK_STATE`: For network connectivity checks

## Architecture

The app follows MVVM (Model-View-ViewModel) architecture with:
- **ViewModels**: Handle business logic and UI state
- **Compose UI**: Reactive UI components
- **Repository**: Data access abstraction
- **Room Database**: Local data persistence

## Development Notes

- The app uses Kotlin with Jetpack Compose
- Room database for local storage
- Retrofit for network communication
- Material Design 3 for UI components
- Speech recognition and text-to-speech for accessibility

## Security Considerations

- API keys should be stored securely in production
- Consider using environment variables or BuildConfig for sensitive data
- The current implementation uses placeholder API keys for security

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
