package org.jetbrains.skiko.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.*
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import java.awt.GraphicsEnvironment
import javax.swing.JFrame

internal fun uiTest(block: suspend UiTestScope.() -> Unit) {
    assumeFalse(GraphicsEnvironment.isHeadless())
    assumeTrue(System.getProperty("skiko.test.ui.enabled", "false") == "true")

    val renderApi = System.getProperty("skiko.test.ui.renderApi", "all")

    runBlocking(Dispatchers.Swing) {
        if (renderApi == "all") {
            SkikoProperties.fallbackRenderApiQueue(SkikoProperties.renderApi).forEach {
                println("Testing $it renderApi")
                println()
                UiTestScope(scope = this, renderApi = it).block()
            }
        } else {
            UiTestScope(scope = this, renderApi = SkikoProperties.parseRenderApi(renderApi)).block()
        }
    }
}

internal class UiTestScope(
    private val scope: CoroutineScope,
    val renderApi: GraphicsApi
) : CoroutineScope by scope {
    fun UiTestWindow(
        properties: SkiaLayerProperties = SkiaLayerProperties(),
        renderFactory: RenderFactory = RenderFactory.Default
    ) = object : UiTestWindow() {
        override val layer: SkiaLayer = SkiaLayer(
            properties = properties.copy(renderApi = renderApi),
            renderFactory = renderFactory
        )

        init {
            contentPane.add(layer)
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
