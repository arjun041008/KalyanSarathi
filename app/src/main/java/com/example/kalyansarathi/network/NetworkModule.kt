package com.example.kalyansarathi.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    // Note: In production, this should be stored securely (e.g., in BuildConfig or environment variables)
    // For development purposes, you can set this in your local.properties file
    private const val API_KEY = "AIzaSyCXB9nKAVYkXLdxOV-_9AFWoKeCQwc29X8"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)  // Increased from 30 to 60 seconds
        .readTimeout(120, TimeUnit.SECONDS)   // Increased from 30 to 120 seconds for long responses
        .writeTimeout(60, TimeUnit.SECONDS)    // Increased from 30 to 60 seconds
        .callTimeout(150, TimeUnit.SECONDS)   // Added overall call timeout
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val geminiApiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)
    
    // Test function to verify API connectivity
    suspend fun testApiConnection(): Boolean {
        return try {
            val testRequest = GeminiRequest(
                contents = listOf(
                    RequestContent(
                        parts = listOf(
                            RequestPart(text = "Hello, this is a test message. Please respond with 'API connection successful'.")
                        )
                    )
                )
            )
            
            val response = geminiApiService.generateContent(API_KEY, testRequest)
            response.isSuccessful && response.body() != null
        } catch (e: Exception) {
            println("=== API CONNECTION TEST FAILED ===")
            println("Exception: ${e.message}")
            println("=================================")
            false
        }
    }
}


