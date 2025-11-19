package com.carlos.makeupsales.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    secondary = PurpleAccent,
    onSecondary = Color.White,
    background = SoftBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary
)

private val DarkColors = darkColorScheme(
    primary = PinkPrimary,
    secondary = PurpleAccent
)

@Composable
fun MakeupSalesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
