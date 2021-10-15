package org.jetbrains.skiko.sample

import platform.AppKit.*

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import kotlinx.cinterop.*

fun makeApp() = BouncingBalls()

fun main() {
    NSApplication.sharedApplication()
    val layer = SkiaLayer()
    val window = SkiaWindow(layer)
    layer.renderer = GenericRenderer(layer, makeApp())
    window.nsWindow.orderFrontRegardless()
    NSApp?.run()
}
