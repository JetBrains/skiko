package org.jetbrains.skiko.renderer

import org.jetbrains.skiko.*
import org.jetbrains.skiko.LockFile.Companion.skiko
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import java.awt.Dimension

/**
 * Common class for all AWT renderers.
 * Don't forget to call [onDeviceChosen] and [onContextInit] to send necessary analytics.
 */
@OptIn(ExperimentalSkikoApi::class)
internal abstract class AwtRenderer(
    layer: SkiaLayer,
    private val analytics: SkiaLayerAnalytics,
    private val graphicsApi: GraphicsApi,
) : Renderer(layer) {
    private var isFirstFrameRendered = false

    private val rendererAnalytics = analytics.renderer(Version.skiko, hostOs, graphicsApi)
    private var deviceAnalytics: DeviceAnalytics? = null
    protected var isDisposed = false
        private set

    init {
        rendererAnalytics.init()
    }

    override fun dispose() {
        checkDisposed()
        isDisposed = true
        super.dispose()
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

    /**
     * Should be called when the Skia context has been created.
     */
    protected fun onContextInitialized() {
        if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
            Logger.info { "Renderer info:\n $renderInfo" }
        }
        context?.run {
            val gpuResourceCacheLimit = layer.properties.gpuResourceCacheLimit
            if (gpuResourceCacheLimit >= 0) {
                resourceCacheLimit = gpuResourceCacheLimit
            }
        }
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

    override fun onLayerComponentResized() {
        syncBoundsFromPlatformComponent()

        if (!layer.isShowing && layer.isDisplayable && (layer.width > 0) && (layer.height > 0)) {
            renderBeforeShown()
            return
        }

        needRender(throttledToVsync = false)
    }

    /**
     * Renders and presents a frame when the layer is already displayable but not yet showing.
     * This is needed so we have a frame ready when the window is first shown, to prevent the window background
     * flashing.
     */
    protected open fun renderBeforeShown(): Boolean {
        renderImmediately()
        return true
    }
}
