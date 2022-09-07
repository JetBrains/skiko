package org.jetbrains.skiko

import org.jetbrains.skia.Canvas

interface SkikoView {
    // Input
    fun onKeyboardEvent(event: SkikoKeyboardEvent) = Unit
    fun onPointerEvent(event: SkikoPointerEvent) = Unit

    @Deprecated("This method will be removed. Use override val input: SkikoInput")
    fun onInputEvent(event: SkikoInputEvent) = Unit
    val input: SkikoInput get() = SkikoInput.Empty
    fun onTouchEvent(events: Array<SkikoTouchEvent>) = Unit
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

    override val input: SkikoInput get() = app.input

    override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
        app.onKeyboardEvent(event)
    }

    override fun onPointerEvent(event: SkikoPointerEvent) {
        app.onPointerEvent(event)
    }

    override fun onTouchEvent(events: Array<SkikoTouchEvent>) {
        app.onTouchEvent(events)
    }

    override fun onGestureEvent(event: SkikoGestureEvent) {
        app.onGestureEvent(event)
    }
}
