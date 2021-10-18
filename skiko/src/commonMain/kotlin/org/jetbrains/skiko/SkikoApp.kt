package org.jetbrains.skiko

import org.jetbrains.skia.Canvas

interface SkikoApp {
    // Input
    fun onKeyboardEvent(event: SkikoKeyboardEvent)
    fun onMouseEvent(event: SkikoMouseEvent)
    fun onInputEvent(event: SkikoInputEvent)

    // Rendering
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

open class GenericSkikoApp(
    val layer: SkiaLayer,
    val app: SkikoApp): SkikoApp {

    init {
        layer.setApp(this)
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        app.onRender(canvas, (width / contentScale).toInt(), (height / contentScale).toInt(), nanoTime)
        // Request next frame immediately.
        layer.needRedraw()
    }

    override fun onInputEvent(event: SkikoInputEvent) {
        app.onInputEvent(event)
    }

    override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
        app.onKeyboardEvent(event)
    }

    override fun onMouseEvent(event: SkikoMouseEvent) {
        app.onMouseEvent(event)
    }
}

abstract class NoInputSkikoApp: SkikoApp {

    override fun onInputEvent(event: SkikoInputEvent) {
    }

    override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
    }

    override fun onMouseEvent(event: SkikoMouseEvent) {
    }
}
