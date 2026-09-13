package com.example.recharge.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recharge.theme.*

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onAuthenticated()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            // Brand mark
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(PrimaryContainer, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text("⟳", style = MaterialTheme.typography.displayLarge.copy(color = Primary))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Recharge",
                style = MaterialTheme.typography.headlineLarge,
                color = TextHighEmphasis
            )
            Text(
                text = "Mindful digital wellbeing",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium
            )

            Spacer(Modifier.height(48.dp))

            // Tab toggle: Sign In / Sign Up
            var isSignUp by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceVariant, shape = PillShape)
                    .padding(4.dp)
            ) {
                listOf("Sign In", "Sign Up").forEachIndexed { i, label ->
                    val selected = (i == 1) == isSignUp
                    Button(
                        onClick = { isSignUp = (i == 1) },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Primary else Color.Transparent,
                            contentColor = if (selected) OnPrimary else TextMedium
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            AuthForm(
                isSignUp = isSignUp,
                state = state,
                onSignIn = { email, password -> viewModel.signIn(email, password) },
                onSignUp = { email, password -> viewModel.signUp(email, password) },
                onForgotPassword = { email -> viewModel.sendPasswordReset(email) }
            )

            // Error message
            AnimatedVisibility(state.error != null, enter = fadeIn(), exit = fadeOut()) {
                state.error?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        }
    }
}

@Composable
private fun AuthForm(
    isSignUp: Boolean,
    state: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onForgotPassword: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = TextMedium) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = CardShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
                focusedLabelColor = Primary,
                cursorColor = Primary
            ),
            singleLine = true
        )

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextMedium) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null, tint = TextMedium
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (isSignUp) onSignUp(email, password) else onSignIn(email, password)
            }),
            shape = CardShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
                focusedLabelColor = Primary,
                cursorColor = Primary
            ),
            singleLine = true
        )

        // Primary action button
        Button(
            onClick = { if (isSignUp) onSignUp(email, password) else onSignIn(email, password) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            enabled = !state.isLoading
        ) {
            Text(
                if (isSignUp) "Create Account" else "Sign In",
                style = MaterialTheme.typography.labelLarge,
                color = OnPrimary
            )
        }

        // Forgot password (sign in only)
        if (!isSignUp) {
            TextButton(
                onClick = { onForgotPassword(email) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Forgot password?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
