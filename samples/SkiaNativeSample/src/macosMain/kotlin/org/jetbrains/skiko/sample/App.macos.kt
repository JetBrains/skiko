package org.jetbrains.skiko.sample

import platform.AppKit.*

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import kotlinx.cinterop.*
import kotlin.math.cos
import kotlin.math.sin

fun main() {
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
        renderer, w, h, nanoTime -> displayScene(renderer.canvas!!, nanoTime)
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

