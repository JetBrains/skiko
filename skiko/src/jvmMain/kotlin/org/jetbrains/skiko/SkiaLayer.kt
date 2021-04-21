package org.jetbrains.skiko

import org.jetbrains.skija.Canvas
import org.jetbrains.skija.ClipMode
import org.jetbrains.skija.Picture
import org.jetbrains.skija.PictureRecorder
import org.jetbrains.skija.Rect
import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.context.createContextHandler
import org.jetbrains.skiko.redrawer.Redrawer
import java.awt.Graphics
import java.awt.event.*
import java.awt.im.InputMethodRequests
import javax.swing.JPanel
import javax.swing.SwingUtilities.isEventDispatchThread

interface SkiaRenderer {
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

private class PictureHolder(val instance: Picture, val width: Int, val height: Int)

open class SkiaLayer(
    private val properties: SkiaLayerProperties = SkiaLayerProperties()
) : JPanel() {

    internal val backedLayer : HardwareLayer

    init {
        setOpaque(false)
        layout = null
        backedLayer = object : HardwareLayer() {
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
        add(backedLayer)
        @Suppress("LeakingThis")
        backedLayer.addHierarchyListener {
            if (it.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
                checkIsShowing()
            }
        }
    }

    private var isInited = false
    private var isRendering = false

    private fun checkIsShowing() {
        if (!isInited && isShowing) {
            backedLayer.defineContentScale()
            init()
        }
    }

    val contentScale: Float
        get() = backedLayer.contentScale

    val windowHandle: Long
        get() = backedLayer.windowHandle

    var fullscreen: Boolean
        get() = backedLayer.fullscreen
        set(value) { backedLayer.fullscreen = value }

    protected open fun contentScaleChanged() = Unit

    var renderer: SkiaRenderer? = null
    val clipComponents = mutableListOf<ClipRectangle>()

    @Volatile
    private var isDisposed = false
    internal var redrawer: Redrawer? = null
    private var contextHandler: ContextHandler? = null
    private val fallbackRenderApiQueue = SkikoProperties.fallbackRenderApiQueue.toMutableList()
    var renderApi: GraphicsApi = fallbackRenderApiQueue[0]
        private set

    @Volatile
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()
    private val pictureLock = Any()


    open fun init() {
        backedLayer.init()
        renderApi = fallbackRenderApiQueue.removeAt(0)
        contextHandler = createContextHandler(this, renderApi)
        redrawer = platformOperations.createRedrawer(this, renderApi, properties)
        isInited = true
    }

    open fun dispose() {
        check(!isDisposed)
        check(isEventDispatchThread())

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
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (backedLayer.checkContentScale()) {
            contentScaleChanged()
        }
        redrawer?.syncSize()

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
        if (e != null)
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

    /**
     * Redraw on the next animation Frame (on vsync signal if vsync is enabled).
     */
    fun needRedraw() {
        check(!isDisposed)
        check(isEventDispatchThread())
        redrawer?.needRedraw()
    }

    @Suppress("LeakingThis")
    private val fpsCounter = defaultFPSCounter(this)

    internal fun update(nanoTime: Long) {
        check(!isDisposed)
        check(isEventDispatchThread())

        fpsCounter?.tick()

        val pictureWidth = (width * contentScale).toInt().coerceAtLeast(0)
        val pictureHeight = (height * contentScale).toInt().coerceAtLeast(0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val canvas = pictureRecorder.beginRecording(bounds)!!

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

    internal fun prepareDrawContext(): Boolean {
        check(!isDisposed)
        contextHandler?.apply {
            if (!initContext()) {
                fallbackToNextApi()
                return false
            }
            initCanvas()
        }
        return true
    }

    internal fun draw() {
        check(!isDisposed)
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

    private fun fallbackToNextApi() {
        renderApi = fallbackRenderApiQueue.removeAt(0)
        println("Falling back to $renderApi rendering...")
        contextHandler?.dispose()
        redrawer?.dispose()
        contextHandler = createContextHandler(this, renderApi)
        redrawer = platformOperations.createRedrawer(this, renderApi, properties)
        redrawer!!.redrawImmediately()
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
