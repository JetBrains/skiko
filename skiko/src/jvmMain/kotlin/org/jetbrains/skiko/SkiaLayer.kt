package org.jetbrains.skiko

import org.jetbrains.skija.*
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.RasterRedrawer
import org.jetbrains.skiko.context.createContextHandler
import org.jetbrains.skiko.context.SoftwareContextHandler
import java.awt.Graphics
import javax.swing.SwingUtilities.isEventDispatchThread

interface SkiaRenderer {
    suspend fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

private class PictureHolder(val instance: Picture, val width: Int, val height: Int)

open class SkiaLayer : HardwareLayer() {
    var renderer: SkiaRenderer? = null
    val clipComponents = mutableListOf<ClipRectangle>()

    internal var skijaState = createContextHandler(this)

    @Volatile
    private var isDisposed = false
    private var redrawer: Redrawer? = null

    @Volatile
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()
    private val pictureLock = Any()

    override fun init() {
        super.init()
        redrawer = platformOperations.createRedrawer(this)
        redrawer?.syncSize()
        needRedraw()
    }

    override fun dispose() {
        check(!isDisposed)
        check(isEventDispatchThread())
        redrawer?.dispose()
        picture?.instance?.close()
        pictureRecorder.close()
        isDisposed = true
        super.dispose()
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        super.setBounds(x, y, width, height)
        redrawer?.syncSize()
        needRedraw()
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

    private val fpsCounter = FPSCounter(
        count = SkikoProperties.fpsCount,
        probability = SkikoProperties.fpsProbability
    )

    override suspend fun update(nanoTime: Long) {
        check(!isDisposed)
        check(isEventDispatchThread())

        if (SkikoProperties.fpsEnabled) {
            fpsCounter.tick()
        }

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
        skijaState.apply {
            if (!initContext()) {
                fallbackToRaster()
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

    private fun fallbackToRaster() {
        println("Falling back to software rendering...")
        redrawer?.dispose()
        skijaState = SoftwareContextHandler(this)
        redrawer = RasterRedrawer(this)
        needRedraw()
    }
}
