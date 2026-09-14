package com.example.recharge.video

import android.annotation.SuppressLint
import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoScreen(
    onVideoComplete: () -> Unit,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showUrlPrompt by remember { mutableStateOf(false) }
    var redirecting by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }

    // When redirecting starts, animate progress bar then complete
    LaunchedEffect(redirecting) {
        if (redirecting) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
            )
            viewModel.completeSession()
            onVideoComplete()
        }
    }

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
            // Fullscreen clean video player
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            val webView = this
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.BLACK)

                            // Enable cookies including 3rd party cookies required by YouTube embed
                            android.webkit.CookieManager.getInstance().apply {
                                setAcceptCookie(true)
                                setAcceptThirdPartyCookies(webView, true)
                            }

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                allowContentAccess = true
                                allowFileAccess = true
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                            }
                            
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    return false
                                }
                            }
                            webChromeClient = android.webkit.WebChromeClient()
                            
                            // YouTube blocks embeds when loaded as top-level window.
                            // Wrapping it in an HTML document with an <iframe> and loading with base URL resolves the configuration error.
                            val html = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                    <style>
                                        * { margin: 0; padding: 0; box-sizing: border-box; }
                                        html, body {
                                            width: 100%;
                                            height: 100%;
                                            background-color: #000000;
                                            overflow: hidden;
                                        }
                                        .container {
                                            position: absolute;
                                            top: 0;
                                            left: 0;
                                            width: 100%;
                                            height: 100%;
                                        }
                                        iframe {
                                            width: 100%;
                                            height: 100%;
                                            border: none;
                                        }
                                    </style>
                                </head>
                                <body>
                                    <div class="container">
                                        <iframe
                                            id="ytplayer"
                                            src="https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&playsinline=1&rel=0&modestbranding=1&controls=1&fs=0&iv_load_policy=3&disablekb=0&enablejsapi=1&origin=https://www.youtube-nocookie.com"
                                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                                            allowfullscreen>
                                        </iframe>
                                    </div>
                                </body>
                                </html>
                            """.trimIndent()

                            loadDataWithBaseURL("https://www.youtube-nocookie.com", html, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Invisible tap zone at top of screen to trigger redirect
                // (bottom area left free for YouTube scrub bar)
                if (!redirecting) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.75f)
                            .align(Alignment.TopCenter)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                redirecting = true
                            }
                    )
                }

                // Progress bar at bottom when redirecting
                if (redirecting) {
                    // Dim overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Redirecting…",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Thin white progress bar at the very bottom
                    LinearProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f)
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
