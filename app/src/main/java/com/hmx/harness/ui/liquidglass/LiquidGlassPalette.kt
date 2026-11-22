package com.hmx.harness.ui.liquidglass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * HMXHARNESS Liquid Glass palette.
 *
 * Presentation-only tokens. These colors are deliberately independent of the
 * Material color scheme so the glass system keeps a coherent identity whether
 * the user picked dynamic color, a custom palette, or light/dark mode. Text
 * colors always come from MaterialTheme so contrast and user theming survive.
 */
@Immutable
object LiquidGlassPalette {
    val Midnight = Color(0xFF090713)
    val DeepViolet = Color(0xFF151027)
    val NightBlue = Color(0xFF0C1024)

    val GlassWhite = Color(0xFFF4F0FF)
    val GlassLavender = Color(0xFFB9A7FF)
    val GlassBlueViolet = Color(0xFF8F9CFF)

    val ElectricViolet = Color(0xFF8B5CF6)
    val SoftPurple = Color(0xFFA78BFA)
    val IcyBlue = Color(0xFF7DD3FC)

    val TextPrimary = Color(0xFFF7F5FF)
    val TextSecondary = Color(0xFFC9C2DE)
    val TextDisabled = Color(0xFF8B859F)

    val Specular = Color(0xFFFFFFFF)
    val EdgeLight = Color(0xFFD8CBFF)
    val Shadow = Color(0xFF04020C)
}

/** Four material depths, mirroring the 4-level glass hierarchy. */
@Immutable
enum class LiquidGlassLevel(val id: Int) {
    LIGHT(1), STANDARD(2), DEEP(3), FOCUS(4)
}

/** Runtime quality switch. Low-end devices get the layered-gradient fallback only. */
@Immutable
enum class LiquidGlassQuality { FULL, LITE }

/** Depth presets: fill alpha, border alpha, highlight alpha, elevation. */
internal data class GlassDepth(
    val topTint: Color,
    val bottomTint: Color,
    val fillAlpha: Float,
    val borderAlpha: Float,
    val highlightAlpha: Float,
    val elevation: Float,
)

internal fun depthOf(level: LiquidGlassLevel, tokens: GlassTokens): GlassDepth = when (level) {
    LiquidGlassLevel.LIGHT -> GlassDepth(
        tokens.surfaceTint, tokens.surfaceTint,
        fillAlpha = tokens.fillAlpha * .55f, borderAlpha = tokens.edgeAlpha * .75f,
        highlightAlpha = tokens.specularAlpha * .55f, elevation = 2f)
    LiquidGlassLevel.STANDARD -> GlassDepth(
        tokens.surfaceTint, tokens.surfaceTint,
        fillAlpha = tokens.fillAlpha, borderAlpha = tokens.edgeAlpha,
        highlightAlpha = tokens.specularAlpha, elevation = 5f)
    LiquidGlassLevel.DEEP -> GlassDepth(
        tokens.surfaceStrong, tokens.surfaceTint,
        fillAlpha = tokens.strongFillAlpha * .82f, borderAlpha = tokens.edgeAlpha * 1.15f,
        highlightAlpha = tokens.specularAlpha * .9f, elevation = 12f)
    LiquidGlassLevel.FOCUS -> GlassDepth(
        tokens.surfaceTint, tokens.surfaceTint,
        fillAlpha = tokens.fillAlpha * 1.08f, borderAlpha = tokens.edgeAlpha * 1.45f,
        highlightAlpha = tokens.specularAlpha * 1.15f, elevation = 16f)
}
