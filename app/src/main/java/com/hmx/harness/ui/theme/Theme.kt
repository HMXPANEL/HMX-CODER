package com.hmx.harness.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import com.hmx.harness.data.ThemeMode
import com.hmx.harness.data.ProgressBarStyle
import com.hmx.harness.data.ThemePalette
import com.hmx.harness.ui.liquidglass.LiquidGlassAmbientBackground
import com.hmx.harness.ui.liquidglass.LocalLiquidGlassQuality
import com.hmx.harness.ui.liquidglass.LocalHmxGlassBackdrop
import com.hmx.harness.ui.liquidglass.LiquidGlassQuality
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
import com.styropyr0.prismal.sources.prismalGlassLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

val LocalProgressBarStyle = staticCompositionLocalOf { ProgressBarStyle.FLAT }

/**
 * App theme: Material You dynamic color on Android 12+, with a Google-blue–seeded
 * palette as the fallback, selectable tonal palettes and Google Sans Flex typography.
 *
 * Motion uses the standard (non-expressive) scheme: the app ships its own subtle
 * spring/tween specs via [defaultSpatialSpec] & friends, and the standard scheme
 * keeps M3 components' internal transitions (button shape morphs, indicator
 * behavior) calm instead of playful.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HarnessTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    progressBarStyle: ProgressBarStyle = ProgressBarStyle.FLAT,
    colorPalette: ThemePalette = ThemePalette.BLUE,
    customPalette: com.hmx.harness.data.CustomThemePalette? = null,
    canvasSeed: Long? = null,
    glassEnabled: Boolean = false,
    liquidGlass: Boolean = false,
    glassTransparency: Float = .5f,
    motionBlurEnabled: Boolean = false,
    motionBlurIntensity: Float = .5f,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val context = LocalContext.current
    val personal = remember(customPalette, canvasSeed, dark) {
        when {
            canvasSeed != null -> canvasColorScheme(canvasSeed, dark)
            customPalette != null -> customPalette.colorScheme(dark)
            else -> null
        }
    }
    val base = when {
        personal != null -> personal
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        else -> colorPalette.colorScheme(dark)
    }
    // AMOLED blacks out the surface roles but keeps the accent roles, so a
    // wallpaper-derived primary survives the switch to true black.
    val colorScheme = if (themeMode == ThemeMode.AMOLED) base.amoledSurfaces() else base
    val glass = HarnessGlassStyle(HarnessGlassMode.resolve(
        glassEnabled && com.hmx.harness.data.FeatureAvailability.HARNESS_GLASS,
        liquidGlass, Build.VERSION.SDK_INT), colorScheme.surface, glassTransparency)

    // Graceful fallback: low-memory devices (e.g. Helio G85 class) get the
    // lightweight layered-gradient material and skip optional GPU-heavy layers.
    val glassQuality = remember {
        if (Runtime.getRuntime().maxMemory() < 320L * 1024 * 1024) LiquidGlassQuality.LITE
        else LiquidGlassQuality.FULL
    }
    // PrismalAGSL records ONLY this background layer. Glass surfaces sample it;
    // app content is a sibling above, so glass never samples itself.
    val glassBackdrop = rememberPrismalGlassLayer()
    CompositionLocalProvider(
        LocalStatusColors provides (if (dark) StatusDark else StatusLight),
        LocalProgressBarStyle provides progressBarStyle,
        LocalMotionBlur provides MotionBlurStyle(motionBlurEnabled, motionBlurIntensity),
        LocalLiquidGlassQuality provides glassQuality,
        LocalHmxGlassBackdrop provides glassBackdrop,
    ) {
        MaterialTheme(
            colorScheme = if (glass.mode == HarnessGlassMode.OFF) colorScheme else colorScheme.glassContainers(),
            typography = HarnessTypography,
            shapes = HarnessShapes,
            motionScheme = MotionScheme.standard(),
            content = {
                Box(Modifier.fillMaxSize()) {
                    LiquidGlassAmbientBackground(
                        androidx.compose.ui.Modifier.matchParentSize().prismalGlassLayer(glassBackdrop),
                        base = colorScheme.surface,
                        accent = colorScheme.primary,
                        secondary = colorScheme.tertiary,
                    )
                    HarnessGlassRoot(glass, content)
                }
            },
        )
    }
}

/**
 * Liquid Glass makes every surface in the app a translucent material, not just
 * the components that opted into [HarnessGlassSurface]. Screens that still use
 * ordinary Material `Surface(color = surfaceContainer…)` then show the recorded
 * backdrop through their panels instead of an opaque sheet, which is what makes
 * the whole app read as glass rather than a flat app with a few glass accents.
 *
 * The alphas mirror the dark palette already shipped in `Color.kt`; text keeps
 * its Material roles, so contrast is unchanged on a solid surface. With glass
 * off, the scheme is returned untouched.
 */
internal fun ColorScheme.glassContainers(): ColorScheme = copy(
    background = androidx.compose.ui.graphics.Color.Transparent,
    surface = androidx.compose.ui.graphics.Color.Transparent,
    surfaceContainerLowest = surfaceContainerLowest.copy(alpha = .90f),
    surfaceContainerLow = surfaceContainerLow.copy(alpha = .80f),
    surfaceContainer = surfaceContainer.copy(alpha = .77f),
    surfaceContainerHigh = surfaceContainerHigh.copy(alpha = .77f),
    surfaceContainerHighest = surfaceContainerHighest.copy(alpha = .77f),
)
