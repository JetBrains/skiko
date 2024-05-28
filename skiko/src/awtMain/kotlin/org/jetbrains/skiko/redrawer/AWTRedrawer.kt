package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.*
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics

/**
 * Common class for all AWT redrawers.
 * Don't forget to call [onDeviceChosen] and [onContextInit] to send necessary analytics.
 */
@OptIn(ExperimentalSkikoApi::class)
internal abstract class AWTRedrawer(
    private val layer: SkiaLayer,
    private val analytics: SkiaLayerAnalytics,
    private val graphicsApi: GraphicsApi,
) : Redrawer {
    private var isFirstFrameRendered = false

    private val rendererAnalytics = analytics.renderer(Version.skiko, hostOs, graphicsApi)
    private var deviceAnalytics: DeviceAnalytics? = null
    protected var isDisposed = false
        private set
    private var isRendering = false

    init {
        rendererAnalytics.init()
    }

    override fun dispose() {
        require(!isDisposed) { "$javaClass is disposed" }
        isDisposed = true
    }

    internal fun tryRedrawImmediately() {
        if (!layer.isShowing) return

        // It might be called inside `renderDelegate`,
        // so to avoid recursive call (not supported) just schedule redrawing.
        //
        // For example if we call some AWT function inside renderer.onRender,
        // such as `jframe.isEnabled = false` on Linux
        if (isRendering) {
            needRedraw()
        } else {
            redrawImmediately()
        }
    }

    open fun onChangeBounds() {
        syncSize()

        // To avoid visual artifacts, redrawing should be performed immediately, without scheduling to "later".
        tryRedrawImmediately()
    }

    /**
     * Should be called when the device name is known as early, as possible.
     */
    protected fun onDeviceChosen(deviceName: String?) {
        require(!isDisposed) { "$javaClass is disposed" }
        require(deviceAnalytics == null) { "deviceAnalytics is not null" }
        rendererAnalytics.deviceChosen()
        deviceAnalytics = analytics.device(Version.skiko, hostOs, graphicsApi, deviceName)
        deviceAnalytics?.init()
    }

    /**
     * Should be called when initialization of graphic context is ended. Only call it after [onDeviceChosen]
     */
    protected fun onContextInit() {
        require(!isDisposed) { "$javaClass is disposed" }
        requireNotNull(deviceAnalytics) { "deviceAnalytics is not null. Call onDeviceChosen after choosing the drawing device" }
        deviceAnalytics?.contextInit()
    }

    protected fun update(nanoTime: Long) {
        require(!isDisposed) { "$javaClass is disposed" }
        try {
            isRendering = true
            layer.update(nanoTime)
        } finally {
            isRendering = false
        }
    }

    protected inline fun inDrawScope(body: () -> Unit) {
        requireNotNull(deviceAnalytics) { "deviceAnalytics is not null. Call onDeviceChosen after choosing the drawing device" }
        if (!isDisposed) {
            if (!isFirstFrameRendered) {
                deviceAnalytics?.beforeFirstFrameRender()
            }
            layer.inDrawScope(body)
            if (!isFirstFrameRendered && !isDisposed) {
                deviceAnalytics?.afterFirstFrameRender()
            }
            isFirstFrameRendered = true
        }
    }
}