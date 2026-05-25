package com.calorie.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val app = application as CalorieApp
        val credentialManager = CredentialManager.create(this)

        setContent {
            App(
                authRepository = app.authRepository,
                mealRepository = app.mealRepository,
                apiClient = app.apiClient,
                bookmarkRepository = app.bookmarkRepository,
                weightRepository = app.weightRepository,
                waterRepository = app.waterRepository,
                onLogout = {
                    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            app.database.clearAllTables()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                onGoogleSignInClick = { onTokenReceived, onError ->
                    lifecycleScope.launch {
                        try {
                            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId("101851020637-3pifturaj43ied5tv3hlld6mv13s6lte.apps.googleusercontent.com") 
                                .setAutoSelectEnabled(false)
                                .build()

                            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(
                                request = request,
                                context = this@MainActivity
                            )
                            val credential = result.credential
                            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                onTokenReceived(googleIdTokenCredential.idToken)
                            } else {
                                onError("Unexpected credential type")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            onError(e.localizedMessage ?: "Google Sign-In failed")
                        }
                    }
                }
            )
        }
    }
}
