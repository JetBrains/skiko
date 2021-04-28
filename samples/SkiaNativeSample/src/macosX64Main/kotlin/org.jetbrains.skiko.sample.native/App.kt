package org.jetbrains.skiko.sample.native

import platform.AppKit.*

import org.jetbrains.skiko.skia.native.*
import org.jetbrains.skiko.native.SkiaLayer
import org.jetbrains.skiko.native.SkiaRenderer
import org.jetbrains.skiko.native.SkiaWindow
import kotlinx.cinterop.*
import kotlin.math.cos
import kotlin.math.sin

fun main(args: Array<String>) {
    NSApplication.sharedApplication()
    createWindow("Skia/Native macos sample")
    NSApp?.run()
}

fun createWindow(title: String)  {
    val window = SkiaWindow()

    var angle = 0.0f

    window.layer.renderer = Renderer(window.layer) {
        renderer, w, h, nanoTime -> displayScene(renderer, nanoTime)
    }

    window.nsWindow.orderFrontRegardless()
}

class Renderer(
    val layer: SkiaLayer,
    val displayScene: (Renderer, Int, Int, Long) -> Unit
): SkiaRenderer {
    var canvas: SkCanvas? = null

    override fun onRender(canvas: SkCanvas, width: Int, height: Int, nanoTime: Long) {
        this.canvas = canvas
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        displayScene(this, (width / contentScale).toInt(), (height / contentScale).toInt(), nanoTime)
        layer.needRedraw()
    }
}

fun displayScene(renderer: Renderer, nanoTime: Long) {
    val canvas = renderer.canvas!!

    val paint = SkPaint()
    paint.setColor(SK_ColorGREEN)

    canvas.clear(SK_ColorRED);

    canvas.save();
    canvas.translate(128.0f, 128.0f)
    canvas.rotate(nanoTime.toFloat() / 1e7f)
    val rect = SkRect.MakeXYWH(-90.5f, -90.5f, 181.0f, 181.0f)
    canvas.drawRect(rect, paint.ptr)
    canvas.restore();

    // TODO: need memory management.
    // nativeHeap.free(paint)
}
