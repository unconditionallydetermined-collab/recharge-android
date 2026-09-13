package com.example.recharge.home

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "recharge",
                    color = Color.Black.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp
                )
                IconButton(onClick = { onNavigate(AppRoute.Settings) }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Center decoration (Tablet with stylus)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TabletDecoration()
            }

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

                // The user requested to set the current state to recharge if possible.
                // We'll bypass the Standby check and always allow RECHARGE if the queue isn't empty.
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

                Surface(
                    onClick = buttonAction,
                    enabled = buttonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .border(
                            2.dp,
                            if (buttonEnabled) Color.Black else Color.LightGray,
                            RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    contentColor = if (buttonEnabled) Color.Black else Color.LightGray
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
                            color = if (buttonEnabled) Color.Black else Color.LightGray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = buttonSubtext,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (buttonEnabled) Color.Black.copy(alpha = 0.6f) else Color.LightGray
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
        val black = Color.Black
        
        // Tablet body
        val tabletWidth = size.width * 0.6f
        val tabletHeight = size.height * 0.8f
        val topLeftX = (size.width - tabletWidth) / 2
        val topLeftY = (size.height - tabletHeight) / 2
        
        drawRoundRect(
            color = black,
            topLeft = Offset(topLeftX, topLeftY),
            size = Size(tabletWidth, tabletHeight),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        
        // Screen inner rectangle
        drawRoundRect(
            color = black,
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
            color = black,
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
        drawPath(path, color = black)
        
        // Writing lines
        val lineStartX = topLeftX + 24.dp.toPx()
        val line1Y = topLeftY + 48.dp.toPx()
        drawLine(black, Offset(lineStartX, line1Y), Offset(lineStartX + 48.dp.toPx(), line1Y), strokeWidth = thinStroke, cap = StrokeCap.Round)
        
        val line2Y = line1Y + 24.dp.toPx()
        drawLine(black, Offset(lineStartX, line2Y), Offset(lineStartX + 70.dp.toPx(), line2Y), strokeWidth = thinStroke, cap = StrokeCap.Round)
        
        val line3Y = line2Y + 24.dp.toPx()
        drawLine(black, Offset(lineStartX, line3Y), Offset(lineStartX + 36.dp.toPx(), line3Y), strokeWidth = thinStroke, cap = StrokeCap.Round)
    }
}
