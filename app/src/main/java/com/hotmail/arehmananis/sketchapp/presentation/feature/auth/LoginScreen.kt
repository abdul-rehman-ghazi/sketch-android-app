package com.hotmail.arehmananis.sketchapp.presentation.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Login screen with Google Sign-In
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Navigate on success
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App branding
            Text(
                text = "✏️ SketchApp",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Create and share beautiful sketches",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // State-based content
            when (uiState) {
                is LoginUiState.Idle -> {
                    GoogleSignInButton(
                        onClick = {
                            scope.launch {
                                val helper = GoogleSignInHelper(context)
                                helper.signIn().fold(
                                    onSuccess = { idToken ->
                                        viewModel.signInWithGoogle(idToken)
                                    },
                                    onFailure = { error ->
                                        viewModel.signInWithGoogle("")
                                    }
                                )
                            }
                        }
                    )
                }

                is LoginUiState.Loading -> {
                    CircularProgressIndicator()
                    Text(
                        text = "Signing in...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                is LoginUiState.Error -> {
                    ErrorContent(
                        message = (uiState as LoginUiState.Error).message,
                        onRetry = {
                            viewModel.resetToIdle()
                        }
                    )
                }

                is LoginUiState.Success -> {
                    // Will navigate away
                    Text(text = "Sign-in successful!")
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = "Sign in with Google",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}
