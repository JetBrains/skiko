package org.jetbrains.skiko.renderer

import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicBoolean

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
    private val updateRequested = AtomicBoolean(false)

    var isDisposed = false
        private set

    fun requestUpdate() {
        updateRequested.set(true)
    }

    fun updateIfRequested(nanoTime: Long) {
        if (updateRequested.getAndSet(false)) {
            layer.update(nanoTime)
        }
    }

    /** Renders and presents one frame through [AwtRenderer.renderFrame]. */
    suspend fun drawFrame(immediate: Boolean) {
        if (isDisposed) return
        withFrameAnalytics {
            layer.inDrawScope {
                renderer.renderFrame(this, immediate)
            }
        }
    }

    /** Runs [body] as one frame: inside the layer's draw scope, wrapped in the frame analytics. */
    fun inFrame(forcedSize: Dimension? = null, body: (LayerDrawScope) -> Unit) {
        if (isDisposed) return
        withFrameAnalytics {
            layer.inDrawScope(forcedSize) { body(this) }
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
