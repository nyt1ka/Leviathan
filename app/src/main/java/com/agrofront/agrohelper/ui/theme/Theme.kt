package com.agrofront.agrohelper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Forest = Color(0xFF1F5B3A)
private val ForestDark = Color(0xFF123C27)
private val FieldGold = Color(0xFFC79224)
private val Cream = Color(0xFFF7F4EA)
private val WarmSurface = Color(0xFFFFFDF7)
private val Ink = Color(0xFF1C241F)
private val Muted = Color(0xFF68736B)
private val Night = Color(0xFF101713)
private val NightSurface = Color(0xFF18211B)

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8EBDD),
    onPrimaryContainer = ForestDark,
    secondary = FieldGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4E2B5),
    onSecondaryContainer = Color(0xFF4A3500),
    background = Cream,
    onBackground = Ink,
    surface = WarmSurface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE9EDE7),
    onSurfaceVariant = Muted,
    outline = Color(0xFFA8B2AA)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD0A6),
    onPrimary = Color(0xFF0C3620),
    primaryContainer = Color(0xFF245B3D),
    onPrimaryContainer = Color(0xFFD9F2E1),
    secondary = Color(0xFFE7C46E),
    onSecondary = Color(0xFF3E2E00),
    background = Night,
    onBackground = Color(0xFFE7EEE9),
    surface = NightSurface,
    onSurface = Color(0xFFE7EEE9),
    surfaceVariant = Color(0xFF29352D),
    onSurfaceVariant = Color(0xFFBBC6BE),
    outline = Color(0xFF7B887F)
)

@Composable
fun AgroHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
