package com.example.recharge.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recharge.AppRoute

@Composable
fun HomeScreen(
    onNavigate: (AppRoute) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Minimalist glow animations
    val infiniteTransition = rememberInfiniteTransition(label = "attention")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    // 3 staggered lines
    val line1Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "line1Progress"
    )
    val line2Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing, delayMillis = 500),
            repeatMode = RepeatMode.Restart
        ),
        label = "line2Progress"
    )
    val line3Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing, delayMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "line3Progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(AppRoute.Settings) }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Center decoration (Solid white tablet with uniform bezels, overflowing top edge)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .offset(y = (-80).dp), // Moves it up to flow out of viewable area
                contentAlignment = Alignment.Center
            ) {
                TabletDecoration(glowAlpha)
            }

            // Attention lines flowing down toward the button (multiple lines)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .drawBehind {
                        val centerX = size.width / 2
                        val lineHeight = size.height
                        val dotRadius = 2.dp.toPx()
                        
                        val spacing = 32.dp.toPx()

                        // Helper function to draw a line + dot
                        fun drawFlowLine(xOffset: Float, progress: Float, alphaMult: Float = 1f) {
                            val xPos = centerX + xOffset
                            val dotY = progress * lineHeight

                            // Faint vertical line
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f * alphaMult),
                                start = Offset(xPos, 0f),
                                end = Offset(xPos, lineHeight),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Glowing dot
                            drawCircle(
                                color = Color.White.copy(alpha = glowAlpha * alphaMult),
                                radius = dotRadius,
                                center = Offset(xPos, dotY)
                            )
                        }

                        drawFlowLine(-spacing, line2Progress, 0.6f)
                        drawFlowLine(0f, line1Progress, 1f)
                        drawFlowLine(spacing, line3Progress, 0.6f)
                    }
            )

            // Determine button states
            val buttonText: String
            val buttonSubtext: String
            val buttonEnabled: Boolean
            val buttonAction: () -> Unit

            if (state.isQueueEmpty) {
                buttonText = "EMPTY"
                buttonSubtext = "Add apps in settings"
                buttonEnabled = false
                buttonAction = {}
            } else if (state.showRedirect) {
                buttonText = "REDIRECT"
                buttonSubtext = "Launch ${state.nextAppName}"
                buttonEnabled = true
                buttonAction = { viewModel.launchRedirect(); onNavigate(AppRoute.Home) }
            } else {
                buttonText = "RECHARGE"
                buttonSubtext = "Start mindful protocol"
                buttonEnabled = true
                buttonAction = { onNavigate(AppRoute.Quotes) }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Small non-prominent text button above the prominent button
                if (buttonText == "REDIRECT") {
                    TextButton(
                        onClick = { onNavigate(AppRoute.Quotes) },
                        modifier = Modifier.padding(bottom = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "RECHARGE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 4.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                } else if (buttonText == "RECHARGE" && state.showRedirect && !state.isQueueEmpty) {
                    TextButton(
                        onClick = { viewModel.launchRedirect(); onNavigate(AppRoute.Home) },
                        modifier = Modifier.padding(bottom = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "REDIRECT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 4.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                // THE PROMINENT BUTTON — bottom half, thumbs reach (White button, Black text)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer white glow behind redirect button
                    if (buttonEnabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp) // Stronger glow covering more area
                                .blur(48.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = glowAlpha * 0.8f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        )
                    }

                    Surface(
                        onClick = buttonAction,
                        enabled = buttonEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .then(
                                if (!buttonEnabled) Modifier.border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(20.dp)
                                ) else Modifier
                            ),
                        shape = RoundedCornerShape(20.dp),
                        color = if (buttonEnabled) Color.White else Color(0xFF111116),
                        contentColor = if (buttonEnabled) Color.Black else Color.White.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = buttonText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 6.sp,
                                color = if (buttonEnabled) Color.Black else Color.White.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = buttonSubtext,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (buttonEnabled) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabletDecoration(glowAlpha: Float = 0.5f) {
    // Scaled up rectangle flowing out of viewable area with glowing sides
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(width = 280.dp, height = 385.dp)
    ) {
        // 1. Ambient outer aura
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(32.dp)
                .background(Color.White.copy(alpha = glowAlpha * 0.5f), RoundedCornerShape(24.dp))
        )

        // 2. Left side glow (flaring outward)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-12).dp)
                .width(36.dp)
                .fillMaxHeight(0.85f)
                .blur(16.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = glowAlpha * 0.95f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 3. Right side glow (flaring outward)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 12.dp)
                .width(36.dp)
                .fillMaxHeight(0.85f)
                .blur(16.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = glowAlpha * 0.95f)
                        )
                    )
                )
        )

        // 4. Main rectangle tablet body with uniform bezel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(14.dp) // Uniform bezel thickness
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black, RoundedCornerShape(16.dp))
            )
        }
    }
}
