package org.jetbrains.skiko.swing

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ClipMode
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.*
import java.awt.Graphics2D
import java.util.concurrent.CancellationException
import javax.accessibility.Accessible
import javax.swing.SwingUtilities.isEventDispatchThread

open class SkiaSwingLayer internal constructor(
    private val skikoView: SkikoView,
    private val properties: SkiaLayerProperties,
    private val analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
) : SkiaSwingLayerComponent() {
    internal companion object {
        init {
            Library.load()
        }
    }

    private var isInited = false
    private var isRendering = false

    private val contentScale: Float
        get() = graphicsConfiguration.defaultTransform.scaleX.toFloat()

    override val clipComponents = mutableListOf<ClipRectangle>()

    @Volatile
    private var isDisposed = false

    internal var redrawer: SwingRedrawer? = null
    private val fallbackRenderApiQueue = SkikoProperties.fallbackRenderApiQueue(properties.renderApi).toMutableList()
    private var _renderApi = fallbackRenderApiQueue[0]

    override val renderApi: GraphicsApi = _renderApi

    @Volatile
    private var picture: PictureHolder? = null
    private var pictureRecorder: PictureRecorder? = null
    private val pictureLock = Any()

    @Suppress("LeakingThis")
    private val fpsCounter = defaultFPSCounter(this)

    @Suppress("unused") // used in Compose Multiplatform
    constructor(
        skikoView: SkikoView,
        isVsyncEnabled: Boolean = SkikoProperties.vsyncEnabled,
        isVsyncFramelimitFallbackEnabled: Boolean = SkikoProperties.vsyncFramelimitFallbackEnabled,
        renderApi: GraphicsApi = SkikoProperties.renderApi,
        analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
    ) : this(
        skikoView,
        SkiaLayerProperties(
            isVsyncEnabled,
            isVsyncFramelimitFallbackEnabled,
            renderApi
        ),
        analytics,
    )

    init {
        isOpaque = false
        layout = null
    }

    override fun removeNotify() {
        Logger.debug { "SkiaSwingLayer.awt#removeNotify $this" }
        dispose()
        super.removeNotify()
    }

    override fun addNotify() {
        Logger.debug { "SkiaSwingLayer.awt#addNotify $this" }
        super.addNotify()
        init(isInited)
    }

    private fun findNextWorkingRenderApi() {
        var thrown: Boolean
        do {
            thrown = false
            try {
                _renderApi = fallbackRenderApiQueue.removeAt(0)
                redrawer?.dispose()
                redrawer = createDefaultSwingRedrawer(this@SkiaSwingLayer, analytics, properties)
            } catch (e: RenderException) {
                Logger.warn(e) { "Fallback to next API" }
                thrown = true
            }
        } while (thrown && fallbackRenderApiQueue.isNotEmpty())

        if (thrown && fallbackRenderApiQueue.isEmpty()) {
            throw RenderException("Cannot fallback to any render API")
        }
    }

    private fun init(recreation: Boolean = false) {
        isDisposed = false
        pictureRecorder = PictureRecorder()
        if (recreation) {
            fallbackRenderApiQueue.add(0, renderApi)
        }
        findNextWorkingRenderApi()
        isInited = true
    }

    override fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isInited && !isDisposed) {
            // we should dispose redrawer first (to cancel `draw` in rendering thread)
            redrawer?.dispose()
            redrawer = null
            picture?.instance?.close()
            picture = null
            pictureRecorder?.close()
            pictureRecorder = null
            isDisposed = true
        }
    }

    override fun paint(g: java.awt.Graphics) {
        redrawer?.redraw(g as Graphics2D)
    }

    internal fun update(nanoTime: Long) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }

        FrameWatcher.nextFrame()
        fpsCounter?.tick()

        val pictureWidth = (width * contentScale).toInt().coerceAtLeast(0)
        val pictureHeight = (height * contentScale).toInt().coerceAtLeast(0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val pictureRecorder = pictureRecorder!!
        val canvas = pictureRecorder.beginRecording(bounds)

        // clipping
        for (component in clipComponents) {
            canvas.clipRectBy(component)
        }

        try {
            isRendering = true
            skikoView.onRender(canvas, pictureWidth, pictureHeight, nanoTime)
        } finally {
            isRendering = false
        }

        // we can dispose layer during onRender
        // or even dispose it and pack it again
        if (!isDisposed && !pictureRecorder.isClosed) {
            synchronized(pictureLock) {
                picture?.instance?.close()
                val picture = pictureRecorder.finishRecordingAsPicture()
                this.picture = PictureHolder(picture, pictureWidth, pictureHeight)
            }
        }
    }

    internal inline fun inDrawScope(body: () -> Unit) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }
        try {
            body()
        } catch (e: CancellationException) {
            // ignore
        } catch (e: RenderException) {
            if (!isDisposed) {
                Logger.warn(e) { "Exception in draw scope" }
                findNextWorkingRenderApi()
                repaint()
            }
        }
    }

    internal fun draw(canvas: Canvas) {
        check(!isDisposed) { "SkiaLayer is disposed" }
        lockPicture {
            canvas.drawPicture(it.instance)
        }
    }

    private fun <T : Any> lockPicture(action: (PictureHolder) -> T): T? {
        return synchronized(pictureLock) {
            val picture = picture
            if (picture != null) {
                action(picture)
            } else {
                null
            }
        }
    }

    private fun Canvas.clipRectBy(rectangle: ClipRectangle) {
        val dpi = contentScale
        clipRect(
            Rect.makeLTRB(
                rectangle.x * dpi,
                rectangle.y * dpi,
                (rectangle.x + rectangle.width) * dpi,
                (rectangle.y + rectangle.height) * dpi
            ),
            ClipMode.DIFFERENCE,
            true
        )
    }

    override fun requestNativeFocusOnAccessible(accessible: Accessible?) {
        // TODO: support accessibility
    }
}
