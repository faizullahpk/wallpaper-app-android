package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Sophisticated Dark Color Scheme values
private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),          // Lavender/purple accent (e.g. WallSpace title, badges)
    onPrimary = Color(0xFF381E72),        // Deep dark indigo text on primary
    primaryContainer = Color(0xFF4A4458), // Medium dark-slate highlight (active nav, chips)
    onPrimaryContainer = Color(0xFFE8DEF8), // Lavender white
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    background = Color(0xFF0A0A0A),       // Deep black background
    onBackground = Color(0xFFE6E1E5),     // Mauve/off-white text
    surface = Color(0xFF121212),          // Zinc-900 background for containers
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF1C1C1C),   // Bottom menu and darker container surfaces
    onSurfaceVariant = Color(0xFFCAC4D0), // Grayish text for captions
    outline = Color(0xFF27272A),          // Elegant Zinc-800 border
    outlineVariant = Color(0xFF3F3F46)    // Zinc-700 separator
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    amoledMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SophisticatedDarkColorScheme
        else -> LightColorScheme
    }

    // Apply premium active AMOLED solid black surfaces when requested
    if (darkTheme && amoledMode) {
        colorScheme = colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF0D0D0D),
            onBackground = Color.White,
            onSurface = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}