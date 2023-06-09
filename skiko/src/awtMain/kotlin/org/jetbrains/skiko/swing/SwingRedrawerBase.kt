package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import java.awt.Graphics2D
import java.util.concurrent.CancellationException
import javax.swing.SwingUtilities

@OptIn(ExperimentalSkikoApi::class)
internal abstract class SwingRedrawerBase(
    private val swingLayerProperties: SwingLayerProperties,
    private val skikoView: SkikoView,
    private val analytics: SkiaLayerAnalytics,
    private val graphicsApi: GraphicsApi
) : SwingRedrawer {
    private var isFirstFrameRendered = false

    private val rendererAnalytics = analytics.renderer(Version.skiko, hostOs, graphicsApi)
    private var deviceAnalytics: DeviceAnalytics? = null
    private var isDisposed = false

    private var context: DirectContext? = null

    init {
        rendererAnalytics.init()
    }

    protected abstract fun createDirectContext(): DirectContext?

    protected abstract fun initCanvas(context: DirectContext?): DrawingSurfaceData

    protected abstract fun flush(drawingSurfaceData: DrawingSurfaceData, g: Graphics2D)

    final override fun dispose() {
        require(!isDisposed) { "$javaClass is disposed" }
        context?.close()
        isDisposed = true
    }

    final override fun redraw(g: Graphics2D) {
        require(!isDisposed) { "$javaClass is disposed" }

        inDrawScope {
            if (!initDirectContext()) {
                throw RenderException("Cannot init graphic context")
            }
            val drawingSurfaceData = initCanvas(context)

            val scale = swingLayerProperties.scale
            val width = (swingLayerProperties.width * scale).toInt().coerceAtLeast(0)
            val height = (swingLayerProperties.height * scale).toInt().coerceAtLeast(0)

            drawingSurfaceData.canvas?.apply {
                clear(Color.TRANSPARENT)
                skikoView.onRender(this, width, height, System.nanoTime())
            }

            flush(drawingSurfaceData, g)
            drawingSurfaceData.surface?.close()
            drawingSurfaceData.renderTarget?.close()
        }
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

    protected open fun rendererInfo(): String {
        return "GraphicsApi: ${graphicsApi}\n" +
                "OS: ${hostOs.id} ${hostArch.id}\n"
    }

    private fun initDirectContext(): Boolean {
        try {
            if (context == null) {
                context = createDirectContext()
                onContextInit()
                if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                    Logger.info { "Renderer info:\n ${rendererInfo()}" }
                }
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to create Skia Metal context!" }
            return false
        }
        return true
    }

    private fun onContextInit() {
        require(!isDisposed) { "$javaClass is disposed" }
        requireNotNull(deviceAnalytics) { "deviceAnalytics is not null. Call onDeviceChosen after choosing the drawing device" }
        deviceAnalytics?.contextInit()
    }

    private inline fun inDrawScope(body: () -> Unit) {
        check(SwingUtilities.isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        requireNotNull(deviceAnalytics) { "deviceAnalytics is not null. Call onDeviceChosen after choosing the drawing device" }
        if (!isDisposed) {
            if (!isFirstFrameRendered) {
                deviceAnalytics?.beforeFirstFrameRender()
            }
            try {
                body()
            } catch (e: CancellationException) {
                // ignore
            }
            if (!isFirstFrameRendered && !isDisposed) {
                deviceAnalytics?.afterFirstFrameRender()
            }
            isFirstFrameRendered = true
        }
    }

    protected class DrawingSurfaceData(
        val renderTarget: BackendRenderTarget?,
        val surface: Surface?,
        val canvas: Canvas?
    )
}