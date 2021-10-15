package org.jetbrains.skiko.sample

import platform.AppKit.*

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import kotlinx.cinterop.*
import platform.Foundation.NSMakeRect

fun makeApp() = BouncingBalls()

fun main() {
    NSApplication.sharedApplication()
    val skiaLayer = SkiaLayer()
    val windowStyle = NSWindowStyleMaskTitled or
                NSWindowStyleMaskMiniaturizable or
                NSWindowStyleMaskClosable or
                NSWindowStyleMaskResizable
    val window = NSWindow(
        contentRect = NSMakeRect(0.0, 0.0, 640.0, 480.0),
        styleMask = windowStyle,
        backing =  NSBackingStoreBuffered,
        defer = true)
    val app = makeApp()
    skiaLayer.renderer = GenericSkikoApp(skiaLayer, app, app)
    skiaLayer.initLayer(window)
    window.orderFrontRegardless()
    NSApp?.run()
}
