package org.jetbrains.skiko

import org.jetbrains.skia.Canvas

interface SkikoView {
    // Input
    fun onKeyboardEvent(event: SkikoKeyboardEvent) = Unit
    fun onPointerEvent(event: SkikoPointerEvent) = Unit
    fun onInputEvent(event: SkikoInputEvent) = Unit
    fun onTouchEvent(events: Set<SkikoTouchEvent>) = Unit
    fun onGestureEvent(event: SkikoGestureEvent) = Unit

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

    override fun onTouchEvent(events: Set<SkikoTouchEvent>) {
        app.onTouchEvent(events)
    }

    override fun onGestureEvent(event: SkikoGestureEvent) {
        app.onGestureEvent(event)
    }
}
