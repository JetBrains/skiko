package org.jetbrains.skiko

import kotlinx.cinterop.*
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.redrawer.Redrawer
import platform.Foundation.*
import platform.UIKit.*

actual open class SkiaLayer actual constructor(
    properties: SkiaLayerProperties
) {
    var width: Int = 0
    var height: Int = 0

    constructor(width: Int, height: Int) : this(
        makeDefaultSkiaLayerProperties()) {
        this.width = width
        this.height = height
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

    fun initLayer() {
        println("SkiaLayer.initLayer")
        //redrawer = createNativeRedrawer(this, renderApi, properties)
        redrawer?.redrawImmediately()
    }

    var renderer: SkiaRenderer? = null
    private var redrawer: Redrawer? = null
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()

    fun update(nanoTime: Long) {
        println("SkiaLayer.update")

        val width = 800f
        val height = 600f

        val pictureWidth = (width * contentScale).coerceAtLeast(0.0F)
        val pictureHeight = (height * contentScale).coerceAtLeast(0.0F)

        val bounds = Rect.makeWH(pictureWidth, pictureHeight)
        val canvas = pictureRecorder.beginRecording(bounds)
        renderer?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), nanoTime)

        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, pictureWidth.toInt(), pictureHeight.toInt())
    }
}
