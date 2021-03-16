package org.jetbrains.skiko

import org.jetbrains.skija.Canvas
import org.jetbrains.skija.ClipMode
import org.jetbrains.skija.Picture
import org.jetbrains.skija.PictureRecorder
import org.jetbrains.skija.Rect
import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.context.createContextHandler
import org.jetbrains.skiko.redrawer.Redrawer
import java.awt.event.InputMethodListener
import java.awt.event.KeyListener
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelListener
import java.awt.Graphics
import java.awt.event.HierarchyEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities.invokeLater
import javax.swing.SwingUtilities.isEventDispatchThread

interface SkiaRenderer {
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

private class PictureHolder(val instance: Picture, val width: Int, val height: Int)

open class SkiaLayer(
    private val properties: SkiaLayerProperties = SkiaLayerProperties()
) : JPanel() {

    val backedLayer : HardwareLayer

    init {
        setOpaque(false)
        layout = null
        backedLayer = object : HardwareLayer() { }
        add(backedLayer)
        @Suppress("LeakingThis")
        backedLayer.addHierarchyListener {
            if (it.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
                checkIsShowing()
            }
        }
    }

    private var isInited = false

    private fun checkIsShowing() {
        if (!isInited && isShowing) {
            backedLayer.defineContentScale()
            init()
            isInited = true
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

    @Volatile
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()
    private val pictureLock = Any()

    open fun init() {
        backedLayer.init()
        val initialRenderApi = fallbackRenderApiQueue.removeAt(0)
        contextHandler = createContextHandler(this, initialRenderApi)
        redrawer = platformOperations.createRedrawer(this, initialRenderApi, properties)
        redrawer?.syncSize()
        redraw()
    }

    open fun dispose() {
        check(!isDisposed)
        check(isEventDispatchThread())
        redrawer?.dispose()  // we should dispose redrawer first (to cancel `draw` in rendering thread)
        contextHandler?.dispose()
        picture?.instance?.close()
        pictureRecorder.close()
        isDisposed = true
        if (isInited) {
            backedLayer.dispose()
        }
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        super.setBounds(x, y, width, height)
        backedLayer.setSize(width, height)
        if (backedLayer.checkContentScale()) {
            contentScaleChanged()
        }
        redrawer?.syncSize()
        redraw()
        revalidate()
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (backedLayer.checkContentScale()) {
            contentScaleChanged()
        }
        redrawer?.syncSize()
        redrawer?.redrawImmediately()
    }

    override fun addInputMethodListener(l: InputMethodListener) {
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

    private var redrawScheduled = false

    /**
     * Redraw as soon as possible (but not right now)
     */
    fun redraw() {
        if (!redrawScheduled) {
            redrawScheduled = true
            invokeLater {
                redrawScheduled = false
                if (!isDisposed) {
                    redrawer?.redrawImmediately()
                }
            }
        }
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

    open fun update(nanoTime: Long) {
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

        renderer?.onRender(canvas, pictureWidth, pictureHeight, nanoTime)

        // we can dispose layer during onRender
        if (!isDisposed) {
            synchronized(pictureLock) {
                picture?.instance?.close()
                val picture = pictureRecorder.finishRecordingAsPicture()
                this.picture = PictureHolder(picture, pictureWidth, pictureHeight)
            }
        }
    }

    open fun draw() {
        check(!isDisposed)
        contextHandler?.apply {
            if (!initContext()) {
                fallbackToNextApi()
                return
            }
            initCanvas()
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
        val nextApi = fallbackRenderApiQueue.removeAt(0)
        println("Falling back to $nextApi rendering...")
        contextHandler?.dispose()
        redrawer?.dispose()
        contextHandler = createContextHandler(this, nextApi)
        redrawer = platformOperations.createRedrawer(this, nextApi, properties)
        needRedraw()
    }
}
