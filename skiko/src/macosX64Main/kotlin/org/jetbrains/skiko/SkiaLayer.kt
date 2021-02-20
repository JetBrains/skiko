package org.jetbrains.skiko.native

import kotlinx.cinterop.pointed
import kotlinx.cinterop.useContents
import org.jetbrains.skiko.native.context.*
import org.jetbrains.skiko.native.redrawer.*
import org.jetbrains.skiko.skia.native.*

interface SkiaRenderer {
    fun onRender(canvas: SkCanvas, width: Int, height: Int, nanoTime: Long)
}

// TODO: this is exact copy of jvm counterpart. Commonize!
private class PictureHolder(val instance: SkPicture, val width: Int, val height: Int)

open class SkiaLayer(
    private val properties: SkiaLayerProperties = SkiaLayerProperties()
) : HardwareLayer() {
    var renderer: SkiaRenderer? = null

    internal var skiaState = createContextHandler(this)

    private var isDisposed = false

    private var redrawer: Redrawer? = null

    private var picture: PictureHolder? = null
    private val pictureRecorder = SkPictureRecorder()
    private val pictureLock = Any()

    override fun init() {
        println("SkiaLayer::init")
        super.init()
        redrawer = platformOperations.createRedrawer(this, properties)
        redrawer?.redrawImmediately()
    }

    override fun dispose() {
        redrawer?.dispose()
        super.dispose()
    }

    fun needRedraw() {
        redrawer?.needRedraw()
    }

    override fun update(nanoTime: Long) {
        println("SkiaLayer::update")

        val width = nsView.frame.useContents { size.width }
        val height = nsView.frame.useContents { size.height }

        val pictureWidth = (width * contentScale).coerceAtLeast(0.0)
        val pictureHeight = (height * contentScale).coerceAtLeast(0.0)

        val bounds = SkRect.MakeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val canvas = pictureRecorder.beginRecording(bounds, null)!!

        // TODO: get rid of .pointed?
        renderer?.onRender(canvas.pointed, pictureWidth.toInt(), pictureHeight.toInt(), nanoTime)

        val picture = pictureRecorder.finishRecordingAsPicture()!!
        this.picture = PictureHolder(picture, pictureWidth.toInt(), pictureHeight.toInt())
    }

    override fun draw() {
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

