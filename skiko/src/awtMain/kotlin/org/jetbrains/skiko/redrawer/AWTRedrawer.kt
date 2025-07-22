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
        checkDisposed()
        layer.update(nanoTime)
    }

    protected inline fun inDrawScope(body: () -> Unit) {
        requireNotNull(deviceAnalytics) { "deviceAnalytics is not null. Call onDeviceChosen after choosing the drawing device" }
        if (!isDisposed) {
            if (!isFirstFrameRendered) {
                deviceAnalytics?.beforeFirstFrameRender()
            }
            deviceAnalytics?.beforeFrameRender()
            layer.inDrawScope(body)
            if (!isFirstFrameRendered && !isDisposed) {
                deviceAnalytics?.afterFirstFrameRender()
            }
            deviceAnalytics?.afterFrameRender()
            isFirstFrameRendered = true
        }
    }

    protected fun checkDisposed() {
        check(!isDisposed) { "${this.javaClass.simpleName} is disposed" }
    }
}