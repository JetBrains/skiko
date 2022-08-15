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

    init {
        rendererAnalytics.init()
    }

    /**
     * Should be called when the device name is known as early, as possible.
     */
    protected fun onDeviceChosen(deviceName: String?) {
        rendererAnalytics.deviceChosen()
        deviceAnalytics = analytics.device(Version.skiko, hostOs, graphicsApi, deviceName)
        deviceAnalytics?.init()
    }

    /**
     * Should be called when initialization of graphic context is ended. Only call it after [onDeviceChosen]
     */
    protected fun onContextInit() {
        deviceAnalytics?.contextInit()
    }

    protected fun update(nanoTime: Long) = layer.update(nanoTime)

    protected inline fun inDrawScope(body: () -> Unit) {
        if (!isFirstFrameRendered) {
            deviceAnalytics?.beforeFirstFrameRender()
        }
        layer.inDrawScope(body)
        if (!isFirstFrameRendered) {
            deviceAnalytics?.afterFirstFrameRender()
        }
        isFirstFrameRendered = true
    }
}