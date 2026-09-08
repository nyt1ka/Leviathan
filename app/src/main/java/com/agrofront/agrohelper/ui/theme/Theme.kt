package com.agrofront.agrohelper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DeepBlue = Color(0xFF0B4F8A)
private val NightBlue = Color(0xFF07345D)
private val AgroGreen = Color(0xFF78B82A)
private val Lime = Color(0xFFA7DC31)
private val SunsetGold = Color(0xFFFFB52E)
private val WarmOrange = Color(0xFFF47A2A)
private val Cream = Color(0xFFFFFBF1)
private val SurfaceLight = Color(0xFFFFFFFF)
private val Ink = Color(0xFF17202A)
private val Muted = Color(0xFF64717E)
private val DarkBackground = Color(0xFF071522)
private val DarkSurface = Color(0xFF0D2234)

private val LightColors = lightColorScheme(
    primary = DeepBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7ECFF),
    onPrimaryContainer = NightBlue,
    secondary = AgroGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F6C9),
    onSecondaryContainer = Color(0xFF294B0B),
    tertiary = SunsetGold,
    onTertiary = Color(0xFF4B2B00),
    tertiaryContainer = Color(0xFFFFE8BE),
    onTertiaryContainer = Color(0xFF5B3500),
    background = Cream,
    onBackground = Ink,
    surface = SurfaceLight,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF0F5F8),
    onSurfaceVariant = Muted,
    outline = Color(0xFFA6B5C0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7BC3FF),
    onPrimary = Color(0xFF003355),
    primaryContainer = Color(0xFF124C75),
    onPrimaryContainer = Color(0xFFD7ECFF),
    secondary = Lime,
    onSecondary = Color(0xFF213600),
    secondaryContainer = Color(0xFF36550E),
    onSecondaryContainer = Color(0xFFE6F6C9),
    tertiary = Color(0xFFFFC767),
    onTertiary = Color(0xFF432B00),
    background = DarkBackground,
    onBackground = Color(0xFFE7F0F6),
    surface = DarkSurface,
    onSurface = Color(0xFFE7F0F6),
    surfaceVariant = Color(0xFF173248),
    onSurfaceVariant = Color(0xFFB9C8D3),
    outline = Color(0xFF73899A)
)

private val AgroShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun AgroHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        shapes = AgroShapes,
        content = content
    )
}
