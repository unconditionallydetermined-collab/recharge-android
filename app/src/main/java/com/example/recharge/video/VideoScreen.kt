package com.example.recharge.video

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

    var showUrlPrompt by remember { mutableStateOf(false) }

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
        
        val videoId = extractYouTubeId(state.youtubeUrl)
        if (videoId == null) {
            if (showUrlPrompt) {
                UrlEntryPrompt(
                    url = state.urlInput,
                    onUrlChange = viewModel::onUrlInput,
                    onSave = { 
                        viewModel.saveUrl()
                        showUrlPrompt = false
                    },
                    error = state.urlError
                )
            } else {
                // Empty black screen with plus sign
                Box(modifier = Modifier.fillMaxSize()) {
                    IconButton(
                        onClick = { showUrlPrompt = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Video", tint = Color.White)
                    }
                }
            }
        } else {
            // Video player (YouTube IFrame in WebView)
            var videoEnded by remember { mutableStateOf(false) }
            
            if (videoEnded) {
                // Simple black end screen
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            viewModel.completeSession()
                            onVideoComplete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = PillShape,
                        modifier = Modifier.height(56.dp).padding(horizontal = 32.dp)
                    ) {
                        Text("Launch ${state.nextAppName}", color = OnPrimary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            } else {
                // Fullscreen Video Player
                Box(modifier = Modifier.fillMaxSize()) {
                    var webViewRef by remember { mutableStateOf<WebView?>(null) }
                    
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(android.graphics.Color.BLACK)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                webViewClient = WebViewClient()
                                webChromeClient = android.webkit.WebChromeClient()
                                addJavascriptInterface(object {
                                    @android.webkit.JavascriptInterface
                                    fun onVideoEnded() {
                                        post { videoEnded = true }
                                    }
                                }, "AndroidVideoInterface")
                                
                                loadDataWithBaseURL(
                                    "https://localhost",
                                    buildYouTubeHtml(videoId),
                                    "text/html",
                                    "utf-8",
                                    null
                                )
                                webViewRef = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Invisible clickable overlay to capture taps for play/pause
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                if (state.isPlaying) {
                                    viewModel.togglePlayPause()
                                    webViewRef?.evaluateJavascript("player.pauseVideo();", null)
                                } else {
                                    viewModel.togglePlayPause()
                                    webViewRef?.evaluateJavascript("player.playVideo();", null)
                                }
                            }
                    )

                    // Close button at top right
                    IconButton(
                        onClick = { videoEnded = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }
            }
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
                        'autoplay': 1,
                        'origin': 'https://localhost'
                    },
                    events: { 
                        'onReady': function(e) { e.target.playVideo(); },
                        'onStateChange': function(e) {
                            if (e.data === YT.PlayerState.ENDED && window.AndroidVideoInterface) {
                                window.AndroidVideoInterface.onVideoEnded();
                            }
                        }
                    }
                });
            }
        </script>
    </body>
    </html>
""".trimIndent()
