package com.agrofront.agrohelper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AgroColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFFF9A825),
    tertiary = Color(0xFF6D8F3D),
    background = Color(0xFFF7F8F2),
    surface = Color.White
)

@Composable
fun AgroHelperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AgroColors,
        content = content
    )
}
