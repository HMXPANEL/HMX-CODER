package com.hmx.harness.ui.liquidglass

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware material tokens for the Liquid Glass system.
 *
 * The glass is neutral and background-aware: dark mode uses charcoal/slate
 * tint rather than white, light mode uses a soft neutral tint rather than a
 * bright white overlay. Edge lighting and specular sweeps are deliberately
 * low intensity so the material reads as transparent glass instead of a
 * milky card with a glowing outline.
 */
@Immutable
data class GlassTokens(
    val dark: Boolean,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val surfaceTint: Color,
    val surfaceStrong: Color,
    val edge: Color,
    val specular: Color,
    val shadow: Color,
    /** Body tint alpha for neutral panels. */
    val fillAlpha: Float,
    /** Higher tint alpha used for floating chrome such as menus and dialogs. */
    val strongFillAlpha: Float,
    val edgeAlpha: Float,
    val specularAlpha: Float,
    val shadowAlpha: Float,
)

internal fun glassTokensFor(dark: Boolean): GlassTokens = if (dark) {
    GlassTokens(
        dark = true,
        backgroundTop = Color(0xFF101216),
        backgroundBottom = Color(0xFF08090C),
        surfaceTint = Color(0xFF1B1E25),
        surfaceStrong = Color(0xFF15181E),
        edge = Color(0xFFB9C2D0),
        specular = Color(0xFFE9EDF4),
        shadow = Color(0xFF000000),
        fillAlpha = .34f,
        strongFillAlpha = .64f,
        edgeAlpha = .13f,
        specularAlpha = .09f,
        shadowAlpha = .55f,
    )
} else {
    GlassTokens(
        dark = false,
        backgroundTop = Color(0xFFF7F8FA),
        backgroundBottom = Color(0xFFECEEF2),
        surfaceTint = Color(0xFFFFFFFF),
        surfaceStrong = Color(0xFFF6F7F9),
        edge = Color(0xFF66707F),
        specular = Color(0xFFFFFFFF),
        shadow = Color(0xFF1B1F27),
        fillAlpha = .36f,
        strongFillAlpha = .74f,
        edgeAlpha = .16f,
        specularAlpha = .26f,
        shadowAlpha = .14f,
    )
}

val LocalGlassTokens = staticCompositionLocalOf { glassTokensFor(dark = true) }
