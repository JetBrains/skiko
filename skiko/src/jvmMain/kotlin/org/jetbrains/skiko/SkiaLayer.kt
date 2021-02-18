package org.jetbrains.skiko

import org.jetbrains.skija.Canvas
import org.jetbrains.skija.ClipMode
import org.jetbrains.skija.Picture
import org.jetbrains.skija.PictureRecorder
import org.jetbrains.skija.Rect
import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.context.createContextHandler
import org.jetbrains.skiko.context.SoftwareContextHandler
import org.jetbrains.skiko.redrawer.SoftwareRedrawer
import org.jetbrains.skiko.redrawer.Redrawer
import java.awt.Graphics
import javax.swing.SwingUtilities.isEventDispatchThread
import kotlin.collections.MutableList
import kotlin.collections.toMutableList

interface SkiaRenderer {
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

private class PictureHolder(val instance: Picture, val width: Int, val height: Int)

open class SkiaLayer : HardwareLayer() {
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

    override fun init() {
        super.init()
        val initialRenderApi = fallbackRenderApiQueue.removeAt(0)
        contextHandler = createContextHandler(this, initialRenderApi)
        redrawer = platformOperations.createRedrawer(this, initialRenderApi)
        redrawer?.syncSize()
        redrawer?.redrawImmediately()
    }

    override fun dispose() {
        check(!isDisposed)
        check(isEventDispatchThread())
        contextHandler?.dispose()
        redrawer?.dispose()
        picture?.instance?.close()
        pictureRecorder.close()
        isDisposed = true
        super.dispose()
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        super.setBounds(x, y, width, height)
        redrawer?.syncSize()
        redrawer?.redrawImmediately()
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        redrawer?.syncSize()
        needRedraw()
    }

    fun needRedraw() {
        check(!isDisposed)
        check(isEventDispatchThread())
        redrawer?.needRedraw()
    }

    @Suppress("LeakingThis")
    private val fpsCounter = defaultFPSCounter(this)

    override fun update(nanoTime: Long) {
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

    override fun draw() {
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
        redrawer?.dispose()
        contextHandler = createContextHandler(this, nextApi)
        redrawer = platformOperations.createRedrawer(this, nextApi)
        needRedraw()
    }
}
