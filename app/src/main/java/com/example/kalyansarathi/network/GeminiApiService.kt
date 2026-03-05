package com.example.kalyansarathi.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class GeminiRequest(
    val contents: List<RequestContent>
)

data class RequestContent(
    val parts: List<RequestPart>
)

data class RequestPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?,
    val error: GeminiError?
)

data class Candidate(
    val content: ResponseContent?,
    val finishReason: String?,
    val index: Int?,
    val safetyRatings: List<SafetyRating>?
)

data class ResponseContent(
    val parts: List<ResponsePart>?,
    val role: String?
)

data class ResponsePart(
    val text: String?
)

data class SafetyRating(
    val category: String?,
    val probability: String?
)

data class GeminiError(
    val code: Int?,
    val message: String?,
    val status: String?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}