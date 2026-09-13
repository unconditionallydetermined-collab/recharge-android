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
    val selectedTab = remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Foreground service banner
            if (state.serviceActive) {
                ServiceBanner(elapsedMinutes = state.serviceElapsedMinutes)
            }

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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Dashboard",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextHighEmphasis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { onNavigate(AppRoute.Settings) }) {
                                Icon(Icons.Default.Tune, null, tint = TextMedium)
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = OnPrimary)
                            }
                        }
                    }
                }

                // Session type tabs
                item {
                    SessionTypeTabs(modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(16.dp))
                }

                // Foreground active card
                item {
                    ForegroundActiveCard(
                        currentApp = state.foregroundApp,
                        onSettings = { onNavigate(AppRoute.Settings) },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(20.dp))
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

            // Bottom navigation
            BottomNavBar(selectedTab = selectedTab.intValue, onTabSelected = {
                selectedTab.intValue = it
                when (it) {
                    0 -> onNavigate(AppRoute.Home)
                    1 -> onNavigate(AppRoute.Habits)
                    2 -> onNavigate(AppRoute.Sounds)
                    3 -> onNavigate(AppRoute.Insights)
                    4 -> onNavigate(AppRoute.Profile)
                }
            })
        }
    }
}

@Composable
private fun ServiceBanner(elapsedMinutes: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
        shape = CardShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Spa, null, tint = OnPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Recharge Service Active", style = MaterialTheme.typography.labelLarge, color = PrimaryDark)
                Text("Mindful Restore · ${elapsedMinutes}m elapsed", style = MaterialTheme.typography.bodySmall, color = TextMedium)
            }
            Icon(Icons.Default.Pause, null, tint = TextMedium)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Close, null, tint = TextMedium)
        }
    }
}

@Composable
private fun SessionTypeTabs(modifier: Modifier = Modifier) {
    val tabs = listOf("Interactive App UI" to Icons.Default.PhoneAndroid, "Android Architecture & Room" to Icons.Default.Code)
    var selected by remember { mutableIntStateOf(0) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceVariant, shape = PillShape)
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { i, (label, icon) ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .background(if (selected == i) Primary else Color.Transparent)
                    .clickable { selected = i }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(icon, null, tint = if (selected == i) OnPrimary else TextMedium, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected == i) OnPrimary else TextMedium)
            }
        }
    }
}

@Composable
private fun ForegroundActiveCard(currentApp: String?, onSettings: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = CardShape,
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(PrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Primary, CircleShape)
                        .align(Alignment.TopEnd)
                )
                Icon(Icons.Default.RadioButtonChecked, null, tint = Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("FOREGROUND ACTIVE · UsageStatsManager", style = MaterialTheme.typography.labelSmall, color = Primary)
                Text(
                    "Recharge Queue active • Current: ${currentApp ?: "…"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextHighEmphasis
                )
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Settings, null, tint = TextMedium, modifier = Modifier.size(18.dp))
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
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = CardShape,
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("CURRENT STATE", style = MaterialTheme.typography.labelSmall, color = TextMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isQueueEmpty) "Queue Empty\nAdd Apps" else "Intentional Mindset\nReady",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextHighEmphasis,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = {},
                    shape = PillShape,
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                ) {
                    Icon(Icons.Default.Tune, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Simulate\nCooldown", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Start Recharge button
            AnimatedVisibility(showRecharge) {
                var pressed by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        pressed = true
                        onRecharge()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .scale(if (pressed) 0.97f else 1f),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = !isQueueEmpty
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SelfImprovement, null, tint = OnPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (isQueueEmpty) "Add Apps First" else "Start Recharge", style = MaterialTheme.typography.titleLarge, color = if (isQueueEmpty) TextMedium else OnPrimary)
                        Text(if (isQueueEmpty) "Queue is empty" else "Video restore & mindful quotes", style = MaterialTheme.typography.bodySmall, color = if (isQueueEmpty) TextMedium else OnPrimary.copy(alpha = 0.7f))
                    }
                    Icon(Icons.Default.ArrowForward, null, tint = OnPrimary)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Launch Redirect button
            AnimatedVisibility(showRedirect) {
                OutlinedButton(
                    onClick = onRedirect,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = PrimaryContainer,
                        contentColor = PrimaryDark
                    ),
                    border = null,
                    enabled = !isQueueEmpty
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.OpenInNew, null, tint = Primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (isQueueEmpty) "No Apps in Queue" else "Launch Redirect", style = MaterialTheme.typography.titleMedium, color = if (isQueueEmpty) TextMedium else PrimaryDark)
                        Text(if (isQueueEmpty) "Add apps in Manage Queue" else "Open current queue app ($nextAppName)", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                    }
                    Icon(Icons.Default.OpenInNew, null, tint = TextMedium, modifier = Modifier.size(16.dp))
                }
            }
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

@Composable
private fun BottomNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        "Restore" to Icons.Default.SelfImprovement,
        "Habits" to Icons.Default.CalendarToday,
        "Sounds" to Icons.Default.Waves,
        "Insights" to Icons.Default.BarChart,
        "Profile" to Icons.Default.Person
    )
    NavigationBar(
        containerColor = Surface,
        tonalElevation = 0.dp
    ) {
        tabs.forEachIndexed { i, (label, icon) ->
            NavigationBarItem(
                selected = selectedTab == i,
                onClick = { onTabSelected(i) },
                icon = { Icon(icon, label, modifier = Modifier.size(22.dp)) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = PrimaryContainer,
                    unselectedIconColor = TextMedium,
                    unselectedTextColor = TextMedium
                )
            )
        }
    }
}
