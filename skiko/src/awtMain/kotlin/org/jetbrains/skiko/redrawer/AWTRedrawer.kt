package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.*
import org.jetbrains.skiko.LockFile.Companion.skiko
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import java.awt.Dimension

/**
 * Common class for all AWT redrawers.
 * Don't forget to call [onDeviceChosen] and [onContextInit] to send necessary analytics.
 */
@OptIn(ExperimentalSkikoApi::class)
internal abstract class AWTRedrawer(
    protected val layer: SkiaLayer,
    private val analytics: SkiaLayerAnalytics,
    private val graphicsApi: GraphicsApi,
) : AutoCloseable {
    private val rendererAnalytics = analytics.renderer(Version.skiko, hostOs, graphicsApi)

    var deviceAnalytics: DeviceAnalytics? = null
        private set

    @Volatile
    protected var isDisposed = false
        private set

    init {
        rendererAnalytics.init()
    }

    /**
     * Should be called when the device name is known as early, as possible.
     */
    protected fun onDeviceChosen(deviceName: String?) {
        checkDisposed()
        require(deviceAnalytics == null) { "deviceAnalytics is not null" }
        rendererAnalytics.deviceChosen()
        deviceAnalytics = analytics.device(Version.skiko, hostOs, graphicsApi, deviceName)
        deviceAnalytics?.init()
    }

    /**
     * Should be called when initialization of graphic context is ended. Only call it after [onDeviceChosen]
     */
    protected fun onContextInit() {
        checkDisposed()
        requireNotNull(deviceAnalytics) { "deviceAnalytics is not null. Call onDeviceChosen after choosing the drawing device" }
        deviceAnalytics?.contextInit()
    }

    protected fun checkDisposed() {
        check(!isDisposed) { "${this.javaClass.simpleName} is disposed" }
    }

    abstract val renderInfo: String

    /**
     * Renders and presents exactly one frame at [scope]'s size. [immediate] selects the synchronous-redraw
     * variant.
     *
     * Throwing [org.jetbrains.skiko.RenderException] means the frame failed, and makes
     * [SkiaLayer.inDrawScope] fall back to the next render API.
     */
    abstract suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean)

    /**
     * Whether [renderBeforeShown] runs at all. `false` leaves the layer without a frame until it is shown.
     */
    open val presentsBeforeShown: Boolean get() = true

    /**
     * Renders and presents a frame when the layer is already displayable but not yet showing.
     * This is needed so we have a frame ready when the window is first shown, to prevent the window background
     * flashing.
     *
     * Returns `true` if this backend presented that frame itself; `false` presents it the ordinary way.
     */
    open fun renderBeforeShown(scope: LayerDrawScope): Boolean = false

    /**
     * Hands over the [FrameHost] this backend records its own frames through. Called once, before the first
     * frame.
     */
    open fun attachFrameHost(host: FrameHost) {}

    /**
     * Called on every frame request, including the ones the frame loop schedules no frame for.
     */
    open fun onFrameRequested(throttledToVsync: Boolean) {}

    /**
     * Wraps one frame of the loop. Place pacing before or after [frame], or skip it entirely to hold the loop
     * off while this backend presents on its own.
     */
    open suspend fun runFrame(frame: suspend () -> Unit) = frame()

    /**
     * `true` suppresses the loop's per-window frame dispatcher; the backend then drives every frame itself
     * through its [FrameHost].
     */
    // TODO: remove along with the cross-window batch, once one frame clock serves every window on a display.
    open val schedulesOwnFrames: Boolean get() = false

    open fun setVisible(isVisible: Boolean) {}

    open fun syncBounds() {}

    /**
     * Whether [SkiaLayer] presents a frame synchronously while it lays out, instead of scheduling one.
     */
    open val presentsOnLayout: Boolean get() = false

    /**
     * Whether the platform is driving the current resize. While `true` the loop leaves AWT resize events alone,
     * since the platform reports the size and presents the frames itself.
     */
    open val isHandlingLiveResizeNow: Boolean get() = false

    /**
     * Releases every native and Skia resource this backend owns. [isDisposed] is already `true` when it runs.
     * Called once, on the EDT.
     */
    protected abstract fun releaseResources()

    final override fun close() {
        if (isDisposed) return
        isDisposed = true
        releaseResources()
    }

    open fun isTransparentBackgroundSupported(): Boolean = defaultIsTransparentBackgroundSupported(layer)
}

/**
 * The frame loop, as seen by a backend that records frames on its own schedule.
 */
internal interface FrameHost {
    fun requestFrame(throttledToVsync: Boolean)

    fun updateIfRequested(nanoTime: Long = renderTime())

    fun renderImmediately()

    fun inFrame(body: (LayerDrawScope) -> Unit)

    fun inForcedSizeFrame(size: Dimension, body: (LayerDrawScope) -> Unit)
}