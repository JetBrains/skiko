package org.jetbrains.skiko

import org.jetbrains.skia.Canvas

interface SkikoView {
    // Input
    fun onKeyboardEvent(event: SkikoKeyboardEvent)
    fun onPointerEvent(event: SkikoPointerEvent)
    fun onInputEvent(event: SkikoInputEvent)

    // Rendering
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

open class GenericSkikoView(
        val layer: SkiaLayer,
        val app: SkikoView
    ): SkikoView {

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

    override fun onPointerEvent(event: SkikoPointerEvent) {
        app.onPointerEvent(event)
    }
}

abstract class NoInputSkikoView: SkikoView {

    override fun onInputEvent(event: SkikoInputEvent) {
    }

    override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
    }

    override fun onPointerEvent(event: SkikoPointerEvent) {
    }
}
