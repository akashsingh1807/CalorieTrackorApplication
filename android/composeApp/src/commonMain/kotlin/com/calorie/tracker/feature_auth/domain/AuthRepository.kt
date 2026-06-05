package com.calorie.tracker.feature_auth.domain

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun loginWithGoogle(idToken: String): Result<String>
    suspend fun register(name: String, email: String, password: String): Result<String>
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun isLoggedIn(): Boolean
    fun hasCompletedOnboarding(): Boolean
    fun setHasCompletedOnboarding(completed: Boolean)
}
