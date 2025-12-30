package com.hotmail.arehmananis.sketchapp.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light theme color scheme - Vibrant and modern
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors - Vibrant purple
    primary = VibrantPurple,
    onPrimary = PureWhite,
    primaryContainer = VibrantIndigo.copy(alpha = 0.15f),
    onPrimaryContainer = DeepPurple,

    // Secondary colors - Coral orange accent
    secondary = CoralOrange,
    onSecondary = PureWhite,
    secondaryContainer = CoralOrange.copy(alpha = 0.15f),
    onSecondaryContainer = CoralOrange,

    // Tertiary colors - Bright pink
    tertiary = BrightPink,
    onTertiary = PureWhite,
    tertiaryContainer = BrightPink.copy(alpha = 0.15f),
    onTertiaryContainer = BrightPink,

    // Background and surfaces
    background = LightCanvas,
    onBackground = DarkText,
    surface = PureWhite,
    onSurface = DarkText,
    surfaceVariant = LightGray,
    onSurfaceVariant = MediumGray,
    surfaceTint = VibrantPurple,

    // Error states
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorRed.copy(alpha = 0.15f),
    onErrorContainer = ErrorRed,

    // Outline and dividers
    outline = SoftGray,
    outlineVariant = LightGray,
    scrim = DarkText.copy(alpha = 0.4f),

    // Inverse colors (for snackbars, etc.)
    inverseSurface = DarkCanvas,
    inverseOnSurface = LightText,
    inversePrimary = NeonPurple
)

/**
 * Dark theme color scheme - Neon accents on dark background
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors - Neon purple
    primary = NeonPurple,
    onPrimary = DarkCanvas,
    primaryContainer = NeonPurple.copy(alpha = 0.2f),
    onPrimaryContainer = NeonPurple,

    // Secondary colors - Neon orange accent
    secondary = NeonOrange,
    onSecondary = DarkCanvas,
    secondaryContainer = NeonOrange.copy(alpha = 0.2f),
    onSecondaryContainer = NeonOrange,

    // Tertiary colors - Neon pink
    tertiary = NeonPink,
    onTertiary = DarkCanvas,
    tertiaryContainer = NeonPink.copy(alpha = 0.2f),
    onTertiaryContainer = NeonPink,

    // Background and surfaces
    background = DarkCanvas,
    onBackground = LightText,
    surface = DarkSurface,
    onSurface = LightText,
    surfaceVariant = MediumDark,
    onSurfaceVariant = MediumLightGray,
    surfaceTint = NeonPurple,

    // Error states
    error = ErrorRedBright,
    onError = DarkCanvas,
    errorContainer = ErrorRedBright.copy(alpha = 0.2f),
    onErrorContainer = ErrorRedBright,

    // Outline and dividers
    outline = BorderDark,
    outlineVariant = MediumDark,
    scrim = PureWhite.copy(alpha = 0.1f),

    // Inverse colors (for snackbars, etc.)
    inverseSurface = LightCanvas,
    inverseOnSurface = DarkText,
    inversePrimary = VibrantPurple
)

/**
 * SketchApp modern theme with vibrant gradients and custom typography
 *
 * @param darkTheme Whether to use dark theme (defaults to system preference)
 * @param content The composable content to theme
 */
@Composable
fun SketchAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar icon color based on theme for edge-to-edge mode
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
