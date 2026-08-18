package org.jetbrains.skiko.renderer

import org.jetbrains.skiko.*
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics

/**
 * Implementations must call [onDeviceChosen] and then [onContextInit] during initialization;
 * skipping them silently disables device analytics.
 */
@OptIn(ExperimentalSkikoApi::class)
internal abstract class AwtRenderer(
    internal val layer: SkiaLayer,
    private val analytics: SkiaLayerAnalytics,
    private val graphicsApi: GraphicsApi,
) : AutoCloseable {
    private val rendererAnalytics = analytics.renderer(Version.skiko, hostOs, graphicsApi)

    var deviceAnalytics: DeviceAnalytics? = null
        private set

    @Volatile
    internal var isDisposed = false
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
     * How this backend produces the frame that must be ready before the window is first shown, so
     * the window background does not flash. The driver asks before recording anything: [NONE] skips
     * that frame and its analytics entirely, [RENDER] presents it through [renderFrame], [BACKEND]
     * calls [renderBeforeShown] so the backend presents it its own way.
     */
    enum class BeforeShownFrame { NONE, RENDER, BACKEND }

    open val beforeShownFrame: BeforeShownFrame get() = BeforeShownFrame.RENDER

    /**
     * Renders and presents the before-shown frame. Only called when [beforeShownFrame] is
     * [BeforeShownFrame.BACKEND].
     */
    open fun renderBeforeShown(scope: LayerDrawScope) {
        error("$this does not present the before-shown frame itself")
    }

    /**
     * The driver's listener for platform live-resize events; `null` until [attachFrameEvents] runs.
     * Native resize callbacks are JNI-bound to this class, so the backend receives them and forwards
     * each one.
     * Written on the EDT, read from platform threads.
     */
    @Volatile
    protected var frameEvents: LiveResizeListener? = null
        private set

    internal fun attachFrameEvents(events: LiveResizeListener) {
        frameEvents = events
    }

    /**
     * Requests one frame from the platform's own scheduler while it is driving a live resize;
     * the platform coalesces.
     */
    open fun requestPlatformDrivenFrame() {}

    /**
     * Renders one frame the platform asked for during a live resize, at the size the driver has
     * already recorded into [scope]. [isResizeFrame] is `false` for frames of the drag that do not
     * change the size.
     */
    open fun renderPlatformDrivenFrame(scope: LayerDrawScope, isResizeFrame: Boolean) {
        error("$this does not render platform-driven frames")
    }

    /**
     * Wraps one frame of the loop. Place pacing before or after [frame], or skip it entirely to hold the loop
     * off while this backend presents on its own.
     */
    open suspend fun runFrame(frame: suspend () -> Unit) = frame()

    /**
     * Whether [runFrame] blocks on vsync after the frame body. The driver then records the next
     * frame's content early, while the previous frame is still waiting.
     */
    open val pacesAfterFrame: Boolean get() = false

    open fun setVisible(isVisible: Boolean) {}

    open fun syncBoundsFromPlatformComponent() {}

    /**
     * Whether this backend presents synchronously at resize moments -- when [SkiaLayer] lays out,
     * and when a live resize ends, before the platform's resize loop returns -- instead of
     * scheduling a frame. The driver suppresses the layout present while the platform is driving
     * a live resize.
     */
    open val presentsOnResize: Boolean get() = false

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
 * Live-resize notifications a backend forwards to its driver. The backend owns the platform thread
 * choreography around each call; the driver owns what the event means for frame scheduling.
 */
internal interface LiveResizeListener {
    fun onLiveResizeStarted()

    /** Records and renders one frame at the given size, synchronously on the calling thread. */
    fun onLiveResizeFrame(width: Int, height: Int, isResizeFrame: Boolean)

    fun onLiveResizeEnded()
}

