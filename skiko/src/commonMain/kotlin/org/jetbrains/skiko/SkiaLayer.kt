package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture

expect open class SkiaLayer {
    var renderApi: GraphicsApi
    val contentScale: Float
    var fullscreen: Boolean
    var transparency: Boolean

    var renderer: SkiaRenderer?

    var eventProcessor: SkikoEventProcessor?

    fun needRedraw()
}

fun SkiaLayer.setApp(renderer: SkiaRenderer) {
    this.renderer = renderer
    if (renderer is SkikoEventProcessor) {
        this.eventProcessor = renderer
    }
}

object SkikoMouseButtons {
    const val NONE = 0
    const val LEFT = 1 shl 0
    const val RIGHT = 1 shl 1
    const val MIDDLE = 1 shl 2
}

expect class SkikoPlatformInputEvent
data class SkikoInputEvent(
    val input: String,
    val platform: SkikoPlatformInputEvent?
)

enum class SkikoKeyboardEventKind {
    UP, DOWN
}
expect class SkikoPlatformKeyboardEvent
data class SkikoKeyboardEvent(
    val code: Int,
    val kind: SkikoKeyboardEventKind,
    val platform: SkikoPlatformKeyboardEvent?
)

enum class SkikoMouseEventKind {
    UP, DOWN, MOVE
}
expect class SkikoPlatformPointerEvent
data class SkikoMouseEvent(
    val x: Int, val y: Int,
    val buttonMask: Int,
    val kind: SkikoMouseEventKind,
    val platform: SkikoPlatformPointerEvent?
)

val SkikoMouseEvent.isLeftClick: Boolean
    get() = (buttonMask and SkikoMouseButtons.LEFT) != 0 && (kind == SkikoMouseEventKind.UP)

val SkikoMouseEvent.isRightClick: Boolean
    get() = (buttonMask and SkikoMouseButtons.RIGHT) != 0 && (kind == SkikoMouseEventKind.UP)

interface SkikoEventProcessor {
    fun onKeyboardEvent(event: SkikoKeyboardEvent)
    fun onMouseEvent(event: SkikoMouseEvent)
    fun onInputEvent(event: SkikoInputEvent)
}

interface SkiaRenderer {
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

open class GenericSkikoApp(
    val layer: SkiaLayer,
    val appRenderer: SkiaRenderer,
    val appEventProcessor: SkikoEventProcessor? = if (appRenderer is SkikoEventProcessor) appRenderer else null
): SkiaRenderer, SkikoEventProcessor {
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        appRenderer.onRender(canvas, (width / contentScale).toInt(), (height / contentScale).toInt(), nanoTime)
        layer.needRedraw()
    }

    override fun onInputEvent(event: SkikoInputEvent) {
        appEventProcessor?.onInputEvent(event)
    }

    override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
        appEventProcessor?.onKeyboardEvent(event)
    }

    override fun onMouseEvent(event: SkikoMouseEvent) {
        appEventProcessor?.onMouseEvent(event)
    }
}

internal class PictureHolder(val instance: Picture, val width: Int, val height: Int)
