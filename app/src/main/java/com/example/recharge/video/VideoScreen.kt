package com.example.recharge.video

import android.annotation.SuppressLint
import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            // Video player
            var videoEnded by remember { mutableStateOf(false) }
            
            if (videoEnded) {
                // Black screen with white bold text — tap anywhere to redirect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.completeSession()
                            onVideoComplete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Launch ${state.nextAppName}",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Fullscreen Video Player with spoofed user agent
                Box(modifier = Modifier.fillMaxSize()) {
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
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                // Spoof Chrome Mobile user agent so YouTube doesn't block playback
                                settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        // Keep all navigation inside the WebView
                                        return false
                                    }
                                }
                                webChromeClient = android.webkit.WebChromeClient()
                                
                                // Load the full YouTube mobile page — most reliable approach
                                loadUrl("https://m.youtube.com/watch?v=$videoId")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Small "Done" text at top right — tapping ends session
                    Text(
                        text = "Done",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(20.dp)
                            .clickable { videoEnded = true }
                    )
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
