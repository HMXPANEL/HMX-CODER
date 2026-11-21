package com.hmx.harness.ui.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
): Modifier = composed {
    val depth = depthOf(level)
    val tint = accent.takeOrElse { LiquidGlassPalette.GlassWhite }
    val lift = elevation ?: depth.elevation.dp
    val quality = LocalLiquidGlassQuality.current
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
