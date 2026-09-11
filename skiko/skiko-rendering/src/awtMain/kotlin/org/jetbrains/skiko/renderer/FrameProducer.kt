package org.jetbrains.skiko.renderer

import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import java.awt.Dimension

/**
 * Produces the frames of one [SkiaLayer]: records the layer's content, renders it through the
 * [renderer], and carries the per-frame analytics. [FrameDriver] and the [FrameScheduler] decide
 * when each step runs.
 */
@OptIn(ExperimentalSkikoApi::class)
internal class FrameProducer(
    internal val layer: SkiaLayer,
    internal val renderer: AwtRenderer,
) {
    private val deviceAnalytics: DeviceAnalytics? get() = renderer.deviceAnalytics
    private var isFirstFrameRendered = false

    var isDisposed = false
        private set

    fun update(nanoTime: Long, forcedSize: Dimension? = null) = layer.update(nanoTime, forcedSize)

    /** Renders and presents one frame through [AwtRenderer.renderFrame]. */
    suspend fun drawFrame(immediate: Boolean) {
        if (isDisposed) return
        withFrameAnalytics {
            layer.inDrawScope {
                with(renderer) { renderFrame(immediate) }
            }
        }
    }

    /** Runs [body] as one frame: inside the layer's draw scope, wrapped in the frame analytics. */
    fun inFrame(forcedSize: Dimension? = null, body: LayerDrawScope.() -> Unit) {
        if (isDisposed) return
        withFrameAnalytics {
            layer.inDrawScope(forcedSize) { body() }
        }
    }

    private inline fun withFrameAnalytics(body: () -> Unit) {
        val isFirstFrame = !isFirstFrameRendered
        isFirstFrameRendered = true
        if (isFirstFrame) deviceAnalytics?.beforeFirstFrameRender()
        deviceAnalytics?.beforeFrameRender()
        body()
        if (isFirstFrame && !isDisposed) deviceAnalytics?.afterFirstFrameRender()
        deviceAnalytics?.afterFrameRender()
    }

    fun dispose() {
        if (isDisposed) return
        isDisposed = true
        renderer.close()
    }
}
