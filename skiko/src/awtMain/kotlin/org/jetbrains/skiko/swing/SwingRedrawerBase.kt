package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import java.awt.Canvas
import java.util.concurrent.CancellationException
import javax.swing.JComponent
import javax.swing.SwingUtilities

@OptIn(ExperimentalSkikoApi::class)
internal abstract class SwingRedrawerBase(
    private val component: JComponent,
    private val skikoView: SkikoView,
    private val analytics: SkiaLayerAnalytics,
    private val graphicsApi: GraphicsApi,
    clipComponents: MutableList<ClipRectangle>,
    private val renderExceptionHandler: (e: RenderException) -> Unit
) : SwingRedrawer {
    private val fpsCounter = defaultFPSCounter(component)
    private val skiaDrawingManager = SkiaDrawingManager(fpsCounter, clipComponents).also {
        // TODO: should we init it later? not on creation
        it.init()
    }

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
        skiaDrawingManager.dispose()
        isDisposed = true
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
        val contentScale = component.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        skiaDrawingManager.update(nanoTime, component.width, component.height, contentScale, skikoView)
    }

    protected inline fun inDrawScope(body: () -> Unit) {
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

    protected fun draw(canvas: org.jetbrains.skia.Canvas) {
        skiaDrawingManager.draw(canvas)
    }
}