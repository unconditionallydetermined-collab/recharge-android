package com.example.recharge.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recharge.AppRoute
import com.example.recharge.data.room.QueueItemEntity
import com.example.recharge.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (AppRoute) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onNavigate(AppRoute.Settings) }) {
                            Icon(Icons.Default.Tune, null, tint = TextMedium)
                        }
                    }
                }

                // Current state + action buttons
                item {
                    CurrentStateCard(
                        showRecharge = state.showRecharge,
                        showRedirect = state.showRedirect,
                        isQueueEmpty = state.isQueueEmpty,
                        nextAppName = state.nextAppName,
                        onRecharge = { onNavigate(AppRoute.Quotes) },
                        onRedirect = { viewModel.launchRedirect(); onNavigate(AppRoute.Home) },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // Queue section header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LibraryAdd, null, tint = Primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Active Intentional Queue",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextHighEmphasis
                            )
                        }
                        TextButton(onClick = { onNavigate(AppRoute.QueueManager) }) {
                            Text("Manage", color = Primary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Queue items
                items(state.queueItems, key = { it.id }) { item ->
                    QueueItemRow(
                        item = item,
                        currentIndex = state.queueIndex,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}







@Composable
private fun CurrentStateCard(
    showRecharge: Boolean,
    showRedirect: Boolean,
    isQueueEmpty: Boolean,
    nextAppName: String,
    onRecharge: () -> Unit,
    onRedirect: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showRecharge) {
        BigMinimalButton(
            text = if (isQueueEmpty) "Empty" else "RECHARGE",
            subtext = if (isQueueEmpty) "Add apps to queue" else "Start mindful protocol",
            onClick = onRecharge,
            enabled = !isQueueEmpty,
            modifier = modifier
        )
    } else if (showRedirect) {
        BigMinimalButton(
            text = if (isQueueEmpty) "Empty" else "REDIRECT",
            subtext = if (isQueueEmpty) "Add apps to queue" else "Launch $nextAppName",
            onClick = onRedirect,
            enabled = !isQueueEmpty,
            modifier = modifier
        )
    } else {
        BigMinimalButton(
            text = "STANDBY",
            subtext = "Queue is empty",
            onClick = {},
            enabled = false,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BigMinimalButton(
    text: String,
    subtext: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = CardShape,
        color = if (enabled) Primary else SurfaceVariant,
        contentColor = if (enabled) OnPrimary else TextMedium
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) OnPrimary.copy(alpha = 0.7f) else TextMedium.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun QueueItemRow(item: QueueItemEntity, currentIndex: Int, modifier: Modifier = Modifier) {
    val position = item.position
    val isActive = position == currentIndex
    val isNext = position == currentIndex + 1

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) PrimaryContainer else Surface
        ),
        shape = CardShape,
        border = if (isActive) CardDefaults.outlinedCardBorder().copy(width = 1.5.dp) else CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(if (isActive) Primary else SurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${position + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isActive) OnPrimary else TextMedium
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.appName, style = MaterialTheme.typography.titleMedium, color = TextHighEmphasis, fontWeight = FontWeight.SemiBold)
                Text("Focus & Systems", style = MaterialTheme.typography.bodySmall, color = TextMedium)
            }
            Surface(
                shape = PillShape,
                color = when {
                    isActive -> Primary
                    isNext -> SurfaceVariant
                    else -> SurfaceVariant
                }
            ) {
                Text(
                    text = when {
                        isActive -> "ACTIVE"
                        isNext -> "NEXT"
                        else -> "PENDING"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isActive -> OnPrimary
                        else -> TextMedium
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
