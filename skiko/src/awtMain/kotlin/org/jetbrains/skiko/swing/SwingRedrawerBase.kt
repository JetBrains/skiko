package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import java.awt.Graphics2D
import java.util.concurrent.CancellationException
import javax.swing.JComponent
import javax.swing.SwingUtilities

@OptIn(ExperimentalSkikoApi::class)
internal abstract class SwingRedrawerBase(
    private val component: JComponent,
    private val skikoView: SkikoView,
    private val analytics: SkiaLayerAnalytics,
    private val graphicsApi: GraphicsApi,
    private val clipComponents: MutableList<ClipRectangle>,
    private val renderExceptionHandler: (e: RenderException) -> Unit
) : SwingRedrawer {
    private var isFirstFrameRendered = false

    private val rendererAnalytics = analytics.renderer(Version.skiko, hostOs, graphicsApi)
    private var deviceAnalytics: DeviceAnalytics? = null
    protected var isDisposed = false
        private set

    protected abstract val contextHandler: SwingContextHandler

    init {
        rendererAnalytics.init()
    }

    final override fun dispose() {
        require(!isDisposed) { "$javaClass is disposed" }
        isDisposed = true
    }

    final override fun redraw(g: Graphics2D) {
        inDrawScope {
            contextHandler.draw(g)
        }
    }

    protected fun draw(canvas: org.jetbrains.skia.Canvas) {
        val scale = component.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        val width = (component.width * scale).toInt().coerceAtLeast(0)
        val height = (component.height * scale).toInt().coerceAtLeast(0)

        // clipping
        for (component in clipComponents) {
            canvas.clipRectBy(component, scale)
        }

        skikoView.onRender(canvas, width, height, System.nanoTime())
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
            } catch (e: RenderException) {
                if (!isDisposed) {
                    renderExceptionHandler(e)
                }
            }
            if (!isFirstFrameRendered && !isDisposed) {
                deviceAnalytics?.afterFirstFrameRender()
            }
            isFirstFrameRendered = true
        }
    }
}