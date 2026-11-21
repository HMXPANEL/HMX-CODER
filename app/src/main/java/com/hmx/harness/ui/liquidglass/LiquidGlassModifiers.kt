package com.hmx.harness.ui.liquidglass

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import com.styropyr0.prismal.PrismalGlass
import com.styropyr0.prismal.depth.PrismalDepthShadow
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.styropyr0.prismal.sources.PrismalGlassLayer
import com.styropyr0.prismal.specular.PrismalSpecular
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Injected by the app root; defaults to FULL. Set LITE for low-RAM devices. */
val LocalLiquidGlassQuality = staticCompositionLocalOf { LiquidGlassQuality.FULL }

/** The app's sampled backdrop, provided once at the theme root (PrismalAGSL). */
val LocalHmxGlassBackdrop = staticCompositionLocalOf<PrismalGlassLayer?> { null }

/**
 * Lightweight, always-on glass material.
 *
 * A translucent solid color is not glass: this draws a layered vertical tint
 * gradient, a top specular sweep, a refracted light pool, a luminous gradient
 * edge and a soft shadow. It never samples or blurs the backdrop, so it costs
 * no offscreen texture and is safe on mid-range devices. When the existing
 * backdrop engine is enabled it stacks on top of the real blur/lens material.
 */
@Composable
fun Modifier.liquidGlass(
    level: LiquidGlassLevel = LiquidGlassLevel.STANDARD,
    shape: Shape,
    accent: Color = Color.Unspecified,
    elevation: Dp? = null,
    cornerRadius: Dp = 20.dp,
): Modifier = composed {
    val backdrop = LocalHmxGlassBackdrop.current
    val density = LocalDensity.current
    val depth = depthOf(level)
    val tint = accent.takeOrElse { LiquidGlassPalette.GlassWhite }
    val lift = elevation ?: depth.elevation.dp
    val quality = LocalLiquidGlassQuality.current

    // REAL LIQUID GLASS — PrismalAGSL samples the recorded backdrop, applies a
    // backdrop blur, an edge lens (optical refraction + chromatic dispersion)
    // and specular/depth layers. It degrades automatically on older APIs.
    if (backdrop != null && PrismalGlass.isSupported()) {
        val glassShape: () -> androidx.compose.ui.graphics.Shape =
            if (shape == CircleShape) ({ PrismalCapsule() })
            else ({ PrismalRoundedRectangle(cornerRadius) })
        val blurPx = when (level) {
            LiquidGlassLevel.LIGHT -> 6.dp
            LiquidGlassLevel.STANDARD -> 12.dp
            LiquidGlassLevel.DEEP -> 16.dp
            LiquidGlassLevel.FOCUS -> 10.dp
        }
        val lensHeight = when (level) {
            LiquidGlassLevel.LIGHT -> 8.dp
            LiquidGlassLevel.STANDARD -> 14.dp
            LiquidGlassLevel.DEEP -> 18.dp
            LiquidGlassLevel.FOCUS -> 20.dp
        }
        val lensAmount = when (level) {
            LiquidGlassLevel.LIGHT -> 14.dp
            LiquidGlassLevel.STANDARD -> 26.dp
            LiquidGlassLevel.DEEP -> 32.dp
            LiquidGlassLevel.FOCUS -> 38.dp
        }
        val dispersion = if (level == LiquidGlassLevel.FOCUS) 0.32f else 0.18f
        val surfaceAlpha = when (level) {
            LiquidGlassLevel.LIGHT -> .05f
            LiquidGlassLevel.STANDARD -> .08f
            LiquidGlassLevel.DEEP -> .14f
            LiquidGlassLevel.FOCUS -> .18f
        }
        return@composed this.drawPrismalGlass(
            backdrop = backdrop,
            shape = glassShape,
            effects = {
                applyPrismalGlassEffects(
                    density = density,
                    adaptiveLuminance = false,
                    luminance = .5f,
                    blurRadiusPx = with(density) { blurPx.toPx() },
                    refractionHeightPx = with(density) { lensHeight.toPx() },
                    refractionAmountPx = with(density) { lensAmount.toPx() },
                    chromaticAberration = dispersion,
                    brightness = .06f,
                    saturation = 1.35f,
                )
            },
            specular = { PrismalSpecular.Default },
            depthShadow = { PrismalDepthShadow.Default },
            onDrawSurface = {
                val overlay = if (accent == Color.Unspecified) tint else accent
                drawRect(overlay.copy(alpha = surfaceAlpha))
            },
        )
    }

    // FALLBACK — lightweight layered gradient material for devices or contexts
    // without an available backdrop (no offscreen texture, no shader).
    this
        .shadow(lift, shape, clip = false,
            ambientColor = LiquidGlassPalette.Shadow.copy(alpha = .45f),
            spotColor = LiquidGlassPalette.Shadow.copy(alpha = .30f))
        .drawBehind {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@drawBehind

            // 1. Body: translucent tint shifting from cool white to lavender.
            drawRoundShape(shape, Brush.verticalGradient(
                0f to tint.copy(alpha = depth.fillAlpha * 1.25f),
                .55f to depth.bottomTint.copy(alpha = depth.fillAlpha),
                1f to depth.bottomTint.copy(alpha = depth.fillAlpha * .55f)))

            // 2. Specular light sweeping the top edge — the glass "shine".
            drawRoundShape(shape, Brush.verticalGradient(
                0f to LiquidGlassPalette.Specular.copy(alpha = depth.highlightAlpha),
                .16f to Color.Transparent))

            // 3. Refracted light pool offset toward a corner.
            drawRoundShape(shape, Brush.radialGradient(
                listOf(LiquidGlassPalette.IcyBlue.copy(alpha = .10f), Color.Transparent),
                center = Offset(w * .82f, h * .12f), radius = maxOf(w, h) * .9f))

            // 4. Luminous border; FOCUS level adopts the brighter edge light.
            val edge = if (level == LiquidGlassLevel.FOCUS) LiquidGlassPalette.EdgeLight else tint
            drawRoundShape(shape, Brush.linearGradient(
                0f to edge.copy(alpha = depth.borderAlpha),
                .5f to LiquidGlassPalette.Specular.copy(alpha = depth.borderAlpha * .6f),
                1f to LiquidGlassPalette.GlassLavender.copy(alpha = depth.borderAlpha)),
                stroke = Stroke(width = if (quality == LiquidGlassQuality.FULL) 1.1f else .8f))

            // 5. Inner hairline: a second, tighter reflection.
            if (quality == LiquidGlassQuality.FULL) {
                drawRoundShape(shape, SolidColor(LiquidGlassPalette.Specular.copy(alpha = depth.highlightAlpha * .35f)),
                    stroke = Stroke(width = .6f), inset = 1.5f)
            }
        }
}

private fun DrawScope.drawRoundShape(
    shape: Shape,
    brush: Brush,
    stroke: Stroke? = null,
    inset: Float = 0f,
) {
    val rect = Rect(inset, inset, size.width - inset, size.height - inset)
    if (rect.width <= 0f || rect.height <= 0f) return
    val outline = shape.createOutline(rect.size, layoutDirection, this)
    drawOutline(outline, brush, 1f, style = stroke ?: Fill)
}
