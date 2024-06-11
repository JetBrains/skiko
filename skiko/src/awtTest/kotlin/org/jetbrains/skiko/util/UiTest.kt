package org.jetbrains.skiko.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.skiko.*
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import java.awt.GraphicsEnvironment
import javax.swing.JFrame

internal fun uiTest(
    excludeRenderApis: List<GraphicsApi> = emptyList(),
    block: suspend UiTestScope.() -> Unit
) {
    assumeFalse(GraphicsEnvironment.isHeadless())
    assumeTrue(System.getProperty("skiko.test.ui.enabled", "false") == "true")

    val renderApiProperty = System.getProperty("skiko.test.ui.renderApi", "all")

    runBlocking(MainUIDispatcher) {
        if (renderApiProperty == "all") {
            for (renderApi in SkikoProperties.fallbackRenderApiQueue(SkikoProperties.renderApi)) {
                if (renderApi in excludeRenderApis) {
                    println("Skipping $renderApi renderApi")
                    continue
                }
                println("Testing $renderApi renderApi")
                println()
                UiTestScope(scope = this, renderApi = renderApi).block()
            }
        } else {
            val renderApi = SkikoProperties.parseRenderApi(renderApiProperty)
            if (renderApi in excludeRenderApis) {
                println("Skipping $renderApi renderApi")
            }
            else {
                UiTestScope(scope = this, renderApi = renderApi).block()
            }
        }
    }
}

internal class UiTestScope(
    private val scope: CoroutineScope,
    val renderApi: GraphicsApi
) : CoroutineScope by scope {
    @OptIn(ExperimentalSkikoApi::class)
    fun UiTestWindow(
        properties: SkiaLayerProperties = SkiaLayerProperties(),
        analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
        renderFactory: RenderFactory = RenderFactory.Default,
        setupContent: UiTestWindow.() -> Unit = { contentPane.add(layer) }
    ) = object : UiTestWindow() {
        override val layer: SkiaLayer = SkiaLayer(
            properties = properties.copy(renderApi = renderApi),
            analytics = analytics,
            renderFactory = renderFactory
        )

        init {
            setupContent()
        }

        override fun dispose() {
            layer.dispose()
            super.dispose()
        }
    }
}

internal abstract class UiTestWindow : JFrame() {
    abstract val layer: SkiaLayer
}
