package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.runBlocking
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import org.jetbrains.skiko.LayerDrawScope
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalSkikoApi::class)
internal class OnScreenRedrawer(
    private val layer: SkiaLayer,
    private val renderer: AWTRedrawer,
) : Redrawer, FrameHost {
    private val deviceAnalytics: DeviceAnalytics? get() = renderer.deviceAnalytics
    private var isFirstFrameRendered = false

    var isDisposed = false
        private set

    init {
        renderer.attachFrameHost(this)
    }

    override val renderInfo: String get() = renderer.renderInfo
    override fun isTransparentBackgroundSupported(): Boolean = renderer.isTransparentBackgroundSupported()

    val presentsOnLayout: Boolean get() = renderer.presentsOnLayout

    private val updateRequested = AtomicBoolean(false)
    override fun updateIfRequested(nanoTime: Long) {
        if (updateRequested.getAndSet(false)) {
            layer.update(nanoTime)
        }
    }

    private val frameDispatcher = if (renderer.schedulesOwnFrames) null else {
        FrameDispatcher(MainUIDispatcher) {
            renderer.runFrame {
                if (layer.isShowing) {
                    updateIfRequested()
                    drawFrame(immediate = false)
                }
            }
        }
    }

    override fun needRender(throttledToVsync: Boolean) {
        check(!isDisposed) { "OnScreenRedrawer is disposed" }

        val platformDrivesFrame = renderer.isHandlingLiveResizeNow
        if (!platformDrivesFrame) {
            updateRequested.set(true)
        }
        renderer.onFrameRequested(throttledToVsync)
        if (!platformDrivesFrame) {
            frameDispatcher?.scheduleFrame()
        }
    }

    override fun renderImmediately() {
        check(!isDisposed) { "OnScreenRedrawer is disposed" }
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

    override fun update(nanoTime: Long) = layer.update(nanoTime)

    override fun syncBoundsFromPlatformComponent() = renderer.syncBounds()

    override fun onLayerComponentResized() {
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
    override fun setVisible(isVisible: Boolean) = renderer.setVisible(isVisible)

    override fun dispose() {
        if (isDisposed) return
        isDisposed = true
        frameDispatcher?.cancel()
        renderer.close()
    }
}
