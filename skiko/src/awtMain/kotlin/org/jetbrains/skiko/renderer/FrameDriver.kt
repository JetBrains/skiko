package org.jetbrains.skiko.renderer

import kotlinx.coroutines.runBlocking
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.renderTime
import java.awt.Dimension
import javax.swing.SwingUtilities

/**
 * The frame policy of one [SkiaLayer]: routes frame requests to the [scheduler], or to the
 * platform's own frame loop during a live resize. It produces the immediate and before-shown
 * frames itself; disposing it disposes the whole render stack.
 */
@OptIn(ExperimentalSkikoApi::class)
internal class FrameDriver(
    private val layer: SkiaLayer,
    private val producer: FrameProducer,
    private val scheduler: FrameScheduler,
) {
    internal val renderer: AwtRenderer get() = producer.renderer

    var isDisposed = false
        private set

    /**
     * `true` while the platform drives the frames itself (an interactive live resize). The driver
     * then leaves AWT resize events alone and routes frame requests to the platform's scheduler.
     */
    @Volatile
    private var isPlatformDrivingFrames = false

    private val liveResizeListener = object : LiveResizeListener {
        override fun onLiveResizeStarted() {
            isPlatformDrivingFrames = true
            scheduler.pause()
        }

        override fun onLiveResizeFrame(width: Int, height: Int, isResizeFrame: Boolean) {
            if (isDisposed) return
            val size = Dimension(width, height)
            producer.update(renderTime(), forcedSize = size)
            if (isDisposed) return // layer may be disposed in user code during `update`
            producer.inFrame(forcedSize = size) {
                with(renderer) { renderPlatformDrivenFrame(isResizeFrame) }
            }
        }

        override fun onLiveResizeEnded() {
            isPlatformDrivingFrames = false
            scheduler.resume()
            if (renderer.presentsOnResize) {
                renderImmediately()
            } else {
                SwingUtilities.invokeLater {
                    if (!isDisposed) needRender(throttledToVsync = false)
                }
            }
        }
    }

    init {
        renderer.liveResizeListener = liveResizeListener
    }

    val renderInfo: String get() = renderer.renderInfo
    fun isTransparentBackgroundSupported(): Boolean = renderer.isTransparentBackgroundSupported()

    val presentsOnLayout: Boolean get() = renderer.presentsOnResize && !isPlatformDrivingFrames

    fun needRender(throttledToVsync: Boolean) {
        check(!isDisposed) { "FrameDriver is disposed" }

        if (isPlatformDrivingFrames) {
            renderer.requestPlatformDrivenFrame()
        } else {
            scheduler.scheduleFrame(throttledToVsync)
        }
    }

    fun renderImmediately() {
        check(!isDisposed) { "FrameDriver is disposed" }
        producer.update(renderTime())
        if (!isDisposed) { // layer may be disposed in user code during `update`
            runBlocking { producer.drawFrame(immediate = true) }
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
                producer.update(renderTime())
                if (isDisposed) return // layer may be disposed in user code during `update`
                producer.inFrame { with(renderer) { renderBeforeShown() } }
            }
            return
        }

        needRender(throttledToVsync = false)
    }

    fun setVisible(isVisible: Boolean) = renderer.setVisible(isVisible)

    fun dispose() {
        if (isDisposed) return
        isDisposed = true
        scheduler.cancel()
        producer.dispose()
    }
}
