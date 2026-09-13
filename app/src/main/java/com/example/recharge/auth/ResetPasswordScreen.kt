package com.example.recharge.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recharge.theme.*

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var newPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = TextHighEmphasis)
                }
            }
            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(PrimaryContainer, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, null, tint = Primary, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(24.dp))
            Text("Reset Password", style = MaterialTheme.typography.headlineLarge, color = TextHighEmphasis)
            Text("Enter your new password below", style = MaterialTheme.typography.bodyMedium, color = TextMedium)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                shape = CardShape,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.updatePassword(newPassword) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Update Password", style = MaterialTheme.typography.labelLarge, color = OnPrimary)
            }

            state.error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Primary)
            }
        }
    }
}
