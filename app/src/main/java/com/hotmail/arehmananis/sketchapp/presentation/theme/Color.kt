package com.hotmail.arehmananis.sketchapp.presentation.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// LIGHT THEME COLORS
// ============================================================================

// Primary gradient spectrum - Blue to Purple (creativity, imagination)
val VibrantBlue = Color(0xFF3B82F6)
val VibrantIndigo = Color(0xFF6366F1)
val VibrantPurple = Color(0xFF8B5CF6)
val DeepPurple = Color(0xFF7C3AED)

// Accent colors - Warm energy
val CoralOrange = Color(0xFFF97316)
val BrightPink = Color(0xFFEC4899)
val SunsetYellow = Color(0xFFFBBF24)

// Background and surfaces - Clean neutrals
val LightCanvas = Color(0xFFFAFAFA)
val PureWhite = Color(0xFFFFFFFF)
val LightGray = Color(0xFFF5F5F5)
val SoftGray = Color(0xFFE5E7EB)

// Text colors
val DarkText = Color(0xFF1F2937)
val MediumGray = Color(0xFF6B7280)
val LightTextGray = Color(0xFF9CA3AF)

// Semantic colors
val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)
val InfoCyan = Color(0xFF06B6D4)

// ============================================================================
// DARK THEME COLORS
// ============================================================================

// Primary gradient spectrum - Brighter, more saturated for dark mode
val NeonBlue = Color(0xFF60A5FA)
val NeonIndigo = Color(0xFF818CF8)
val NeonPurple = Color(0xFFA78BFA)
val BrightPurple = Color(0xFF9333EA)

// Accent colors - Neon pops
val NeonOrange = Color(0xFFFB923C)
val NeonPink = Color(0xFFF472B6)
val NeonYellow = Color(0xFFFBBF24)

// Background and surfaces - Rich darks
val DarkCanvas = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val MediumDark = Color(0xFF334155)
val BorderDark = Color(0xFF475569)

// Text colors
val LightText = Color(0xFFF1F5F9)
val MediumLightGray = Color(0xFFCBD5E1)
val DarkTextGray = Color(0xFF94A3B8)

// Semantic colors - Brighter for visibility on dark
val SuccessGreenBright = Color(0xFF34D399)
val WarningAmberBright = Color(0xFFFBBF24)
val ErrorRedBright = Color(0xFFF87171)
val InfoCyanBright = Color(0xFF22D3EE)

// ============================================================================
// EXTENDED DRAWING COLOR PALETTE
// ============================================================================

object DrawingColors {
    // Pure colors
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)

    // Vibrant spectrum
    val CrimsonRed = Color(0xFFDC2626)
    val FireOrange = Color(0xFFF97316)
    val SunYellow = Color(0xFFFBBF24)
    val LimeGreen = Color(0xFF84CC16)
    val EmeraldGreen = Color(0xFF10B981)
    val TealCyan = Color(0xFF14B8A6)
    val SkyBlue = Color(0xFF3B82F6)
    val IndigoBlue = Color(0xFF6366F1)
    val VioletPurple = Color(0xFF8B5CF6)
    val FuchsiaPink = Color(0xFFD946EF)
    val RosePink = Color(0xFFF43F5E)

    // Neutral tones
    val SlateGray = Color(0xFF64748B)
    val WarmGray = Color(0xFF78716C)
    val CoolGray = Color(0xFF6B7280)

    // Pastel variants
    val PastelPink = Color(0xFFFDA4AF)
    val PastelOrange = Color(0xFFFDBA74)
    val PastelYellow = Color(0xFFFDE68A)
    val PastelGreen = Color(0xFFA7F3D0)
    val PastelBlue = Color(0xFFA5B4FC)
    val PastelPurple = Color(0xFFDDD6FE)

    // All colors for palette display (organized by category)
    val vibrantColors = listOf(
        CrimsonRed, FireOrange, SunYellow, LimeGreen,
        EmeraldGreen, TealCyan, SkyBlue, IndigoBlue,
        VioletPurple, FuchsiaPink, RosePink
    )

    val pastelColors = listOf(
        PastelPink, PastelOrange, PastelYellow,
        PastelGreen, PastelBlue, PastelPurple
    )

    val neutralColors = listOf(
        Black, SlateGray, WarmGray, CoolGray, White
    )

    // All colors combined for comprehensive palette
    val allColors = vibrantColors + pastelColors + neutralColors
}
