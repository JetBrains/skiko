package org.jetbrains.skiko

import kotlinx.cinterop.*
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.context.MetalContextHandler
import org.jetbrains.skiko.redrawer.MetalRedrawer
import org.jetbrains.skiko.redrawer.Redrawer
import platform.Foundation.*
import platform.UIKit.*

actual open class SkiaLayer actual constructor(
    val properties: SkiaLayerProperties
) {
    var width: Float = 0f
    var height: Float = 0f

    constructor(width: Float, height: Float) : this(
        makeDefaultSkiaLayerProperties()) {
        this.width = width
        this.height = height
    }

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

    var renderer: SkiaRenderer? = null
    internal var redrawer: MetalRedrawer? = null
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()
    private val contextHandler = MetalContextHandler(this)

    fun update(nanoTime: Long) {
        println("SkiaLayer.update")

        val pictureWidth = (width * contentScale).coerceAtLeast(0.0F)
        val pictureHeight = (height * contentScale).coerceAtLeast(0.0F)

        val bounds = Rect.makeWH(pictureWidth, pictureHeight)
        val canvas = pictureRecorder.beginRecording(bounds)
        renderer?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), nanoTime)

        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, pictureWidth.toInt(), pictureHeight.toInt())
    }

    private var initedCanvas = false

    fun draw() {
        println("SkiaLayer.draw")
        contextHandler.apply {
            if (!initedCanvas) {
                if (!initContext()) {
                    error("initContext() failure")
                    return
                }
                initCanvas()
                initedCanvas = true
            }
            clearCanvas()
            val picture = picture
            println("SkiaLayer.draw: picture=$picture")
            if (picture != null) {
                drawOnCanvas(picture.instance)
            }
            flush()
        }
    }
}
