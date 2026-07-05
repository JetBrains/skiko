package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.skia.ISize
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.MetalContextHandler
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Holder for pointer on MetalDevice described in "MetalDevice.h"
 *
 * Naturally [MetalDevice] is just a holder for native objects required for drawing such as:
 *   * [CAMetalLayer](https://developer.apple.com/documentation/quartzcore/cametallayer)
 *   * [MTLDevice](https://developer.apple.com/documentation/metal/mtldevice)
 *   * etc.
 *
 * @see "src/awtMain/objectiveC/macos/MetalDevice.h"
 */
@JvmInline
internal value class MetalDevice(val ptr: Long)

/**
 * Provides a way to request draws on Skia canvas created in [layer] bounds using Metal GPU acceleration.
 *
 * This [MetalRedrawer] draws content on-screen for maximum efficiency,
 * but it may prevent for using it in embedded components (such as interop with Swing).
 *
 * Content to draw is provided by [SkiaLayer.draw].
 *
 * @see MetalContextHandler
 * @see FrameDispatcher
 */
internal class MetalRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.METAL) {
    private val contextHandler: MetalContextHandler

    companion object {
        init {
            Library.load()
        }
    }

    private var drawLock = Any()

    /**
     * [MetalDevice] initialized for given [layer] or null if [MetalRedrawer] is disposed,
     * so future calls of [device] will throw exception
     */
    private var _device: MetalDevice?

    private val device: MetalDevice
        get() {
            val currentDevice = _device
            require(currentDevice != null) { "Device is disposed" }
            return currentDevice
        }

    private val adapter = chooseMetalAdapter(properties.adapterPriority)
    private val vSyncer = if (properties.isVsyncEnabled) MetalVSyncer(layer.windowHandle) else null

    private val windowOcclusionStateChannel = Channel<Boolean>(Channel.CONFLATED)
    @Volatile private var isWindowOccluded = false

    /**
     * The layer size during a live resize session; `null` when not in live resize.
     */
    @Volatile
    private var layerSizeInLiveResize: ISize? = null

    private val isInLiveResize: Boolean
        get() = layerSizeInLiveResize != null

    override val layerSizeWhileHandlingSizing: ISize?
        get() = layerSizeInLiveResize

    init {
        onDeviceChosen(adapter.name)
        val numberOfBuffers = properties.frameBuffering.numberOfBuffers() ?: 0 // zero means default for system
        val initDevice = layer.backedLayer.useDrawingSurfacePlatformInfo {
            MetalDevice(
                createMetalDevice(
                    window = layer.windowHandle,
                    transparency = layer.transparency,
                    frameBuffering = numberOfBuffers,
                    adapter = adapter.ptr,
                    platformInfo = it,
                    // Live resize is driven from the window, so only enable it when this layer covers the whole
                    // window. When embedded as a Swing component (fillsWindow = false), fall back to the legacy path.
                    liveResizeEnabled = layer.fillsWindow && SkikoProperties.metalSynchronousLiveResize
                )
            )
        }
        _device = initDevice
        contextHandler = MetalContextHandler(layer, initDevice, adapter)
        setDisplaySyncEnabled(initDevice.ptr, properties.isVsyncEnabled)
    }

    override val renderInfo: String get() = contextHandler.rendererInfo()

    private val frameDispatcher = FrameScheduler()

    init {
        onContextInit()
    }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        contextHandler.dispose()
        disposeDevice(device.ptr)
        adapter.dispose()
        vSyncer?.dispose()
        _device = null
        super.dispose()
    }

    override fun needRender(throttledToVsync: Boolean) {
        checkDisposed()
        if (isInLiveResize) {
            // The background frame loop is gated off during a resize (two presenters deadlock / starve
            // the drawable pool), so drive animation frames from the AppKit main thread instead — the
            // same single serialized presenter that setBounds uses.
            scheduleFrameOnAppKitThread()
        } else {
            frameDispatcher.scheduleFrame(needUpdate = true, throttledToVsync = throttledToVsync)
        }
    }

    override fun renderImmediately() {
        checkDisposed()
        update()
        inDrawScope {
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                performDraw(flush = true)
                // Trying to draw immediately in Metal will result in lost (undrawn)
                // frames if there are more than two between consecutive vsync events.
                if (SkikoProperties.macOSWaitForPreviousFrameVsyncOnRedrawImmediately) {
                    runBlocking {
                        vSyncer?.waitForVSync()
                    }
                }
            }
        }
    }

    private suspend fun draw(flush: Boolean = true) {
        inDrawScope {
            // Move drawing to another thread to free the main thread
            // It can be expensive to run it in the main thread, and FPS can become unstable.
            // This is visible by running [SkiaLayerPerformanceTest], standard deviation is increased significantly.
            withContext(dispatcherToBlockOn) {
                performDraw(flush = flush)
            }
        }
        if (isDisposed) throw CancellationException()

        // When window is not visible - it doesn't make sense to redraw fast to avoid battery drain.
        if (isWindowOccluded) {
            withTimeoutOrNull(300.milliseconds) {
                // If the window becomes non-occluded, stop waiting immediately
                @Suppress("ControlFlowWithEmptyBody")
                while (windowOcclusionStateChannel.receive()) { }
            }
        }
    }

    // Called from MetalRedrawer.mm
    @Suppress("unused")
    fun onOcclusionStateChanged(isOccluded: Boolean) {
        isWindowOccluded = isOccluded
        windowOcclusionStateChannel.trySend(isOccluded)
    }

    private fun LayerDrawScope.performDraw(flush: Boolean) {
        synchronized(drawLock) {
            if (!isDisposed) {
                autoreleasepool {
                    contextHandler.draw(flush)
                }
            }
        }
    }

    /**
     * Called from native code, on the AppKit main thread, when a live resizing session starts.
     */
    @Suppress("unused")
    fun onLiveResizeStarted(width: Int, height: Int) {
        layerSizeInLiveResize = ISize(width, height)
        scheduleFrameOnAppKitThread()
    }

    /**
     * Called from native code, on the AppKit main thread, when a live resizing session ends.
     */
    @Suppress("unused")
    fun onLiveResizeEnded() {
        layerSizeInLiveResize = null
        invokeLater {
            if (!isDisposed) {
                needRender(throttledToVsync = false)
            }
        }
    }

    /**
     * Called from native code, on the AppKit main thread, to draw a frame during live resize.
     */
    @Suppress("unused")
    fun drawFrameWhileLiveResizing(width: Int, height: Int) {
        if (isDisposed || width <= 0 || height <= 0) return

        layerSizeInLiveResize = ISize(width, height)

        // Record content at exactly the present size, on the EDT.
        try {
            runBlocking(MainUIDispatcher) {
                update()
                // During live resize `finishFrame` (called by `MetalContextHandler.flush()`)
                // must be called on the AppKit main thread to join the resize transaction.
                draw(flush = false)
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to record live-resize frame" }
            return
        }

        // `finishFrame` (called by `MetalContextHandler.flush()`) needs to be called back on the AppKit main thread
        synchronized(drawLock) {
            if (!isDisposed) {
                contextHandler.flush()
            }
        }
    }

    /**
     * Requests one frame on the AppKit main thread (during a live resize).
     *
     * Coalescing (at most one pending frame) lives natively in `scheduleFrameOnAppKitThread`, which hops to the
     * main queue and calls back into [drawFrameWhileLiveResizing], where the frame is rendered and presented.
     */
    private fun scheduleFrameOnAppKitThread() {
        if (isDisposed) return
        scheduleFrameOnAppKitThread(device.ptr)
    }

    override fun syncBoundsFromPlatformComponent() = synchronized(drawLock) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isInLiveResize) return

        val rootPane = getRootPane(layer)
        val globalPosition = convertPoint(layer.backedLayer, 0, 0, rootPane)
        setContentScale(device.ptr, layer.contentScale)
        val x = globalPosition.x
        val y = rootPane.height - globalPosition.y - layer.height
        val width = layer.backedLayer.width.coerceAtLeast(0)
        val height = layer.backedLayer.height.coerceAtLeast(0)
        Logger.debug { "MetalRedrawer#resizeLayers $this {x: $x y: $y width: $width height: $height} rootPane: ${rootPane.size}" }
        resizeLayers(device.ptr, x, y, width, height)
    }

    override fun setVisible(isVisible: Boolean) {
        Logger.debug { "MetalRedrawer#setVisible($isVisible)" }
        setLayerVisible(device.ptr, isVisible)
    }

    private external fun createMetalDevice(window: Long, transparency: Boolean, frameBuffering: Int, adapter: Long, platformInfo: Long, liveResizeEnabled: Boolean): Long
    private external fun disposeDevice(device: Long)
    private external fun resizeLayers(device: Long, x: Int, y: Int, width: Int, height: Int)
    private external fun setLayerVisible(device: Long, isVisible: Boolean)
    private external fun setContentScale(device: Long, contentScale: Float)

    /**
     * Hops to the AppKit main thread (main dispatch queue) and calls back into [drawFrameWhileLiveResizing] with
     * the layer's current pixel size. Used to drive frames through the single main-thread presenter during
     * a live resize.
     */
    private external fun scheduleFrameOnAppKitThread(device: Long)

    /**
     * Set this value to true to synchronize the presentation of the layer’s contents with the display’s refresh,
     * also known as vsync or vertical sync. If false, the layer presents new content more quickly,
     * but possibly with brief visual artifacts (screen tearing).
     *
     * @note see https://developer.apple.com/documentation/quartzcore/cametallayer/2887087-displaysyncenabled
     */
    private external fun setDisplaySyncEnabled(device: Long, enabled: Boolean)

    private inner class FrameScheduler {
        private var updateRequested = AtomicBoolean(false)

        private fun updateIfRequested() {
            if (updateRequested.getAndSet(false)) {
                update()
            }
        }

        private val updateDispatcher = FrameDispatcher(MainUIDispatcher) {
            // Gated off during a live resize: presentation is driven from the AppKit main thread
            if (layer.isShowing && !isInLiveResize) {
                updateIfRequested()
            }
        }

        private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
            if (layer.isShowing && !isInLiveResize) {
                updateIfRequested()
                draw()
            }
            vSyncer?.waitForVSync()
        }

        fun scheduleFrame(needUpdate: Boolean, throttledToVsync: Boolean) {
            if (needUpdate) {
                updateRequested.set(true)

                if (!throttledToVsync) {
                    updateDispatcher.scheduleFrame()
                }
            }
            frameDispatcher.scheduleFrame()
        }

        fun cancel() {
            updateDispatcher.cancel()
            frameDispatcher.cancel()
        }
    }
}
