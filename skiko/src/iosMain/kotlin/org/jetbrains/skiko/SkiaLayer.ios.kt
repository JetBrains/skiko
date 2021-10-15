package org.jetbrains.skiko

import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.context.MetalContextHandler
import org.jetbrains.skiko.redrawer.MetalRedrawer
import platform.UIKit.*

actual open class SkiaLayer(
    var width: Float, var height: Float,
    val properties: SkiaLayerProperties = makeDefaultSkiaLayerProperties()
)
{
    fun isShowing(): Boolean {
        return true
    }

    actual var renderApi: GraphicsApi
        get() = GraphicsApi.METAL
        set(value) { throw UnsupportedOperationException() }

    actual val contentScale: Float
        get() = 1.0f

    actual var fullscreen: Boolean
        get() = true
        set(value) { throw UnsupportedOperationException() }

    actual var transparency: Boolean
        get() = false
        set(value) { throw UnsupportedOperationException() }

    actual fun needRedraw() {
        redrawer?.needRedraw()
    }

    lateinit var view: UIView

    fun initLayer(view: UIView) {
        println("SkiaLayer.initLayer")
        this.view = view
        redrawer = MetalRedrawer(this, properties)
        redrawer?.redrawImmediately()
    }

    actual var renderer: SkiaRenderer? = null
    internal var redrawer: MetalRedrawer? = null
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()
    private val contextHandler = MetalContextHandler(this)

    fun update(nanoTime: Long) {
        val pictureWidth = (width * contentScale).coerceAtLeast(0.0F)
        val pictureHeight = (height * contentScale).coerceAtLeast(0.0F)

        val bounds = Rect.makeWH(pictureWidth, pictureHeight)
        val canvas = pictureRecorder.beginRecording(bounds)
        renderer?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), nanoTime)
        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, pictureWidth.toInt(), pictureHeight.toInt())
    }

    fun draw() {
        contextHandler.apply {
            if (!initContext()) {
                error("initContext() failure")
            }
            initCanvas()
            clearCanvas()
            val picture = picture
            if (picture != null) {
                drawOnCanvas(picture.instance)
            }
            flush()
        }
    }
}
