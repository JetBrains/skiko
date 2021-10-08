package org.jetbrains.skiko

import kotlinx.cinterop.useContents
import org.jetbrains.skiko.context.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.Redrawer

actual open class SkiaLayer(
    private val properties: SkiaLayerProperties = makeDefaultSkiaLayerProperties()
) {
    internal actual val backedLayer: HardwareLayer = HardwareLayer()
    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL

    actual val contentScale: Float
        get() = backedLayer.contentScale

    var renderer: SkiaRenderer? = null

    internal var skiaState = MacOSOpenGLContextHandler(this)

    private var isDisposed = false

    private var redrawer: Redrawer? = null

    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()
    private val pictureLock = Any()

    fun init() {
        println("SkiaLayer::init")
        redrawer = createNativeRedrawer(this, GraphicsApi.OPENGL, properties)
        redrawer?.redrawImmediately()
    }

    fun dispose() {
        redrawer?.dispose()
    }

    fun needRedraw() {
        redrawer?.needRedraw()
    }

    fun update(nanoTime: Long) {
        println("SkiaLayer::update")

        val width = backedLayer.nsView.frame.useContents { size.width }
        val height = backedLayer.nsView.frame.useContents { size.height }

        val pictureWidth = (width * backedLayer.contentScale).coerceAtLeast(0.0)
        val pictureHeight = (height * backedLayer.contentScale).coerceAtLeast(0.0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val canvas = pictureRecorder.beginRecording(bounds)
        renderer?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), nanoTime)

        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, pictureWidth.toInt(), pictureHeight.toInt())
    }

    fun draw() {
        println("SkiaLayer::draw")
        skiaState.apply {
            if (!initContext()) {
                error("initContext() failure. No fallback to raster for Skia/native yet.")
                return
            }
            initCanvas()
            clearCanvas()
            val picture = picture
            println("SkiaLayer::draw: picture=$picture")
            if (picture != null) {
                drawOnCanvas(picture.instance)
            }
            flush()
        }
    }
}

