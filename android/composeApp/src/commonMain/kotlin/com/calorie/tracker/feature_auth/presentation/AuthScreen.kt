package com.calorie.tracker.feature_auth.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorie.tracker.ui.components.Flip7Button
import com.calorie.tracker.ui.components.Flip7ButtonVariant
import com.calorie.tracker.ui.components.Flip7Card
import com.calorie.tracker.ui.components.Flip7CardVariant
import com.calorie.tracker.ui.components.Flip7TextField
import com.calorie.tracker.ui.theme.*

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
    onGoogleSignInClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var isLoginMode by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Minimal Logo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Black, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Caloriyaan",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Card with form
            Flip7Card(
                modifier = Modifier.fillMaxWidth(),
                variant = Flip7CardVariant.MONOCHROME
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tab switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Flip7Button(
                            text = "Log In",
                            variant = if (isLoginMode) Flip7ButtonVariant.MONOCHROME else Flip7ButtonVariant.GRAY,
                            modifier = Modifier.weight(1f),
                            onClick = { isLoginMode = true }
                        )
                        Flip7Button(
                            text = "Sign Up",
                            variant = if (!isLoginMode) Flip7ButtonVariant.MONOCHROME else Flip7ButtonVariant.GRAY,
                            modifier = Modifier.weight(1f),
                            onClick = { isLoginMode = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedContent(
                        targetState = isLoginMode,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "auth_form"
                    ) { isLogin ->
                        if (isLogin) {
                            LoginForm(
                                isLoading = uiState.isLoading,
                                onLogin = { email, password ->
                                    viewModel.login(email, password)
                                }
                            )
                        } else {
                            RegisterForm(
                                isLoading = uiState.isLoading,
                                onRegister = { name, email, password ->
                                    viewModel.register(name, email, password)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Flip7Button(
                        text = "Sign in with Google",
                        variant = Flip7ButtonVariant.MONOCHROME,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        onClick = onGoogleSignInClick
                    )

                    // Error message
                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Oops, try again! $error",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun LoginForm(
    isLoading: Boolean,
    onLogin: (email: String, password: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Flip7TextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            modifier = Modifier.fillMaxWidth()
        )
        Flip7TextField(
            value = password,
            onValueChange = { password = it },
            label = "Secret Password",
            modifier = Modifier.fillMaxWidth(),
            isPassword = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Flip7Button(
                text = "Log In",
                variant = Flip7ButtonVariant.MONOCHROME,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onLogin(email, password) }
            )
        }
    }
}

@Composable
private fun RegisterForm(
    isLoading: Boolean,
    onRegister: (name: String, email: String, password: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Flip7TextField(
            value = name,
            onValueChange = { name = it },
            label = "Your Name",
            modifier = Modifier.fillMaxWidth()
        )
        Flip7TextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            modifier = Modifier.fillMaxWidth()
        )
        Flip7TextField(
            value = password,
            onValueChange = { password = it },
            label = "Secret Password",
            modifier = Modifier.fillMaxWidth(),
            isPassword = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Flip7Button(
                text = "Sign Up",
                variant = Flip7ButtonVariant.MONOCHROME,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onRegister(name, email, password) }
            )
        }
    }
}
