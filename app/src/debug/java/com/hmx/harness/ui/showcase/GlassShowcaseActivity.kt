package com.hmx.harness.ui.showcase

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.graphics.Bitmap
import android.graphics.Canvas
import java.io.File
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmx.harness.AppContainer
import com.hmx.harness.HarnessApp
import com.hmx.harness.core.ChatMessage
import com.hmx.harness.core.Role
import com.hmx.harness.data.AppSettings
import com.hmx.harness.ui.AppNav
import com.hmx.harness.ui.chat.ChatScreen
import com.hmx.harness.ui.chat.ChatViewModel
import com.hmx.harness.ui.settings.ProvidersScreen
import com.hmx.harness.ui.settings.SettingsScreen
import com.hmx.harness.ui.setup.SetupScreen
import com.hmx.harness.ui.theme.HarnessTheme
import java.util.UUID

/**
 * Debug-only host for cloud screenshot QA.
 *
 * Every screen below is the shipped screen composable, rendered with the real
 * [AppContainer], repositories and ViewModels. The activity only picks which
 * screen to show so a screenshot run does not need fragile UI automation.
 * It is never part of a release build.
 */
class GlassShowcaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val container = (application as HarnessApp).container
        val screen = intent.getStringExtra(EXTRA_SCREEN) ?: SCREEN_HOME
        val capture = intent.getStringExtra(EXTRA_CAPTURE)
        val forcedTheme = intent.getStringExtra(EXTRA_THEME)?.let {
            runCatching { com.hmx.harness.data.ThemeMode.valueOf(it.uppercase()) }.getOrNull()
        }
        setContent {
            val settings by container.settings.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
            val themeMode = forcedTheme ?: settings.themeMode
            val dark = when (themeMode) {
                com.hmx.harness.data.ThemeMode.LIGHT -> false
                com.hmx.harness.data.ThemeMode.DARK,
                com.hmx.harness.data.ThemeMode.AMOLED -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            androidx.compose.runtime.SideEffect {
                val controller = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
            }
            HarnessTheme(
                themeMode = themeMode,
                dynamicColor = settings.dynamicColor,
                progressBarStyle = settings.progressBarStyle,
                colorPalette = settings.colorPalette,
                glassEnabled = true,
                liquidGlass = true,
                glassTransparency = settings.harnessGlassTransparency,
                motionBlurEnabled = settings.motionBlurEnabled,
                motionBlurIntensity = settings.motionBlurIntensity,
            ) {
                Box(androidx.compose.ui.Modifier.fillMaxSize()) {
                    when (screen) {
                        SCREEN_ONBOARDING -> SetupScreen(container) {}
                        SCREEN_SETTINGS -> SettingsScreen(container, onBack = {})
                        SCREEN_PROVIDERS -> ProvidersScreen(container, onBack = {})
                        SCREEN_CHAT -> ChatShowcase(container, seeded = true)
                        SCREEN_NAV -> AppNavShowcase(container)
                        else -> ChatShowcase(container, seeded = false)
                    }
                }
            }
        }
        // Debug-only capture: draw the real view hierarchy into a bitmap instead of
        // asking the system screencap service (which kills SwiftShader emulators).
        if (!capture.isNullOrBlank()) {
            window.decorView.postDelayed({
                runCatching {
                    val view = window.decorView
                    if (view.width <= 0 || view.height <= 0) return@runCatching
                    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                    view.draw(Canvas(bitmap))
                    val dir = File(getExternalFilesDir(null), "liquidglass-screenshots").apply { mkdirs() }
                    File(dir, "$capture.png").outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    android.util.Log.i("GlassShowcase", "captured $capture ${view.width}x${view.height}")
                }.onFailure { android.util.Log.w("GlassShowcase", "capture failed for $capture", it) }
            }, 4000)
        }
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val EXTRA_CAPTURE = "capture"
        const val EXTRA_THEME = "theme"
        const val SCREEN_HOME = "home"
        const val SCREEN_ONBOARDING = "onboarding"
        const val SCREEN_SETTINGS = "settings"
        const val SCREEN_PROVIDERS = "providers"
        const val SCREEN_CHAT = "chat"
        const val SCREEN_NAV = "appnav"
    }
}

/** The real chat screen; [seeded] adds a stored conversation so the answer layout is visible. */
@Composable
private fun ChatShowcase(container: AppContainer, seeded: Boolean) {
    var sessionId by remember { mutableStateOf<String?>(null) }
    var ready by remember { mutableStateOf(!seeded) }
    LaunchedEffect(seeded) {
        if (seeded) {
            sessionId = runCatching { seedConversation(container) }.getOrNull()
            ready = true
        }
    }
    if (!ready) return
    val viewModel = remember(sessionId) { ChatViewModel(container, sessionId) }
    ChatScreen(
        viewModel = viewModel,
        onOpenDrawer = {},
        onOpenFile = { _, _ -> },
        onNewChat = {},
        onOpenTerminal = {},
        onOpenSubagent = {},
    )
}

/** Stored messages, not fake UI state: the same rows a real conversation would hold. */
private suspend fun seedConversation(container: AppContainer): String {
    val id = container.sessions.createSession("Liquid Glass interface")
    container.sessions.addMessage(id, ChatMessage(
        id = UUID.randomUUID().toString(),
        role = Role.USER,
        text = "Show me the new Liquid Glass interface.",
        createdAt = System.currentTimeMillis(),
    ))
    container.sessions.addMessage(id, ChatMessage(
        id = UUID.randomUUID().toString(),
        role = Role.ASSISTANT,
        text = "The whole app is glass now: the drawer, the composer and every panel " +
            "sample the background behind them, bend its light at the edges and keep " +
            "the text fully opaque so it stays readable.",
        createdAt = System.currentTimeMillis(),
    ))
    return id
}

/** Real navigation shell, used to photograph the drawer after an edge swipe. */
@Composable
private fun AppNavShowcase(container: AppContainer) {
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        runCatching { container.settings.setOnboardingDone(true) }
        ready = true
    }
    if (ready) AppNav(container)
}
