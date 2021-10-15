package org.jetbrains.skiko.sample

import platform.AppKit.*

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import kotlinx.cinterop.*

fun main() {
    NSApplication.sharedApplication()
    val layer = SkiaLayer()
    layer.renderer = GenericRenderer(layer) {
            canvas, w, h, nanoTime -> displayScene(canvas, nanoTime)
    }
    val window = SkiaWindow(layer)
    window.nsWindow.orderFrontRegardless()
    NSApp?.run()
}
