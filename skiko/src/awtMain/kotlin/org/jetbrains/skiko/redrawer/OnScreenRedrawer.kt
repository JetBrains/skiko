package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.runBlocking
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerAnalytics
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.Version
import org.jetbrains.skiko.hostOs
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The single generic on-screen frame loop for AWT [SkiaLayer]s: it owns the coalescing frame dispatcher and
 * drives any [AWTRedrawer] through its pacing/present hooks, so the scheduling logic exists once here rather
 * than per backend. Each backend places its pacing by implementing [AWTRedrawer.paceBeforeFrame] and/or
 * [AWTRedrawer.paceAfterFrame]. Metal additionally opts into a separate update ticker
 * ([AWTRedrawer.separatesUpdateAndDraw]) so a non-throttled [needRender] can run [SkiaLayer.update] without
 * waiting for the vsync-paced frame.
 */
internal class OnScreenRedrawer(
    private val layer: SkiaLayer,
    val ctx: AWTRedrawer,
    analytics: SkiaLayerAnalytics,
) : Redrawer, FrameHost {
    private val rendererAnalytics = analytics.renderer(Version.skiko, hostOs, ctx.graphicsApi)
    private val deviceAnalytics: DeviceAnalytics?
    private var isFirstFrameRendered = false

    var isDisposed = false
        private set

    init {
        rendererAnalytics.init()
        rendererAnalytics.deviceChosen()
        deviceAnalytics = analytics.device(Version.skiko, hostOs, ctx.graphicsApi, ctx.deviceName).also {
            it.init()
            it.contextInit()
        }
        ctx.attachFrameHost(this)
    }

    override val renderInfo: String get() = ctx.renderInfo
    override fun isTransparentBackgroundSupported(): Boolean = ctx.isTransparentBackgroundSupported()

    private val updateRequested = AtomicBoolean(false)
    private fun updateIfRequested() {
        if (updateRequested.getAndSet(false)) {
            layer.update(System.nanoTime())
        }
    }

    // Separate coalescing update ticker (Metal only): lets a non-throttled needRender advance the layer's
    // state without waiting for the vsync-paced frame.
    private val updateDispatcher: FrameDispatcher? = if (ctx.separatesUpdateAndDraw) {
        FrameDispatcher(MainUIDispatcher) {
            if (layer.isShowing) updateIfRequested()
        }
    } else null

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        ctx.paceBeforeFrame()
        if (layer.isShowing) {
            updateIfRequested()
            drawFrame(immediate = false)
        }
        ctx.paceAfterFrame()
    }

    override fun needRender(throttledToVsync: Boolean) {
        check(!isDisposed) { "OnScreenRedrawer is disposed" }

        // A context that drives its own frames (e.g. macOS live resize) takes over here instead of the
        // internal dispatcher; see [AWTRedrawer.interceptFrameScheduling].
        if (ctx.interceptFrameScheduling()) {
            ctx.onFrameSchedulingIntercepted(throttledToVsync)
            return
        }

        updateRequested.set(true)
        if (updateDispatcher != null && !throttledToVsync) {
            updateDispatcher.scheduleFrame()
        }
        frameDispatcher.scheduleFrame()
    }

    override fun renderImmediately() {
        check(!isDisposed) { "OnScreenRedrawer is disposed" }
        layer.update(System.nanoTime())
        if (!isDisposed) { // layer may be disposed in user code during `update`
            runBlocking { drawFrame(immediate = true) }
        }
    }

    private suspend fun drawFrame(immediate: Boolean) {
        if (isDisposed) return
        withFrameAnalytics {
            // inDrawScope owns the RenderException -> next-API fallback (see SkiaLayer.inDrawScope).
            layer.inDrawScope {
                ctx.renderFrame(this, immediate)
            }
        }
    }

    private inline fun withFrameAnalytics(body: () -> Unit) {
        val isFirstFrame = !isFirstFrameRendered
        // Claim first-frame status before running the body, which suspends on the backends that hand the
        // GPU work off the EDT. A frame that suspends here must not let the next one also see itself as
        // the first, or `beforeFirstFrameRender` is reported twice.
        isFirstFrameRendered = true
        if (isFirstFrame) deviceAnalytics?.beforeFirstFrameRender()
        deviceAnalytics?.beforeFrameRender()
        body()
        if (isFirstFrame && !isDisposed) deviceAnalytics?.afterFirstFrameRender()
        deviceAnalytics?.afterFrameRender()
    }

    override fun requestFrame(throttledToVsync: Boolean) = needRender(throttledToVsync)

    override fun inForcedSizeFrame(size: Dimension, body: (LayerDrawScope) -> Unit) {
        if (isDisposed) return
        layer.update(System.nanoTime(), forcedSize = size)
        if (isDisposed) return // layer may be disposed in user code during `update`
        withFrameAnalytics {
            layer.inDrawScope(forcedSize = size) { body(this) }
        }
    }

    override fun update(nanoTime: Long) = layer.update(nanoTime)

    override fun onPlatformComponentResized() {
        // While the platform drives frames (macOS live resize) it also dictates the size, and the AWT
        // component's own size lags behind it; adopting that stale size would present a wrong-sized frame.
        if (ctx.interceptFrameScheduling()) return
        super.onPlatformComponentResized()
    }

    override fun syncBoundsFromPlatformComponent() = ctx.syncBounds()
    override fun setVisible(isVisible: Boolean) = ctx.setVisible(isVisible)

    override fun dispose() {
        if (isDisposed) return
        isDisposed = true
        updateDispatcher?.cancel()
        frameDispatcher.cancel()
        ctx.dispose()
    }
}
