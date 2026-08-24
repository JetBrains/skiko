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
import javax.swing.SwingUtilities

/**
 * Decides when the frames of one [SkiaLayer] happen: it collects frame requests, schedules them on
 * the EDT, pauses its own scheduling while the platform drives frames through a live resize, and
 * produces the frame that must be ready before the window is first shown. The [renderer] renders
 * and presents what the driver asks for.
 */
@OptIn(ExperimentalSkikoApi::class)
internal class FrameDriver(
    private val layer: SkiaLayer,
    private val renderer: AwtRenderer,
    private val scheduler: FrameScheduler? = null,
) : LiveResizeListener {
    private val deviceAnalytics: DeviceAnalytics? get() = renderer.deviceAnalytics
    private var isFirstFrameRendered = false

    var isDisposed = false
        private set

    /**
     * `true` while the platform drives the frames itself (an interactive live resize). The driver
     * then leaves AWT resize events alone and routes frame requests to the platform's scheduler.
     */
    @Volatile
    private var isPlatformDrivingFrames = false

    init {
        renderer.liveResizeListener = this
    }

    val renderInfo: String get() = renderer.renderInfo
    fun isTransparentBackgroundSupported(): Boolean = renderer.isTransparentBackgroundSupported()

    val presentsOnLayout: Boolean get() = renderer.presentsOnResize && !isPlatformDrivingFrames

    private val updateRequested = AtomicBoolean(false)
    internal fun updateIfRequested(nanoTime: Long = renderTime()) {
        if (updateRequested.getAndSet(false)) {
            layer.update(nanoTime)
        }
    }

    private val frameDispatcher = if (scheduler != null) null else {
        FrameDispatcher(MainUIDispatcher) {
            if (!isPlatformDrivingFrames) {
                renderer.runFrame {
                    if (layer.isShowing) {
                        updateIfRequested()
                        drawFrame(immediate = false)
                    }
                }
            }
        }
    }

    // Records the next frame's content on the EDT while the previous frame still waits for vsync.
    private val earlyRecordDispatcher = if (!renderer.pacesAfterFrame) null else {
        FrameDispatcher(MainUIDispatcher) {
            if (layer.isShowing && !isPlatformDrivingFrames) updateIfRequested()
        }
    }

    fun needRender(throttledToVsync: Boolean) {
        check(!isDisposed) { "FrameDriver is disposed" }

        if (isPlatformDrivingFrames) {
            renderer.requestPlatformDrivenFrame()
        } else {
            updateRequested.set(true)
            if (!throttledToVsync) {
                earlyRecordDispatcher?.scheduleFrame()
            }
            if (scheduler != null) scheduler.scheduleFrame(this) else frameDispatcher?.scheduleFrame()
        }
    }

    fun renderImmediately() {
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

    internal fun inFrame(body: (LayerDrawScope) -> Unit) {
        if (isDisposed) return
        withFrameAnalytics {
            layer.inDrawScope { body(this) }
        }
    }

    override fun onLiveResizeStarted() {
        isPlatformDrivingFrames = true
    }

    override fun onLiveResizeFrame(width: Int, height: Int, isResizeFrame: Boolean) {
        if (isDisposed) return
        val size = Dimension(width, height)
        layer.update(renderTime(), forcedSize = size)
        if (isDisposed) return // layer may be disposed in user code during `update`
        withFrameAnalytics {
            layer.inDrawScope(forcedSize = size) {
                renderer.renderPlatformDrivenFrame(this, isResizeFrame)
            }
        }
    }

    override fun onLiveResizeEnded() {
        isPlatformDrivingFrames = false
        if (renderer.presentsOnResize) {
            renderImmediately()
        } else {
            SwingUtilities.invokeLater {
                if (!isDisposed) needRender(throttledToVsync = false)
            }
        }
    }

    fun syncBoundsFromPlatformComponent() {
        // During a live resize the platform sizes the layers itself; syncing the lagging AWT
        // bounds would fight it.
        if (isPlatformDrivingFrames) return
        renderer.syncBoundsFromPlatformComponent()
    }

    fun onLayerComponentResized() {
        // During live resize, the layer tells us its size directly; the AWT size is not in sync
        if (isPlatformDrivingFrames) return

        syncBoundsFromPlatformComponent()

        if (!layer.isShowing && layer.isDisplayable && layer.width > 0 && layer.height > 0) {
            if (renderer.needsBeforeShownFrame) {
                layer.update(renderTime())
                if (isDisposed) return // layer may be disposed in user code during `update`
                withFrameAnalytics {
                    layer.inDrawScope { renderer.renderBeforeShown(this) }
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
        earlyRecordDispatcher?.cancel()
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
