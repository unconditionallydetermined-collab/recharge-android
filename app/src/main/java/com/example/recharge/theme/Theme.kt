package com.example.recharge.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Primary,
    onSecondary = OnPrimary,
    secondaryContainer = PrimaryContainer,
    onSecondaryContainer = OnPrimaryContainer,
    background = Background,
    onBackground = TextHighEmphasis,
    surface = Surface,
    onSurface = TextHighEmphasis,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextMedium,
    outline = Border,
    outlineVariant = Border,
    scrim = Scrim,
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Color(0xFF1F3832),
    onPrimaryContainer = PrimaryContainer,
    secondary = Primary,
    onSecondary = OnPrimary,
    background = BackgroundDark,
    onBackground = TextHighDark,
    surface = SurfaceDark,
    onSurface = TextHighDark,
    surfaceVariant = Color(0xFF2D3748),
    onSurfaceVariant = TextMedDark,
    outline = BorderDark,
    outlineVariant = BorderDark,
)

@Composable
fun RechargeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RechargeTypography,
        shapes = RechargeShapes,
        content = content
    )
}
