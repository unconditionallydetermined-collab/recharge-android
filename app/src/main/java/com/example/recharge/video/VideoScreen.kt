package com.example.recharge.video

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recharge.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoScreen(
    onVideoComplete: () -> Unit,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Start foreground service to keep WebView audio alive during screen-off
        DisposableEffect(state.youtubeUrl) {
            if (state.youtubeUrl.isNotEmpty()) {
                val intent = Intent(context, MediaPlaybackService::class.java)
                context.startForegroundService(intent)
            }
            onDispose {
                context.stopService(Intent(context, MediaPlaybackService::class.java))
            }
        }
        if (state.youtubeUrl.isEmpty()) {
            // URL entry prompt
            UrlEntryPrompt(
                url = state.urlInput,
                onUrlChange = viewModel::onUrlInput,
                onSave = viewModel::saveUrl,
                error = state.urlError
            )
        } else {
            // Video player (YouTube IFrame in WebView)
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Binaural 432Hz", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(0.6f))
                }

                // Service indicator
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White.copy(0.1f),
                    shape = PillShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(6.dp).background(Primary, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "MediaSessionService Active • Screen-off Playback",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(0.8f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // YouTube IFrame player (WebView)
                val videoId = extractYouTubeId(state.youtubeUrl)
                if (videoId != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    settings.javaScriptEnabled = true
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    webViewClient = WebViewClient()
                                    loadData(buildYouTubeHtml(videoId), "text/html", "utf-8")
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Pause/play overlay (center)
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Black.copy(0.4f), CircleShape)
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Video info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D0D0D))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text("MINDFUL RESTORATION", style = MaterialTheme.typography.labelSmall, color = TextMedium)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Deep Forest Respiration", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text("03:45 / 05:00", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    // Progress bar
                    LinearProgressIndicator(
                        progress = { 0.75f },
                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(PillShape),
                        color = Primary,
                        trackColor = Color.White.copy(0.2f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Spa, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("4-7-8 Breathing Cadence", style = MaterialTheme.typography.labelSmall, color = TextMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = TextMedium, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Auto-lock in 01:15", style = MaterialTheme.typography.labelSmall, color = TextMedium)
                        }
                    }
                }

                // Post-video handoff bottom sheet
                PostVideoHandoff(
                    nextAppName = state.nextAppName,
                    onExtend = {},
                    onLaunch = {
                        viewModel.completeSession()
                        onVideoComplete()
                    }
                )
            }
        }
    }
}

@Composable
private fun PostVideoHandoff(nextAppName: String, onExtend: () -> Unit, onLaunch: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Handle
            Box(
                Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(Color(0xFFE5E7EB), PillShape)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryContainer, androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, null, tint = Primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("RECHARGE STAGE COMPLETE", style = MaterialTheme.typography.labelSmall, color = Primary)
                    Text("State of Calm Reached", style = MaterialTheme.typography.headlineMedium, color = TextHighEmphasis)
                }
                Surface(shape = PillShape, color = SurfaceVariant) {
                    Text("90m\nCooldown", style = MaterialTheme.typography.labelSmall, color = TextMedium,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(10.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FBF9)),
                shape = CardShape,
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Heart Rate Variability", style = MaterialTheme.typography.bodyMedium, color = TextHighEmphasis)
                        Text("Optimal (+14%)", style = MaterialTheme.typography.labelLarge, color = Primary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Dopamine baselines normalized. Your nervous system is primed for high-cognition creative flow.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SCHEDULED APP HANDOFF", style = MaterialTheme.typography.labelSmall, color = TextMedium)
                Text("Priority Route", style = MaterialTheme.typography.labelSmall, color = Primary)
            }

            Spacer(Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                shape = CardShape
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF1A1A2E), androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("N", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(nextAppName, style = MaterialTheme.typography.titleMedium, color = TextHighEmphasis)
                            Spacer(Modifier.width(8.dp))
                            Surface(shape = PillShape, color = PrimaryContainer) {
                                Text("Workspaces", style = MaterialTheme.typography.labelSmall, color = PrimaryDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                        Text("Q3 Architecture Roadmaps • Sprint 14", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                    }
                    Icon(Icons.Default.ArrowForward, null, tint = Primary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onExtend,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = PillShape
                ) {
                    Text("Extend 2 Min", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onLaunch,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Launch $nextAppName", style = MaterialTheme.typography.labelLarge, color = OnPrimary)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.OpenInNew, null, tint = OnPrimary, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Digital wellbeing guardian locked • Distraction feeds blocked for 90m",
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UrlEntryPrompt(
    url: String,
    onUrlChange: (String) -> Unit,
    onSave: () -> Unit,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.VideoLibrary, null, tint = Primary, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(24.dp))
        Text("Add a YouTube Video", style = MaterialTheme.typography.headlineLarge, color = Color.White, textAlign = TextAlign.Center)
        Text("Paste a YouTube URL for your yoga-nidra or restoration video", style = MaterialTheme.typography.bodyMedium, color = TextMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("YouTube URL", color = TextMedium) },
            shape = CardShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.White.copy(0.3f),
                cursorColor = Primary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = Color.Red) } }
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(52.dp), shape = PillShape,
            colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Text("Save Video", style = MaterialTheme.typography.labelLarge, color = OnPrimary)
        }
    }
}

private fun extractYouTubeId(url: String): String? {
    val regex = Regex("(?:youtu\\.be/|youtube\\.com/(?:watch\\?v=|embed/|v/))([a-zA-Z0-9_-]{11})")
    return regex.find(url)?.groupValues?.get(1)
}

private fun buildYouTubeHtml(videoId: String) = """
    <!DOCTYPE html>
    <html>
    <head>
        <style>
            * { margin: 0; padding: 0; background: #000; }
            #player { width: 100vw; height: 100vh; }
        </style>
    </head>
    <body>
        <div id="player"></div>
        <script src="https://www.youtube.com/iframe_api"></script>
        <script>
            var player;
            function onYouTubeIframeAPIReady() {
                player = new YT.Player('player', {
                    videoId: '$videoId',
                    playerVars: {
                        'playsinline': 1,
                        'controls': 0,
                        'rel': 0,
                        'modestbranding': 1,
                        'autoplay': 1
                    },
                    events: { 'onReady': function(e) { e.target.playVideo(); } }
                });
            }
        </script>
    </body>
    </html>
""".trimIndent()
