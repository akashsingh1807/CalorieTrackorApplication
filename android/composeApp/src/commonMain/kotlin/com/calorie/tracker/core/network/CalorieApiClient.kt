package com.calorie.tracker.core.network

import com.calorie.tracker.model.AnalyzeTextResponse
import com.calorie.tracker.model.AuthResponse
import com.calorie.tracker.model.LoginRequest
import com.calorie.tracker.model.Meal
import com.calorie.tracker.model.RegisterRequest
import com.calorie.tracker.model.SignupResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class CalorieApiClient(
    private val baseUrl: String,
    private val httpClient: HttpClient = HttpClient {
        expectSuccess = true
        install(io.ktor.client.plugins.HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 60000
            socketTimeoutMillis = 60000
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }
) {
    private var authToken: String? = null

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }

    private fun HttpRequestBuilder.withAuth() {
        authToken?.let {
            header(HttpHeaders.Authorization, "Bearer $it")
        }
    }

    // ── Auth ────────────────────────────────────────────────────
    suspend fun login(request: LoginRequest): Result<AuthResponse> = runCatching {
        httpClient.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AuthResponse>()
    }

    suspend fun register(request: RegisterRequest): Result<SignupResponse> = runCatching {
        httpClient.post("$baseUrl/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<SignupResponse>()
    }

    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> = runCatching {
        httpClient.post("$baseUrl/api/v1/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("idToken" to idToken))
        }.body<AuthResponse>()
    }

    // ── Users ───────────────────────────────────────────────────
    suspend fun getProfile(): Result<com.calorie.tracker.model.UserProfile> = runCatching {
        httpClient.get("$baseUrl/api/v1/users/me") {
            withAuth()
        }.body<com.calorie.tracker.model.UserProfile>()
    }

    suspend fun updateProfile(request: com.calorie.tracker.model.UpdateProfileRequest): Result<Unit> = runCatching {
        httpClient.put("$baseUrl/api/v1/users/me") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    // ── Meals ────────────────────────────────────────────────────
    suspend fun getDailyMeals(date: String): HttpResponse {
        return httpClient.get("$baseUrl/api/v1/meals/daily") {
            withAuth()
            parameter("date", date)
        }
    }

    suspend fun addMeal(requestBody: String): HttpResponse {
        return httpClient.post("$baseUrl/api/v1/meals") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
    }

    suspend fun analyzeText(text: String): Result<AnalyzeTextResponse> = runCatching {
        httpClient.post("$baseUrl/api/v1/ai/analyze-text") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(com.calorie.tracker.model.AnalyzeTextRequest(text = text))
        }.body<AnalyzeTextResponse>()
    }

    @OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
    suspend fun analyzeMealImage(imageBytes: ByteArray): Result<AnalyzeTextResponse> = runCatching {
        val base64Image = "data:image/jpeg;base64," + kotlin.io.encoding.Base64.Default.encode(imageBytes)
        httpClient.post("$baseUrl/api/v1/ai/detect-food") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("imageUrl" to base64Image))
        }.body<AnalyzeTextResponse>()
    }
}
