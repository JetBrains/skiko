package org.jetbrains.skiko.sample.native

import platform.AppKit.*

import org.jetbrains.skia.*
import org.jetbrains.skiko.native.SkiaLayer
import org.jetbrains.skiko.native.SkiaRenderer
import org.jetbrains.skiko.native.SkiaWindow
import kotlinx.cinterop.*
import kotlin.math.cos
import kotlin.math.sin

fun main(args: Array<String>) {
    // TODO: Remove me! This is to run all cleaners before main() exits.
    kotlin.native.internal.Debugging.forceCheckedShutdown = true

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
    var canvas: Canvas? = null

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        this.canvas = canvas
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        displayScene(this, (width / contentScale).toInt(), (height / contentScale).toInt(), nanoTime)
        layer.needRedraw()
    }
}

fun displayScene(renderer: Renderer, nanoTime: Long) {
    val canvas = renderer.canvas!!

    val paint = Paint()
    // TODO: make color constants.
    //paint.setColor(Color.GREEN)
    paint.setColor(0xFF00FF00.toInt())

    // TODO: make color constants.
    // canvas.clear(Color.RED)
    canvas.clear(0xFFFF0000.toInt())

    canvas.save();
    canvas.translate(128.0f, 128.0f)
    canvas.rotate(nanoTime.toFloat() / 1e7f)
    val rect = Rect.makeXYWH(-90.5f, -90.5f, 181.0f, 181.0f)
    canvas.drawRect(rect, paint)
    canvas.restore();
}

