package org.jetbrains.skiko

import kotlinx.cinterop.useContents
import org.jetbrains.skiko.context.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.Redrawer
import platform.AppKit.NSView
import platform.Foundation.NSMakeRect

actual open class SkiaLayer actual constructor(
    private val properties: SkiaLayerProperties
) {
    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL
    actual val contentScale: Float
        get() = _contentScale

    actual var fullscreen: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("fullscreen unsupported")
        }

    actual var transparency: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("transparency unsupported")
        }

    val nsView = NSView(NSMakeRect(0.0, 0.0, 640.0, 480.0))
    var _contentScale: Float = 1.0f

    var renderer: SkiaRenderer? = null

    private var contextHandler = MacOSOpenGLContextHandler(this)

    private var redrawer: Redrawer? = null

    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()

    fun initLayer() {
        println("SkiaLayer.initLayer")
        redrawer = createNativeRedrawer(this, GraphicsApi.OPENGL, properties)
        redrawer?.redrawImmediately()
    }

    fun disposeLayer() {
        redrawer?.dispose()
        redrawer = null
        initedCanvas = false
    }

    actual fun needRedraw() {
        redrawer?.needRedraw()
    }

    fun update(nanoTime: Long) {
        println("SkiaLayer.update")

        val width = nsView.frame.useContents { size.width }
        val height = nsView.frame.useContents { size.height }

        val pictureWidth = (width * contentScale).coerceAtLeast(0.0)
        val pictureHeight = (height * contentScale).coerceAtLeast(0.0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
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

