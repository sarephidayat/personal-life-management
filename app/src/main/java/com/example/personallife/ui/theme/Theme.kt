package com.example.personallife.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeoYellow,
    onPrimary = LightText,
    secondary = NeoGreen,
    onSecondary = LightText,
    tertiary = NeoBlue,
    onTertiary = LightText,
    error = NeoRed,
    onError = LightText,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = DarkMutedText,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = NeoYellow,
    onPrimary = LightText,
    secondary = NeoGreen,
    onSecondary = LightText,
    tertiary = NeoBlue,
    onTertiary = LightText,
    error = NeoRed,
    onError = LightText,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightCardBg,
    onSurfaceVariant = LightMutedText,
    outline = LightBorder
)

@Composable
fun PersonalLifeTheme(
    themeMode: String = "system", // "system", "light", "dark"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
