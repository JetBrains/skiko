package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
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
     * Cached mirror of the native live-resize state, flipped from [onLiveResizeChanged] (called on the
     * AppKit main thread by the window's start/end live-resize observers). Read on the hot [needRender]
     * path and by the frame loop to avoid a JNI hop per frame. See [isAutoResizing].
     */
    @Volatile
    private var isInLiveResize = false

    init {
        onDeviceChosen(adapter.name)
        val numberOfBuffers = properties.frameBuffering.numberOfBuffers() ?: 0 // zero means default for system
        // Live resize is driven from the window, so only enable it when this layer covers the whole
        // window. When embedded as a Swing component (fillsWindow = false), fall back to the legacy path.
        val liveResizeEnabled = SkikoProperties.macOSSynchronousLiveResize && layer.fillsWindow
        val initDevice = layer.backedLayer.useDrawingSurfacePlatformInfo {
            MetalDevice(
                createMetalDevice(
                    window = layer.windowHandle,
                    transparency = layer.transparency,
                    frameBuffering = numberOfBuffers,
                    adapter = adapter.ptr,
                    platformInfo = it,
                    liveResizeEnabled = liveResizeEnabled
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
            scheduleResizeFrame()
        } else {
            frameDispatcher.scheduleFrame(needUpdate = true, throttledToVsync = throttledToVsync)
        }
    }

    override fun renderImmediately() {
        checkDisposed()
        update()
        inDrawScope {
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                performDraw()
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

    private suspend fun draw() {
        inDrawScope {
            // Move drawing to another thread to free the main thread
            // It can be expensive to run it in the main thread and FPS can become unstable.
            // This is visible by running [SkiaLayerPerformanceTest], standard deviation is increased significantly.
            withContext(dispatcherToBlockOn) {
                performDraw()
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

    private fun LayerDrawScope.performDraw() = synchronized(drawLock) {
        if (!isDisposed) {
            autoreleasepool {
                contextHandler.draw()
            }
        }
    }

    /**
     * True while the window is in an interactive (edge-drag) live resize. During that window, resize
     * geometry and presentation are driven from the main thread (`AWTMetalLayer.setBounds` ->
     * [drawInLiveResize]); [SkiaLayer] reads this to suppress the reshape-driven syncBounds/needRender
     * that would otherwise race that path. Regular content/animation needRender still flows through.
     */
    override val isAutoResizing: Boolean
        get() = isInLiveResize

    /**
     * Requests one animation-driven frame on the AppKit main thread during a live resize. Coalescing
     * (at most one pending frame) lives natively in `scheduleFrameOnAppKitThread`, which hops to the
     * main queue and calls back into [drawResizeFrame], where the frame is rendered and presented through
     * the same transactional path as [drawInLiveResize].
     */
    private fun scheduleResizeFrame() {
        if (isDisposed) return
        scheduleFrameOnAppKitThread(device.ptr)
    }

    /**
     * Called from native (`scheduleFrameOnAppKitThread`) on the AppKit main thread. Renders and
     * presents one frame at the layer's current pixel size ([scaledWidth] x [scaledHeight], read on the
     * main thread just before this call). This is the animation counterpart to the setBounds-driven
     * [drawInLiveResize] — both share the one main-thread presenter, so they never race.
     *
     * The native side clears the coalescing flag before this call, so if `onRender` calls [needRender]
     * while this frame draws, the next frame is scheduled and the animation keeps advancing even when the
     * pointer is held still.
     */
    @Suppress("unused")
    fun drawResizeFrame(scaledWidth: Int, scaledHeight: Int) {
        if (isDisposed || !isInLiveResize) return
        drawInLiveResize(scaledWidth, scaledHeight)
    }

    /**
     * Called from `AWTMetalLayer.setBounds` on the AppKit main thread during a live resize.
     *
     * Records fresh content at exactly [width] x [height] pixels on the EDT (synchronously,
     * so no other frame can slip in between), then presents it 1:1 on this (main) thread. Rendering at
     * the present size avoids any scaling, and presenting here — with presentsWithTransaction = YES —
     * joins the same CATransaction that is committing the window's new size, so content, drawableSize
     * and the window backing all update together.
     *
     * The EDT is idle during live resize (its frame loop is gated off), so the synchronous hop can't
     * deadlock against it.
     */
    fun drawInLiveResize(width: Int, height: Int) {
        if (isDisposed || width <= 0 || height <= 0) return

        // 1. Record content at exactly the present size, on the EDT.
        try {
            invokeAndWait {
                if (!isDisposed && layer.isShowing) {
                    layer.update(System.nanoTime(), width, height)
                }
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to record live-resize frame" }
            return
        }

        // 2. Present that frame 1:1 within the window's resize transaction. performDraw takes drawLock
        // and skips if disposed; we can't use inDrawScope here since it asserts the EDT and we're on
        // the AppKit main thread.
        with(LayerDrawScope(layer.pixelGeometry, width, height)) {
            performDraw()
        }
    }

    /**
     * Called from the native live-resize observers on the AppKit main thread on both edges of a resize.
     *
     * On start: cache the flag (so [needRender] and the frame loop switch to the main-thread path) and
     * bootstrap one main-thread frame, so an ongoing animation keeps advancing even if the pointer is
     * held still from the very first moment of the resize.
     *
     * On end: clear the flag and force one more render on the EDT, so the normal EDT-driven present path
     * takes over from a clean, current frame. Hops to the EDT so [FrameDispatcher.scheduleFrame] is only
     * ever touched from that thread.
     */
    @Suppress("unused")
    fun onLiveResizeChanged(isInLiveResize: Boolean) {
        this@MetalRedrawer.isInLiveResize = isInLiveResize
        if (isInLiveResize) {
            scheduleResizeFrame()
        } else {
            invokeLater {
                if (!isDisposed) needRender(throttledToVsync = false)
            }
        }
    }

    override fun syncBounds() = synchronized(drawLock) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
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
     * Hops to the AppKit main thread (main dispatch queue) and calls back into [drawResizeFrame] with
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
