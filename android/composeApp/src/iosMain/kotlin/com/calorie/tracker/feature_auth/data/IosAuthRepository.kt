package com.calorie.tracker.feature_auth.data

import com.calorie.tracker.core.network.CalorieApiClient
import com.calorie.tracker.feature_auth.domain.AuthRepository
import com.calorie.tracker.model.LoginRequest
import com.calorie.tracker.model.RegisterRequest
import platform.Foundation.NSUserDefaults

class IosAuthRepository(
    private val apiClient: CalorieApiClient
) : AuthRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_ONBOARDING = "has_completed_onboarding"
    }

    override suspend fun login(email: String, password: String): Result<String> {
        return apiClient.login(LoginRequest(email, password)).map { it.token }.also { result ->
            result.getOrNull()?.let { token ->
                apiClient.setAuthToken(token)
            }
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<String> {
        return apiClient.loginWithGoogle(idToken).map { it.token }.also { result ->
            result.getOrNull()?.let { token ->
                apiClient.setAuthToken(token)
            }
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<String> {
        return apiClient.register(RegisterRequest(name, email, password)).map { it.token }.also { result ->
            result.getOrNull()?.let { token -> apiClient.setAuthToken(token) }
        }
    }

    override fun saveToken(token: String) {
        userDefaults.setObject(token, forKey = KEY_TOKEN)
        apiClient.setAuthToken(token)
    }

    override fun getToken(): String? {
        return userDefaults.stringForKey(KEY_TOKEN)
    }

    override fun clearToken() {
        userDefaults.removeObjectForKey(KEY_TOKEN)
        userDefaults.removeObjectForKey(KEY_ONBOARDING)
        apiClient.clearAuthToken()
    }

    override fun isLoggedIn(): Boolean {
        val token = getToken()
        if (token != null) {
            apiClient.setAuthToken(token)
            return true
        }
        return false
    }

    override fun hasCompletedOnboarding(): Boolean {
        return userDefaults.boolForKey(KEY_ONBOARDING)
    }

    override fun setHasCompletedOnboarding(completed: Boolean) {
        userDefaults.setBool(completed, forKey = KEY_ONBOARDING)
    }
}
