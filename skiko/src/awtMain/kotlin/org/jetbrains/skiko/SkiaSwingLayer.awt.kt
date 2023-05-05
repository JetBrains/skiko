package org.jetbrains.skiko

import kotlinx.coroutines.Job
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.MetalOffScreenRedrawer
import org.jetbrains.skiko.redrawer.Redrawer
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.util.concurrent.CancellationException
import javax.accessibility.Accessible
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities.isEventDispatchThread
import javax.swing.UIManager

open class SkiaSwingLayer internal constructor(
    // TODO: support accessibility
    externalAccessibleFactory: ((Component) -> Accessible)? = null,
    private val properties: SkiaLayerProperties,
    private val renderFactory: RenderFactory = RenderFactory.Default,
    private val analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
    val pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
) : JPanel() {

    internal companion object {
        init {
            Library.load()
        }
    }

    enum class PropertyKind {
        Renderer,
        ContentScale,
    }

    private var _transparency: Boolean = false
    var transparency: Boolean
        get() = _transparency
        set(value) {
            _transparency = value
            if (!value) {
                background = UIManager.getColor("Panel.background")
            } else {
                background = Color(0, 0, 0, 0)
            }
        }

    constructor(
        externalAccessibleFactory: ((Component) -> Accessible)? = null,
        isVsyncEnabled: Boolean = SkikoProperties.vsyncEnabled,
        isVsyncFramelimitFallbackEnabled: Boolean = SkikoProperties.vsyncFramelimitFallbackEnabled,
        renderApi: GraphicsApi = SkikoProperties.renderApi,
        analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
        pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
    ) : this(
        externalAccessibleFactory,
        SkiaLayerProperties(
            isVsyncEnabled,
            isVsyncFramelimitFallbackEnabled,
            renderApi
        ),
        RenderFactory.Default,
        analytics,
        pixelGeometry
    )

    private var peerBufferSizeFixJob: Job? = null
    private var latestReceivedGraphicsContextScaleTransform: AffineTransform? = null

    init {
        isOpaque = false
        layout = null

        addPropertyChangeListener("graphicsContextScaleTransform") {
            Logger.debug { "graphicsContextScaleTransform changed for $this" }
            latestReceivedGraphicsContextScaleTransform = it.newValue as AffineTransform
            // TODO: should we sync size??
//            redrawer?.syncSize()
            notifyChange(PropertyKind.ContentScale)
        }
    }

    override fun removeNotify() {
        Logger.debug { "SkiaLayer.awt#removeNotify $this" }
        dispose()
        super.removeNotify()
    }

    override fun addNotify() {
        Logger.debug { "SkiaLayer.awt#addNotify $this" }
        super.addNotify()
        checkShowing()
        init(isInited)
    }

    fun detach() {
        dispose()
    }

    private var isInited = false
    private var isRendering = false

    private fun checkShowing() {
        val wasShowing = isShowingCached
        isShowingCached = super.isShowing()
        if (wasShowing != isShowing) {
            // TODO: should we change is visible size??
//            redrawer?.setVisible(isShowing)
        }
        if (isShowing) {
            // TODO: should we sync size??
//            redrawer?.syncSize()
            repaint()
        }
    }

    private var isShowingCached = false

    override fun isShowing(): Boolean {
        return isShowingCached
    }

    val contentScale: Float
        get() = graphicsConfiguration.defaultTransform.scaleX.toFloat()

    val component: Any
        get() = this

    var skikoView: SkikoView? = null

    fun attachTo(container: Any) {
        attachTo(container as JComponent)
    }

    fun attachTo(jComponent: JComponent) {
        jComponent.add(this)
    }

    private var keyEvent: KeyEvent? = null

    fun addView(view: SkikoView) {
        skikoView = view
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }

            override fun mouseReleased(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }

            override fun mouseEntered(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }

            override fun mouseExited(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }

            override fun mouseMoved(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }
        })

        addMouseWheelListener(object : MouseWheelListener {
            override fun mouseWheelMoved(e: MouseWheelEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }
        })

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                keyEvent = e
                skikoView?.onKeyboardEvent(toSkikoEvent(e))
            }

            override fun keyReleased(e: KeyEvent) {
                keyEvent = e
                skikoView?.onKeyboardEvent(toSkikoEvent(e))
            }

            override fun keyTyped(e: KeyEvent) {
                skikoView?.onInputEvent(toSkikoTypeEvent(e, keyEvent))
            }
        })

        addInputMethodListener(object : InputMethodListener {
            override fun caretPositionChanged(e: InputMethodEvent) {
                skikoView?.onInputEvent(toSkikoTypeEvent(e, keyEvent))
            }

            override fun inputMethodTextChanged(e: InputMethodEvent) {
                skikoView?.onInputEvent(toSkikoTypeEvent(e, keyEvent))
            }
        })
    }

    val clipComponents = mutableListOf<ClipRectangle>()

    @Volatile
    private var isDisposed = false
    internal var redrawer: MetalOffScreenRedrawer? = null
    private val fallbackRenderApiQueue = SkikoProperties.fallbackRenderApiQueue(properties.renderApi).toMutableList()
    private var renderApi_ = fallbackRenderApiQueue[0]
    var renderApi: GraphicsApi
        get() = renderApi_
        private set(value) {
            this.renderApi_ = value
            notifyChange(PropertyKind.Renderer)
        }
    val renderInfo: String
        get() = if (redrawer == null)
            "SkiaLayer isn't initialized yet"
        else
            redrawer!!.renderInfo

    @Volatile
    private var picture: PictureHolder? = null
    private var pictureRecorder: PictureRecorder? = null
    private val pictureLock = Any()

    private fun findNextWorkingRenderApi() {
        var thrown: Boolean
        do {
            thrown = false
            try {
                renderApi = fallbackRenderApiQueue.removeAt(0)
                redrawer?.dispose()
                redrawer = MetalOffScreenRedrawer(this@SkiaSwingLayer, analytics, properties)
                // TODO: should we sync size??
//                redrawer?.syncSize()
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

    private val stateHandlers =
        mutableMapOf<PropertyKind, MutableList<(SkiaSwingLayer) -> Unit>>()

    fun onStateChanged(kind: PropertyKind, handler: (SkiaSwingLayer) -> Unit) {
        stateHandlers.getOrPut(kind, { mutableListOf() }) += handler
    }

    private fun notifyChange(kind: PropertyKind) {
        stateHandlers.get(kind)?.let { handlers ->
            handlers.forEach { it(this) }
        }
    }

    open fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isInited && !isDisposed) {
            // we should dispose redrawer first (to cancel `draw` in rendering thread)
            redrawer?.dispose()
            redrawer = null
            picture?.instance?.close()
            picture = null
            pictureRecorder?.close()
            pictureRecorder = null
            peerBufferSizeFixJob?.cancel()
            isDisposed = true
        }
    }


    override fun paint(g: java.awt.Graphics) {
        Logger.debug { "Paint called on: $this" }
        checkContentScale()

        // `paint` can be called when we already inside `draw` method.
        //
        // For example if we call some AWT function inside renderer.onRender,
        // such as `jframe.isEnabled = false` on Linux
        //
        // To avoid recursive call of `draw` (we don't support recursive calls) we just schedule redrawing.
        if (g is Graphics2D) {
            redrawer?.redraw(g)
        }
    }

    // Workaround for JBR-5274 and JBR-5305
    fun checkContentScale() {
        val currentGraphicsContextScaleTransform = graphicsConfiguration.defaultTransform
        if (currentGraphicsContextScaleTransform != latestReceivedGraphicsContextScaleTransform) {
            firePropertyChange(
                "graphicsContextScaleTransform",
                latestReceivedGraphicsContextScaleTransform,
                currentGraphicsContextScaleTransform
            )
        }
    }

    /**
     * Redraw on the next animation Frame (on vsync signal if vsync is enabled).
     */
    fun needRedraw(g: Graphics2D) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }
        redrawer?.redraw(g)
    }

    @Suppress("LeakingThis")
    private val fpsCounter = defaultFPSCounter(this)

    internal fun update(nanoTime: Long) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }

        checkContentScale()

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
            skikoView?.onRender(canvas, pictureWidth, pictureHeight, nanoTime)
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
            // TODO: what to do on exception?
