package com.imut.autoclicker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6C63FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A42D4),
    secondary = Color(0xFF3FC1C9),
    onSecondary = Color.Black,
    background = Color(0xFF0F1117),
    surface = Color(0xFF1C1F26),
    surfaceVariant = Color(0xFF21262D),
    onBackground = Color(0xFFE1E4E8),
    onSurface = Color(0xFFE1E4E8),
    outline = Color(0xFF30363D),
    error = Color(0xFFF85149)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6C63FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E6FF),
    secondary = Color(0xFF3FC1C9),
    onSecondary = Color.Black,
    background = Color(0xFFFAFBFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF6F8FA),
    onBackground = Color(0xFF24292F),
    onSurface = Color(0xFF24292F),
    outline = Color(0xFFD0D7DE),
    error = Color(0xFFCF222E)
)

@Composable
fun AutoClickerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
