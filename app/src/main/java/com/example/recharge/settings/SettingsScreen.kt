package com.example.recharge.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recharge.AppRoute
import com.example.recharge.data.room.QueueItemEntity
import com.example.recharge.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateTo: (AppRoute) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.updateMessage) {
        state.updateMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearUpdateMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Queue & Mindful Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Mindful Utility Mode banner
            item {
                MindfulModeBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // Queue section header
            item {
                SectionHeader(
                    icon = Icons.Default.Sort,
                    title = "Focus App Queue",
                    subtitle = "Top app launches first",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // Queue items (drag reorderable)
            items(state.queueItems, key = { it.id }) { item ->
                QueueManageRow(
                    item = item,
                    currentIndex = state.queueIndex,
                    onDelete = { viewModel.deleteQueueItem(item) },
                    onMoveUp = {
                        val idx = state.queueItems.indexOf(item)
                        if (idx > 0) viewModel.reorderQueue(idx, idx - 1)
                    },
                    onMoveDown = {
                        val idx = state.queueItems.indexOf(item)
                        if (idx < state.queueItems.size - 1) viewModel.reorderQueue(idx, idx + 1)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Add app button
            item {
                OutlinedButton(
                    onClick = { viewModel.showAppScanner() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(52.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = PrimaryContainer),
                    border = null
                ) {
                    Icon(Icons.Default.AddCircleOutline, null, tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Target App Package", color = Primary, style = MaterialTheme.typography.labelLarge)
                }
            }

            // Quote screens lock section
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(
                    icon = Icons.Default.Lock,
                    title = "Quote Screens Lock",
                    subtitle = "Anti-Relapse",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                QuoteEditLockCard(
                    isLocked = state.isQuoteEditLocked,
                    lockExpiresAt = state.quoteLockExpiresAt,
                    currentQuote = state.firstQuoteText,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Quote editor (visible only when not locked)
            if (!state.isQuoteEditLocked) {
                item {
                    AnimatedVisibility(visible = state.isEditingQuotes) {
                        QuoteEditorSection(
                            quotes = state.editableQuotes,
                            onQuoteChange = { idx, text -> viewModel.updateEditableQuote(idx, text) },
                            onSave = { viewModel.saveQuotes() },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    if (!state.isEditingQuotes) {
                        TextButton(
                            onClick = { viewModel.startEditingQuotes() },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Quote Screens", color = Primary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // System permissions section
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader(
                    icon = Icons.Default.Shield,
                    title = "System Permissions",
                    subtitle = "Android 14+",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                PermissionsPanel(
                    context = context,
                    hasUsageStats = state.hasUsageStatsPermission,
                    hasForegroundService = state.hasForegroundService,
                    hasBatteryExemption = state.hasBatteryExemption,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Updates section
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader(
                    icon = Icons.Default.SystemUpdateAlt,
                    title = "Remote Updates",
                    subtitle = "Sideloading",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                UpdatePanel(
                    onCheckUpdate = { viewModel.checkForUpdates() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Video section
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader(
                    icon = Icons.Default.VideoLibrary,
                    title = "Recharge Video",
                    subtitle = "Custom YouTube Link",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                YoutubeSettingsPanel(
                    youtubeUrl = state.youtubeUrl,
                    onUrlChange = { viewModel.updateYoutubeUrlInput(it) },
                    onSave = { viewModel.saveYoutubeUrl() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // App scanner bottom sheet
    if (state.showAppScanner) {
        AppScannerSheet(
            installedApps = state.installedApps,
            onDismiss = viewModel::hideAppScanner,
            onSelect = viewModel::addToQueue
        )
    }
}

@Composable
private fun MindfulModeBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
        shape = CardShape
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Spa, null, tint = OnPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Mindful Utility Mode", style = MaterialTheme.typography.titleMedium, color = TextHighEmphasis)
                Text("Strict boundaries, deep mental space", style = MaterialTheme.typography.bodySmall, color = TextMedium)
            }
            Surface(shape = PillShape, color = Primary) {
                Text("Active", style = MaterialTheme.typography.labelMedium, color = OnPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextHighEmphasis, modifier = Modifier.weight(1f))
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMedium)
    }
}

@Composable
private fun QueueManageRow(
    item: QueueItemEntity,
    currentIndex: Int,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isActive = item.position == currentIndex
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = CardShape,
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reorder controls
            Column {
                IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.KeyboardArrowUp, "Move up", tint = TextMedium, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, "Move down", tint = TextMedium, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(if (isActive) Primary else SurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.appName.first().uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isActive) OnPrimary else TextMedium
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.appName, style = MaterialTheme.typography.titleMedium, color = TextHighEmphasis)
                Text(item.packageName.substringAfterLast("."), style = MaterialTheme.typography.bodySmall, color = TextMedium)
            }
            Surface(
                shape = PillShape,
                color = if (isActive) PrimaryContainer else SurfaceVariant
            ) {
                Text(
                    if (isActive) "ACTIVE" else "QUEUED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) PrimaryDark else TextMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, "Remove", tint = TextMedium, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun QuoteEditLockCard(
    isLocked: Boolean,
    lockExpiresAt: Long,
    currentQuote: String,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Column(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = CardShape,
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SurfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = TextMedium, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (isLocked) "Locked for 3 Days" else "Unlocked", style = MaterialTheme.typography.titleMedium, color = TextHighEmphasis)
                    Text(
                        if (isLocked) "Editable in ${formatTimeUntil(lockExpiresAt)} to curb impulsive resets"
                        else "Edit your quote screens",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium
                    )
                }
                if (isLocked) {
                    Surface(shape = PillShape, color = SurfaceVariant) {
                        Text("64%\nDone", style = MaterialTheme.typography.labelSmall, color = TextMedium,
                            textAlign = TextAlign.Center, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        if (currentQuote.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                shape = CardShape
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MORNING ANCHOR QUOTE", style = MaterialTheme.typography.labelSmall, color = TextMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = TextMedium, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Immutable", style = MaterialTheme.typography.labelSmall, color = TextMedium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "\"$currentQuote\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextHighEmphasis,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("— Marcus Aurelius", style = MaterialTheme.typography.bodySmall, color = TextMedium, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun PermissionsPanel(
    context: Context,
    hasUsageStats: Boolean,
    hasForegroundService: Boolean,
    hasBatteryExemption: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = CardShape,
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PermissionRow(
                icon = Icons.Default.CheckCircle,
                label = "PACKAGE_USAGE_S...",
                description = "Interception & app switch audits",
                status = if (hasUsageStats) "Granted" else "Required",
                statusColor = if (hasUsageStats) Primary else Color.Red,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            )
            HorizontalDivider(color = Border)
            PermissionRow(
                icon = Icons.Default.CheckCircle,
                label = "Foreground Service",
                description = "Persistent status notification",
                status = if (hasForegroundService) "Active" else "Inactive",
                statusColor = if (hasForegroundService) Primary else TextMedium,
                onClick = {}
            )
            HorizontalDivider(color = Border)
            PermissionRow(
                icon = Icons.Default.BatteryStd,
                label = "Battery Exemption",
                description = "Prevents OEM background kill",
                status = if (hasBatteryExemption) "Granted" else "Grant",
                statusColor = if (hasBatteryExemption) Primary else Color.Transparent,
                statusBg = if (hasBatteryExemption) Color.Transparent else Primary,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    label: String,
    description: String,
    status: String,
    statusColor: Color,
    statusBg: Color = Color.Transparent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(PrimaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = TextHighEmphasis)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextMedium)
        }
        if (statusBg != Color.Transparent) {
            Button(
                onClick = onClick,
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = statusBg),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(status, style = MaterialTheme.typography.labelSmall, color = OnPrimary)
            }
        } else {
            Surface(shape = PillShape, color = statusColor.copy(alpha = 0.1f)) {
                Text(status, style = MaterialTheme.typography.labelSmall, color = statusColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScannerSheet(
    installedApps: List<InstalledApp>,
    onDismiss: () -> Unit,
    onSelect: (InstalledApp) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Installed Apps", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Border
                )
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn {
                items(filteredApps, key = { it.packageName }) { app ->
                    ListItem(
                        headlineContent = { Text(app.name) },
                        supportingContent = { Text(app.packageName, style = MaterialTheme.typography.bodySmall) },
                        trailingContent = {
                            IconButton(onClick = { onSelect(app); onDismiss() }) {
                                Icon(Icons.Default.Add, null, tint = Primary)
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun formatTimeUntil(timestamp: Long): String {
    val diff = timestamp - System.currentTimeMillis()
    if (diff <= 0) return "now"
    val hours = diff / (1000 * 60 * 60)
    val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
    return "${hours}h ${minutes}m"
}

@Composable
private fun QuoteEditorSection(
    quotes: List<String>,
    onQuoteChange: (Int, String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = CardShape,
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Edit Quotes", style = MaterialTheme.typography.titleMedium, color = TextHighEmphasis)
            Text("These will be shown before launching apps.", style = MaterialTheme.typography.bodySmall, color = TextMedium)
            Spacer(Modifier.height(16.dp))
            
            quotes.forEachIndexed { index, quote ->
                OutlinedTextField(
                    value = quote,
                    onValueChange = { onQuoteChange(index, it) },
                    label = { Text("Quote ${index + 1}") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = PillShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Border
                    )
                )
                Spacer(Modifier.height(8.dp))
            }
            
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save & Lock for 3 Days", color = OnPrimary)
            }
        }
    }
}

@Composable
private fun UpdatePanel(
    onCheckUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = CardShape,
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SystemUpdate, null, tint = Primary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("App Updates", style = MaterialTheme.typography.titleMedium, color = TextHighEmphasis)
                    Text("Check for new versions", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                }
                Button(
                    onClick = onCheckUpdate,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text("Check", style = MaterialTheme.typography.labelSmall, color = OnPrimary)
                }
            }
        }
    }
}

@Composable
fun YoutubeSettingsPanel(
    youtubeUrl: String,
    onUrlChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = CardShape,
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = youtubeUrl,
                onValueChange = onUrlChange,
                label = { Text("YouTube URL", color = TextMedium) },
                modifier = Modifier.fillMaxWidth(),
                shape = PillShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Border
                ),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Save, null, tint = OnPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Video", color = OnPrimary)
            }
        }
    }
}
