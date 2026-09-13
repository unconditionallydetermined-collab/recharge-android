package com.example.recharge.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val lineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lineProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar (Recharge text removed, only Settings remains)
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

            // Center decoration (Tablet with stylus, white outlines on black)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TabletDecoration()
            }

            // Attention line flowing down toward the button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .drawBehind {
                        val centerX = size.width / 2
                        val lineHeight = size.height
                        val dotY = lineProgress * lineHeight
                        val dotRadius = 3.dp.toPx()

                        // Faint vertical line
                        drawLine(
                            color = Color.White.copy(alpha = 0.1f),
                            start = Offset(centerX, 0f),
                            end = Offset(centerX, lineHeight),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Glowing dot traveling down
                        drawCircle(
                            color = Color.White.copy(alpha = glowAlpha * 1.5f),
                            radius = dotRadius,
                            center = Offset(centerX, dotY)
                        )
                        // Glow halo
                        drawCircle(
                            color = Color.White.copy(alpha = glowAlpha * 0.5f),
                            radius = dotRadius * 3,
                            center = Offset(centerX, dotY)
                        )
                    }
            )

            // THE BUTTON — bottom half, thumbs reach (White button, Black text)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                val buttonText: String
                val buttonSubtext: String
                val buttonEnabled: Boolean
                val buttonAction: () -> Unit

                // The user requested to prioritize RECHARGE unless queue is empty.
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

                // Outer white glow behind button
                if (buttonEnabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(32.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = glowAlpha),
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

@Composable
fun TabletDecoration() {
    Canvas(modifier = Modifier.size(240.dp)) {
        val strokeWidth = 4.dp.toPx()
        val thinStroke = 2.dp.toPx()
        val white = Color.White
        
        // Tablet body
        val tabletWidth = size.width * 0.6f
        val tabletHeight = size.height * 0.8f
        val topLeftX = (size.width - tabletWidth) / 2
        val topLeftY = (size.height - tabletHeight) / 2
        
        drawRoundRect(
            color = white,
            topLeft = Offset(topLeftX, topLeftY),
            size = Size(tabletWidth, tabletHeight),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        
        // Screen inner rectangle
        drawRoundRect(
            color = white,
            topLeft = Offset(topLeftX + 8.dp.toPx(), topLeftY + 20.dp.toPx()),
            size = Size(tabletWidth - 16.dp.toPx(), tabletHeight - 40.dp.toPx()),
            style = Stroke(width = thinStroke)
        )
        
        // Stylus pen diagonal
        val penEndX = topLeftX + tabletWidth / 2 + 10.dp.toPx()
        val penEndY = topLeftY + tabletHeight * 0.65f
        val penStartX = topLeftX + tabletWidth + 30.dp.toPx()
        val penStartY = topLeftY + 10.dp.toPx()
        
        drawLine(
            color = white,
            start = Offset(penStartX, penStartY),
            end = Offset(penEndX, penEndY),
            strokeWidth = strokeWidth * 1.5f,
            cap = StrokeCap.Round
        )
        
        // Stylus tip (triangle pointing at the end of the pen)
        val path = Path().apply {
            moveTo(penEndX, penEndY)
            lineTo(penEndX - 8.dp.toPx(), penEndY + 16.dp.toPx())
            lineTo(penEndX + 10.dp.toPx(), penEndY + 6.dp.toPx())
            close()
        }
        drawPath(path, color = white)
        
        // Writing lines
        val lineStartX = topLeftX + 24.dp.toPx()
        val line1Y = topLeftY + 48.dp.toPx()
        drawLine(white, Offset(lineStartX, line1Y), Offset(lineStartX + 48.dp.toPx(), line1Y), strokeWidth = thinStroke, cap = StrokeCap.Round)
        
        val line2Y = line1Y + 24.dp.toPx()
        drawLine(white, Offset(lineStartX, line2Y), Offset(lineStartX + 70.dp.toPx(), line2Y), strokeWidth = thinStroke, cap = StrokeCap.Round)
        
        val line3Y = line2Y + 24.dp.toPx()
        drawLine(white, Offset(lineStartX, line3Y), Offset(lineStartX + 36.dp.toPx(), line3Y), strokeWidth = thinStroke, cap = StrokeCap.Round)
    }
}
