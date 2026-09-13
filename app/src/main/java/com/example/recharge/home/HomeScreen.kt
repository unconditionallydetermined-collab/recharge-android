package com.example.recharge.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recharge.AppRoute
import com.example.recharge.data.room.QueueItemEntity
import com.example.recharge.theme.*

@Composable
fun HomeScreen(
    onNavigate: (AppRoute) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Neon glow color
    val neonColor = Color(0xFF00E5A0)
    val neonColorAlt = Color(0xFF00B4D8)

    // Animated glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    // Animated line position flowing down toward button
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
            .background(Color(0xFF0A0A0F))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar with settings icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "recharge",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 3.sp
                )
                IconButton(onClick = { onNavigate(AppRoute.Settings) }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Queue section - scrollable top half
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section label
                item {
                    Text(
                        "ACTIVE QUEUE",
                        color = neonColor.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                    )
                }

                if (state.queueItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .clickable { onNavigate(AppRoute.Settings) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Add,
                                    null,
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Add apps to queue",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(state.queueItems, key = { it.id }) { item ->
                        QueueTile(
                            item = item,
                            currentIndex = state.queueIndex,
                            neonColor = neonColor
                        )
                    }
                }

                // Manage queue link
                if (state.queueItems.isNotEmpty()) {
                    item {
                        Text(
                            "Manage →",
                            color = Color.White.copy(alpha = 0.25f),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { onNavigate(AppRoute.Settings) }
                        )
                    }
                }
            }

            // Neon flow line drawing attention down to the button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .drawBehind {
                        val centerX = size.width / 2
                        val lineHeight = size.height
                        val dotY = lineProgress * lineHeight
                        val dotRadius = 3.dp.toPx()

                        // Faint vertical line
                        drawLine(
                            color = neonColor.copy(alpha = 0.15f),
                            start = Offset(centerX, 0f),
                            end = Offset(centerX, lineHeight),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Glowing dot traveling down
                        drawCircle(
                            color = neonColor.copy(alpha = glowAlpha),
                            radius = dotRadius,
                            center = Offset(centerX, dotY)
                        )
                        // Glow halo
                        drawCircle(
                            color = neonColor.copy(alpha = glowAlpha * 0.3f),
                            radius = dotRadius * 3,
                            center = Offset(centerX, dotY)
                        )
                    }
            )

            // THE BUTTON — bottom half, thumbs reach
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

                if (state.isQueueEmpty) {
                    buttonText = "EMPTY"
                    buttonSubtext = "Add apps in settings"
                    buttonEnabled = false
                    buttonAction = {}
                } else if (state.showRecharge) {
                    buttonText = "RECHARGE"
                    buttonSubtext = "Start mindful protocol"
                    buttonEnabled = true
                    buttonAction = { onNavigate(AppRoute.Quotes) }
                } else if (state.showRedirect) {
                    buttonText = "REDIRECT"
                    buttonSubtext = "Launch ${state.nextAppName}"
                    buttonEnabled = true
                    buttonAction = { viewModel.launchRedirect(); onNavigate(AppRoute.Home) }
                } else {
                    buttonText = "STANDBY"
                    buttonSubtext = "Cooldown active"
                    buttonEnabled = false
                    buttonAction = {}
                }

                // Outer neon glow behind button
                if (buttonEnabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(24.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        neonColor.copy(alpha = glowAlpha * 0.4f),
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
                            if (buttonEnabled) Modifier.drawBehind {
                                drawRoundRect(
                                    color = neonColor.copy(alpha = glowAlpha * 0.6f),
                                    cornerRadius = CornerRadius(20.dp.toPx()),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            } else Modifier.border(
                                1.dp,
                                Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(20.dp)
                            )
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = if (buttonEnabled) Color(0xFF0F1A18) else Color(0xFF111116),
                    contentColor = Color.White
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
                            color = if (buttonEnabled) neonColor else Color.White.copy(alpha = 0.2f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = buttonSubtext,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (buttonEnabled) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun QueueTile(
    item: QueueItemEntity,
    currentIndex: Int,
    neonColor: Color
) {
    val position = item.position
    val isActive = position == currentIndex
    val isNext = position == currentIndex + 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) Color.White.copy(alpha = 0.06f)
                else Color.White.copy(alpha = 0.02f)
            )
            .then(
                if (isActive) Modifier.border(
                    1.dp,
                    neonColor.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                else Modifier.border(
                    1.dp,
                    Color.White.copy(alpha = 0.05f),
                    RoundedCornerShape(12.dp)
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position indicator
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    if (isActive) neonColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${position + 1}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) neonColor else Color.White.copy(alpha = 0.3f)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.appName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = if (isActive) 0.9f else 0.5f)
            )
        }
        // Status chip
        Text(
            text = when {
                isActive -> "ACTIVE"
                isNext -> "NEXT"
                else -> "QUEUED"
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = when {
                isActive -> neonColor
                else -> Color.White.copy(alpha = 0.2f)
            },
            modifier = Modifier
                .border(
                    1.dp,
                    if (isActive) neonColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
