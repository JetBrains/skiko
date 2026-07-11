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
    private val layer: SkiaLayer,
    private val analytics: SkiaLayerAnalytics,
    private val graphicsApi: GraphicsApi,
) : Redrawer {
    private var isFirstFrameRendered = false

    private val rendererAnalytics = analytics.renderer(Version.skiko, hostOs, graphicsApi)
    private var deviceAnalytics: DeviceAnalytics? = null
    protected var isDisposed = false
        private set

    init {
        rendererAnalytics.init()
    }

    override fun dispose() {
        require(!isDisposed) { "$javaClass is disposed" }
        isDisposed = true
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

    override fun update(nanoTime: Long) {
        update(nanoTime, forcedSize = null)
    }

    fun update(nanoTime: Long = renderTime(), forcedSize: Dimension?) {
        checkDisposed()
        layer.update(nanoTime, forcedSize = forcedSize)
    }

    protected inline fun inDrawScope(forcedSize: Dimension? = null, body: LayerDrawScope.() -> Unit) {
        requireNotNull(deviceAnalytics) { "deviceAnalytics is not null. Call onDeviceChosen after choosing the drawing device" }
        if (!isDisposed) {
            val isFirstFrame = !isFirstFrameRendered
            isFirstFrameRendered = true
            if (isFirstFrame) {
                deviceAnalytics?.beforeFirstFrameRender()
            }
            deviceAnalytics?.beforeFrameRender()
            layer.inDrawScope(forcedSize) {
                body()
            }
            if (isFirstFrame && !isDisposed) {
                deviceAnalytics?.afterFirstFrameRender()
            }
            deviceAnalytics?.afterFrameRender()
        }
    }

    protected fun checkDisposed() {
        check(!isDisposed) { "${this.javaClass.simpleName} is disposed" }
    }

    override fun onPlatformComponentResized() {
        syncBoundsFromPlatformComponent()
        if (supportsRenderingBeforeShown && !layer.isShowing && layer.isDisplayable && layer.width > 0 && layer.height > 0) {
            // Render eagerly so the window already has content when it first appears on screen; see
            // supportsRenderingWhileHidden. A scheduled frame would be skipped (rendering is gated on isShowing).
            renderBeforeShown()
        } else {
            needRender(throttledToVsync = false)
        }
    }

    /**
     * Whether this redrawer can render and present a frame while the layer is displayable but not yet
     * showing (see [renderBeforeShown]). When `true`, [onPlatformComponentResized] renders eagerly so the
     * window already has content on its first on-screen frame, avoiding a flash of the window background.
     */
    protected open val supportsRenderingBeforeShown: Boolean get() = false

    /**
     * Renders and presents a frame while the layer is displayable but not showing.
     *
     * Only called when [supportsRenderingBeforeShown] is `true`. The default is a plain [renderImmediately];
     * implementations whose regular present is asynchronous should override this to present synchronously,
     * guaranteeing the content is delivered before the window can appear on screen (an async present could
     * land only after the window is already showing, defeating the purpose of the early render).
     */
    protected open fun renderBeforeShown() {
        renderImmediately()
    }

    override fun isTransparentBackgroundSupported() = defaultIsTransparentBackgroundSupported(layer)
}