//            if (!isDisposed) {
//                Logger.warn(e) { "Exception in draw scope" }
//                findNextWorkingRenderApi()
//                redrawer?.redraw()
//            }
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

    // Captures current layer as bitmap.
    fun screenshot(): Bitmap? {
        check(!isDisposed) { "SkiaLayer is disposed" }
        return lockPicture { picture ->
            val store = Bitmap()
            val ci = ColorInfo(
                ColorType.BGRA_8888, ColorAlphaType.OPAQUE, ColorSpace.sRGB
            )
            store.setImageInfo(ImageInfo(ci, picture.width, picture.height))
            store.allocN32Pixels(picture.width, picture.height)
            val canvas = Canvas(store)
            canvas.drawPicture(picture.instance)
            store.setImmutable()
            store
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

    private fun roundSize(value: Int): Int {
        var rounded = value * contentScale
        val diff = rounded - rounded.toInt()
        // We check values close to 0.5 and edit the size to avoid white lines glitch
        if (diff > 0.4f && diff < 0.6f) {
            rounded = value + 1f
        } else {
            rounded = value.toFloat()
        }
        return rounded.toInt()
    }
// TODO: support accessibility
//    fun requestNativeFocusOnAccessible(accessible: Accessible?) {
//        backedLayer.requestNativeFocusOnAccessible(accessible)
//    }
}
