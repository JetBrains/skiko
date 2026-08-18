package org.jetbrains.skiko.renderer

import kotlinx.coroutines.runBlocking
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.renderTime
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import org.jetbrains.skiko.LayerDrawScope
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Schedules the frames of one [SkiaLayer] on the EDT and drives the [renderer] that renders and
 * presents them.
 */
@OptIn(ExperimentalSkikoApi::class)
internal class FrameDriver(
    private val layer: SkiaLayer,
    private val renderer: AwtRenderer,
    private val scheduler: FrameScheduler? = null,
) : FrameHost {
    private val deviceAnalytics: DeviceAnalytics? get() = renderer.deviceAnalytics
    private var isFirstFrameRendered = false

    var isDisposed = false
        private set

    init {
        renderer.attachFrameHost(this)
    }

    val renderInfo: String get() = renderer.renderInfo
    fun isTransparentBackgroundSupported(): Boolean = renderer.isTransparentBackgroundSupported()

    val presentsOnLayout: Boolean get() = renderer.presentsOnLayout

    private val updateRequested = AtomicBoolean(false)
    override fun updateIfRequested(nanoTime: Long) {
        if (updateRequested.getAndSet(false)) {
            layer.update(nanoTime)
        }
    }

    private val frameDispatcher = if (scheduler != null) null else {
        FrameDispatcher(MainUIDispatcher) {
            renderer.runFrame {
                if (layer.isShowing) {
                    updateIfRequested()
                    drawFrame(immediate = false)
                }
            }
        }
    }

    fun needRender(throttledToVsync: Boolean) {
        check(!isDisposed) { "FrameDriver is disposed" }

        val platformDrivesFrame = renderer.isHandlingLiveResizeNow
        if (!platformDrivesFrame) {
            updateRequested.set(true)
        }
        renderer.onFrameRequested(throttledToVsync)
        if (!platformDrivesFrame) {
            if (scheduler != null) scheduler.scheduleFrame(this) else frameDispatcher?.scheduleFrame()
        }
    }

    override fun renderImmediately() {
        check(!isDisposed) { "FrameDriver is disposed" }
        layer.update(renderTime())
        if (!isDisposed) { // layer may be disposed in user code during `update`
            runBlocking { drawFrame(immediate = true) }
        }
    }

    private suspend fun drawFrame(immediate: Boolean) {
        if (isDisposed) return
        withFrameAnalytics {
            layer.inDrawScope {
                renderer.renderFrame(this, immediate)
            }
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

    override fun requestFrame(throttledToVsync: Boolean) = needRender(throttledToVsync)

    override fun inFrame(body: (LayerDrawScope) -> Unit) {
        if (isDisposed) return
        withFrameAnalytics {
            layer.inDrawScope { body(this) }
        }
    }

    override fun inForcedSizeFrame(size: Dimension, body: (LayerDrawScope) -> Unit) {
        if (isDisposed) return
        layer.update(renderTime(), forcedSize = size)
        if (isDisposed) return // layer may be disposed in user code during `update`
        withFrameAnalytics {
            layer.inDrawScope(forcedSize = size) { body(this) }
        }
    }

    fun syncBoundsFromPlatformComponent() = renderer.syncBoundsFromPlatformComponent()

    fun onLayerComponentResized() {
        // During live resize, the layer tells us its size directly; the AWT size is not in sync
        if (renderer.isHandlingLiveResizeNow) return

        syncBoundsFromPlatformComponent()

        if (!layer.isShowing && layer.isDisplayable && layer.width > 0 && layer.height > 0) {
            if (!renderer.presentsBeforeShown) return
            layer.update(renderTime())
            if (isDisposed) return // layer may be disposed in user code during `update`
            withFrameAnalytics {
                layer.inDrawScope {
                    val scope = this
                    if (!renderer.renderBeforeShown(scope)) {
                        runBlocking { renderer.renderFrame(scope, immediate = true) }
                    }
                }
            }
            return
        }

        needRender(throttledToVsync = false)
    }

    fun setVisible(isVisible: Boolean) = renderer.setVisible(isVisible)

    fun dispose() {
        if (isDisposed) return
        isDisposed = true
        frameDispatcher?.cancel()
        renderer.close()
    }
}

/**
 * Schedules the driver's next frame. A [FrameDriver] constructed with one delegates scheduling to
 * it instead of running its own dispatcher.
 */
internal fun interface FrameScheduler {
    fun scheduleFrame(driver: FrameDriver)
}
