package org.jetbrains.skiko.swing

import com.jetbrains.JBR
import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import java.awt.Graphics2D
import java.util.concurrent.CancellationException
import javax.swing.SwingUtilities

/**
 * Provides a base implementation of drawing [SkikoRenderDelegate] content on [java.awt.Graphics2D]
 *
 * Each [redraw] request is handled in a following way:
 *   1. For the first request initialize native GPU context using [createDirectContext]
 *   2. Create [org.jetbrains.skia.Canvas] where content should be drawn using [initCanvas]
 *   3. Acquire drawing "commands" using [SkikoRenderDelegate]
 *   4. Flush these commands on [java.awt.Graphics2D] using [flush]
 *
 * All the steps are performed synchronously on EDT.
 */
@OptIn(ExperimentalSkikoApi::class)
internal abstract class SwingRedrawerBase(
    private val swingLayerProperties: SwingLayerProperties,
    private val analytics: SkiaLayerAnalytics,
    private val graphicsApi: GraphicsApi
) : SwingRedrawer {
    private var isFirstFrameRendered = false

    private val rendererAnalytics = analytics.renderer(Version.skiko, hostOs, graphicsApi)
    private var deviceAnalytics: DeviceAnalytics? = null
    private var isDisposed = false
    private val swingDrawer = createSwingDrawer()

    init {
        rendererAnalytics.init()
    }

    protected abstract fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long)

    override fun dispose() {
        require(!isDisposed) { "$javaClass is disposed" }
        isDisposed = true
        swingDrawer.dispose()
    }

    final override fun redraw(g: Graphics2D) {
        require(!isDisposed) { "$javaClass is disposed" }

        inDrawScope {
            val scale = swingLayerProperties.scale
            val width = (swingLayerProperties.width * scale).toInt().coerceAtLeast(0)
            val height = (swingLayerProperties.height * scale).toInt().coerceAtLeast(0)
            onRender(g, width, height, System.nanoTime())
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

    protected fun onContextInit() {
        require(!isDisposed) { "$javaClass is disposed" }
        requireNotNull(deviceAnalytics) { "deviceAnalytics is not null. Call onDeviceChosen after choosing the drawing device" }
        if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
            Logger.info { "Renderer info:\n ${rendererInfo()}" }
        }
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

    protected fun getSwingDrawer(): SwingDrawer = swingDrawer

    private fun createSwingDrawer(): SwingDrawer {
        if (JBR.isNativeRasterLoaderSupported()) {
            // TODO: check if not OpenGL
            // TODO: report a bug
            return VolatileImageSwingDrawer()
        }

        return SoftwareSwingDrawer(swingLayerProperties)
    }
}