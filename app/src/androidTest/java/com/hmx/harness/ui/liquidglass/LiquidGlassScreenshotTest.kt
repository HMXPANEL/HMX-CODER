package com.hmx.harness.ui.liquidglass

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hmx.harness.HarnessApp
import com.hmx.harness.data.ThemeMode
import com.hmx.harness.ui.chat.ChatListTestActivity
import com.hmx.harness.ui.chat.components.ChatEmptyState
import com.hmx.harness.ui.common.AppHeader
import com.hmx.harness.ui.projects.ProjectsScreen
import com.hmx.harness.ui.settings.ProvidersScreen
import com.hmx.harness.ui.settings.SettingsScreen
import com.hmx.harness.ui.theme.HarnessTheme
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Captures real rendered HMXHARNESS screens for visual QA in CI.
 * Presentation only: it composes existing screens with the production theme
 * and never invokes a provider, agent or tool.
 */
@RunWith(AndroidJUnit4::class)
class LiquidGlassScreenshotTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val container get() = (instrumentation.targetContext.applicationContext as HarnessApp).container

    private fun capture(name: String) {
        instrumentation.waitForIdleSync()
        Thread.sleep(500)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        val dir = File(container.appContext.getExternalFilesDir(null), "liquidglass-screenshots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun withScreen(name: String, content: @Composable () -> Unit) {
        ActivityScenario.launch(ChatListTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    HarnessTheme(themeMode = ThemeMode.DARK, dynamicColor = false, glassEnabled = true, liquidGlass = true) {
                        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { content() }
                    }
                }
            }
            capture(name)
        }
    }

    @Test fun captureHome() = withScreen("01-home") {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            ChatEmptyState(null, null, true, {}, {}, Modifier, code = true)
        }
    }

    @Test fun captureHeaderAndGlassCards() = withScreen("02-chrome") {
        androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp)) {
            AppHeader(title = "HMXHARNESS", subtitle = "Liquid Glass")
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            LiquidGlassCard { Text("Standard glass card", style = MaterialTheme.typography.titleMedium) }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            LiquidGlassCard(level = LiquidGlassLevel.FOCUS) {
                Text("Focus glass card", style = MaterialTheme.typography.titleMedium)
            }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            LiquidGlassButton(onClick = {}) { Text("Glass action") }
        }
    }

    @Test fun captureProviders() = withScreen("03-providers") {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) { ProvidersScreen(container, {}) }
    }

    @Test fun captureProjects() = withScreen("04-projects") {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) { ProjectsScreen(container, {}, {}, {}) }
    }

    @Test fun captureSettings() = withScreen("05-settings") {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) { SettingsScreen(container, {}) }
    }
}
