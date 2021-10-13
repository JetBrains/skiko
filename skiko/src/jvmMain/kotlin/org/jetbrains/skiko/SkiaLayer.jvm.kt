package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ClipMode
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Picture
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.redrawer.Redrawer
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.event.*
import java.awt.im.InputMethodRequests
import java.util.concurrent.CancellationException
import javax.accessibility.Accessible
import javax.swing.JPanel
import javax.swing.SwingUtilities.isEventDispatchThread

actual open class SkiaLayer internal constructor(
    externalAccessibleFactory: ((Component) -> Accessible)? = null,
    private val properties: SkiaLayerProperties = makeDefaultSkiaLayerProperties(),
    private val renderFactory: RenderFactory
) : JPanel() {

    companion object {
        init {
            Library.load()
        }
    }

    enum class PropertyKind {
        Renderer,
        ContentScale,
    }

    actual var transparency: Boolean = false

    internal val backedLayer: HardwareLayer

    actual constructor(
        properties: SkiaLayerProperties
    ) : this(null, properties, RenderFactory.Default)


    constructor(
        properties: SkiaLayerProperties = makeDefaultSkiaLayerProperties(),
        externalAccessibleFactory: ((Component) -> Accessible)? = null
    ) : this(externalAccessibleFactory, properties, RenderFactory.Default)

    val canvas: java.awt.Canvas
        get() = backedLayer

    init {
        isOpaque = false
        background = Color(0, 0, 0, 0)
        layout = null
        backedLayer = object : HardwareLayer(externalAccessibleFactory) {
            override fun paint(g: Graphics) {
                // 1. JPanel.paint is not always called (in rare cases).
                //    For example if we call 'jframe.isResizable = false` on Ubuntu
                //
                // 2. HardwareLayer.paint is also not always called.
                //    For example, on macOs when we resize window or change DPI
                //
                // 3. to avoid double paint in one single frame, use needRedraw instead of redrawImmediately
                redrawer?.needRedraw()
            }

            override fun getInputMethodRequests(): InputMethodRequests? {
                return this@SkiaLayer.inputMethodRequests
            }
        }
        @Suppress("LeakingThis")
        add(backedLayer)
        backedLayer.addHierarchyListener {
            if (it.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
                checkShowing()
            }
            if (it.changeFlags and HierarchyEvent.DISPLAYABILITY_CHANGED.toLong() != 0L) {
                checkInit()
            }
        }
    }

    private var isInited = false
    private var isRendering = false

    private fun checkInit() {
        if (!isInited && isDisplayable) {
            backedLayer.defineContentScale()
            checkShowing()
            init()
        }
    }

    private fun checkShowing() {
        isShowingCached = super.isShowing()
        if (isShowing) {
            repaint()
        }
    }

    private var isShowingCached = false

    override fun isShowing(): Boolean {
        return isShowingCached
    }

    actual val contentScale: Float
        get() = backedLayer.contentScale

    val contentHandle: Long
        get() = backedLayer.contentHandle

    val windowHandle: Long
        get() = backedLayer.windowHandle

    actual var fullscreen: Boolean
        get() = backedLayer.fullscreen
        set(value) {
            backedLayer.fullscreen = value
        }

    var renderer: SkiaRenderer? = null
    val clipComponents = mutableListOf<ClipRectangle>()

    @Volatile
    private var isDisposed = false
    internal var redrawer: Redrawer? = null
    private var contextHandler: ContextHandler? = null
    private val fallbackRenderApiQueue = SkikoProperties.fallbackRenderApiQueue.toMutableList()
    private var renderApi_ = fallbackRenderApiQueue[0]
    actual var renderApi: GraphicsApi
        get() = renderApi_
        private set(value) {
            this.renderApi_ = value
            notifyChange(PropertyKind.Renderer)
        }
    val renderInfo: String
        get() = if (contextHandler?.context == null)
            "ContextHandler hasn't been initialized yet."
        else
            contextHandler!!.rendererInfo()

    @Volatile
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()
    private val pictureLock = Any()

    private fun findNextWorkingRenderApi() {
        var thrown: Boolean
        do {
            thrown = false
            try {
                renderApi = fallbackRenderApiQueue.removeAt(0)
                contextHandler?.dispose()
                redrawer?.dispose()
                contextHandler = renderFactory.createContextHandler(this, renderApi)
                redrawer = renderFactory.createRedrawer(this, renderApi, properties)
                redrawer?.syncSize()
            } catch (e: RenderException) {
                println(e.message)
                thrown = true
            }
        } while (thrown && fallbackRenderApiQueue.isNotEmpty())

        if (thrown && fallbackRenderApiQueue.isEmpty()) {
            throw RenderException("Cannot fallback to any render API")
        }
    }

    protected open fun init() {
        backedLayer.init()
        findNextWorkingRenderApi()
        isInited = true
    }

    private val stateHandlers =
        mutableMapOf<PropertyKind, MutableList<(SkiaLayer) -> Unit>>()

    fun onStateChanged(kind: PropertyKind, handler: (SkiaLayer) -> Unit) {
        stateHandlers.getOrPut(kind, { mutableListOf() }) += handler
    }

    private fun notifyChange(kind: PropertyKind) {
        stateHandlers.get(kind)?.let { handlers ->
            handlers.forEach { it(this) }
        }
    }

    open fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }

        if (isInited) {
            redrawer?.dispose()  // we should dispose redrawer first (to cancel `draw` in rendering thread)
            contextHandler?.dispose()
            picture?.instance?.close()
            pictureRecorder.close()
            backedLayer.dispose()
            isDisposed = true
        }
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        var roundedWidth = width
        var roundedHeight = height
        if (isInited) {
            roundedWidth = roundSize(width)
            roundedHeight = roundSize(height)
        }
        super.setBounds(x, y, roundedWidth, roundedHeight)
        backedLayer.setSize(roundedWidth, roundedHeight)
        redrawer?.syncSize()
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (backedLayer.checkContentScale()) {
            notifyChange(PropertyKind.ContentScale)
        }
        redrawer?.syncSize() // setBounds not always called (for example when we change density on Linux

        // `paint` can be called when we already inside `draw` method.
        //
        // For example if we call some AWT function inside renderer.onRender,
        // such as `jframe.isEnabled = false` on Linux
        //
        // To avoid recursive call of `draw` (we don't support recursive calls) we just schedule redrawing.
        if (isRendering) {
            redrawer?.needRedraw()
        } else {
            redrawer?.redrawImmediately()
        }
    }

    // We need to delegate all event listeners to the Canvas (so and focus/input)
    // Canvas is heavyweight AWT component, JPanel is lightweight Swing component
    // Event handling doesn't properly work when we mix heavyweight and lightweight components.
    // For example, Canvas will eat all mouse events
    // (see "mouse events on a heavyweight component do not fall through to its parent",
    // https://www.comp.nus.edu.sg/~cs3283/ftp/Java/swingConnect/archive/tech_topics_arch/mixing/mixing.html)

    override fun enableInputMethods(enable: Boolean) {
        backedLayer.enableInputMethods(enable)
    }

    override fun getInputMethodListeners(): Array<InputMethodListener> {
        return backedLayer.getInputMethodListeners()
    }

    override fun processInputMethodEvent(e: InputMethodEvent?) {
        backedLayer.doProcessInputMethodEvent(e)
    }

    override fun requestFocus() {
        backedLayer.requestFocus()
    }

    override fun requestFocus(cause: FocusEvent.Cause?) {
        backedLayer.requestFocus(cause)
    }

    override fun addInputMethodListener(l: InputMethodListener) {
        super.addInputMethodListener(l)
        backedLayer.addInputMethodListener(l)
    }

    override fun addMouseListener(l: MouseListener) {
        backedLayer.addMouseListener(l)
    }

    override fun addMouseMotionListener(l: MouseMotionListener) {
        backedLayer.addMouseMotionListener(l)
    }

    override fun addMouseWheelListener(l: MouseWheelListener) {
        backedLayer.addMouseWheelListener(l)
    }

    override fun addKeyListener(l: KeyListener) {
        backedLayer.addKeyListener(l)
    }

    override fun removeInputMethodListener(l: InputMethodListener) {
        super.removeInputMethodListener(l)
        backedLayer.removeInputMethodListener(l)
    }

    override fun removeMouseListener(l: MouseListener) {
        backedLayer.removeMouseListener(l)
    }

    override fun removeMouseMotionListener(l: MouseMotionListener) {
        backedLayer.removeMouseMotionListener(l)
    }

    override fun removeMouseWheelListener(l: MouseWheelListener) {
        backedLayer.removeMouseWheelListener(l)
    }

    override fun removeKeyListener(l: KeyListener) {
        backedLayer.removeKeyListener(l)
    }

    override fun setFocusTraversalKeysEnabled(focusTraversalKeysEnabled: Boolean) {
        backedLayer.focusTraversalKeysEnabled = focusTraversalKeysEnabled
    }

    /**
     * Redraw on the next animation Frame (on vsync signal if vsync is enabled).
     */
    actual fun needRedraw() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }
        redrawer?.needRedraw()
    }

    @Suppress("LeakingThis")
    private val fpsCounter = defaultFPSCounter(this)

    internal fun update(nanoTime: Long) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }

        fpsCounter?.tick()

        val pictureWidth = (width * contentScale).toInt().coerceAtLeast(0)
        val pictureHeight = (height * contentScale).toInt().coerceAtLeast(0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val canvas = pictureRecorder.beginRecording(bounds)

        // clipping
        for (component in clipComponents) {
            canvas.clipRectBy(component)
        }

        try {
            isRendering = true
            renderer?.onRender(canvas, pictureWidth, pictureHeight, nanoTime)
        } finally {
            isRendering = false
        }

        // we can dispose layer during onRender
        if (!isDisposed) {
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
                println(e.message)
                findNextWorkingRenderApi()
                redrawer?.redrawImmediately()
            }
        }
    }

    // can be called from non-swing thread
    // throws exception if initialization of graphic context was not successful
    internal fun draw() {
        contextHandler?.apply {
            if (!initContext()) {
                throw RenderException("Cannot init graphic context")
            }
            initCanvas()
        }

        check(!isDisposed) { "SkiaLayer is disposed" }
        contextHandler?.apply {
            clearCanvas()
            synchronized(pictureLock) {
                val picture = picture
                if (picture != null) {
                    drawOnCanvas(picture.instance)
                }
            }
            flush()
        }
        FrameWatcher.nextFrame()
    }

    // Captures current layer as bitmap.
    fun screenshot(): Bitmap? {
        return contextHandler?.let {
            synchronized(pictureLock) {
                val picture = picture
                if (picture != null) {
                    val store = Bitmap()
                    val ci = ColorInfo(
                        ColorType.BGRA_8888, ColorAlphaType.OPAQUE, ColorSpace.sRGB)
                    store.setImageInfo(ImageInfo(ci, picture.width, picture.height))
                    store.allocN32Pixels(picture.width, picture.height)
                    val canvas = Canvas(store)
                    canvas.drawPicture(picture.instance)
                    store.setImmutable()
                    store
                } else {
                    null
                }
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
}
