package com.example.recharge.quotes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recharge.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuoteScreen(
    onFinished: () -> Unit,
    viewModel: QuotesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Flash overlay animation state
    var showFlash by remember { mutableStateOf(false) }
    var flashColor by remember { mutableStateOf(Color.Black) }
    var flashVisible by remember { mutableStateOf(false) }

    // Determine if this is the final (black bg) screen
    val isFinalScreen = state.currentIndex == state.totalCount - 1
    val bgColor = if (isFinalScreen) Color.Black else Background
    val textColor = if (isFinalScreen) Color.White else TextHighEmphasis

    suspend fun doFlashTransition(onEnd: () -> Unit) {
        val colors = listOf(Color.Black, Color(0xFF888888), Color.White, Color.Black, Color(0xFF333333))
        flashVisible = true
        for (c in colors) {
            flashColor = c
            delay(80)
        }
        flashVisible = false
        onEnd()
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            // Step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = PillShape, color = if (isFinalScreen) Color.White.copy(0.1f) else SurfaceVariant) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).background(Primary, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Step ${state.currentIndex + 1} of ${state.totalCount} • Digital Reset",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isFinalScreen) Color.White else TextMedium
                        )
                    }
                }
                TextButton(onClick = onFinished) {
                    Text("Skip", style = MaterialTheme.typography.labelLarge, color = if (isFinalScreen) Color.White.copy(0.5f) else TextMedium)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Close, null, tint = if (isFinalScreen) Color.White.copy(0.5f) else TextMedium, modifier = Modifier.size(16.dp))
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { (state.currentIndex + 1).toFloat() / state.totalCount },
                modifier = Modifier.fillMaxWidth().height(3.dp).padding(horizontal = 24.dp),
                color = Primary,
                trackColor = if (isFinalScreen) Color.White.copy(0.2f) else Border
            )

            Spacer(Modifier.height(32.dp))

            // Section label
            Text(
                "INTENTIONAL PAUSE",
                style = MaterialTheme.typography.labelSmall,
                color = Primary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Quote card (off-white bg, except final dark screen uses a lighter card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isFinalScreen) Color(0xFF1A1A1A) else Surface),
                shape = CardShape,
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("99", style = MaterialTheme.typography.headlineMedium, color = Primary)
                        Spacer(Modifier.width(8.dp))
                        Text("DAILY GROUNDING", style = MaterialTheme.typography.labelSmall, color = TextMedium)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "\"${state.quotes.getOrNull(state.currentIndex) ?: ""}\"",
                        style = MaterialTheme.typography.headlineLarge,
                        color = textColor,
                        textAlign = TextAlign.Start
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Presence over impulse", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Mindful Protocol", style = MaterialTheme.typography.labelSmall, color = Primary)
                        }
                    }
                }
            }

            // Breathing circle (on dark quote screen)
            if (isFinalScreen) {
                Spacer(Modifier.height(48.dp))
                BreathingCircle(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(Modifier.weight(1f))

            // Continue button
            Button(
                onClick = {
                    scope.launch {
                        if (state.currentIndex < state.totalCount - 1) {
                            doFlashTransition { viewModel.nextQuote() }
                        } else {
                            onFinished()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(56.dp),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    if (state.currentIndex < state.totalCount - 1) "Continue" else "Continue to Recharge",
                    style = MaterialTheme.typography.labelLarge,
                    color = OnPrimary
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, tint = OnPrimary)
            }
        }

        // Flash overlay
        if (flashVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(flashColor)
            )
        }
    }
}

@Composable
private fun BreathingCircle(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Primary.copy(alpha = glowAlpha * 0.3f), CircleShape)
        )
        // Middle ring
        Box(
            modifier = Modifier
                .size((160 * scale).dp)
                .background(Color.Transparent, CircleShape)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            )
        }
        // Inner white circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Inhale", style = MaterialTheme.typography.titleMedium, color = Primary)
                Text("1s", style = MaterialTheme.typography.headlineMedium, color = TextHighEmphasis)
                Text("Focus inwardly", style = MaterialTheme.typography.bodySmall, color = TextMedium)
            }
        }
    }
}
