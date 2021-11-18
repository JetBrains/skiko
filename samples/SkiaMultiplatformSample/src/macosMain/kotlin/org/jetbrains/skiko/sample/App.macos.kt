package org.jetbrains.skiko.sample

import platform.AppKit.*

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import kotlinx.cinterop.*
import platform.Foundation.NSMakeRect
import platform.darwin.NSObject

fun makeApp(skiaLayer: SkiaLayer) = Clocks(skiaLayer)

fun main() {
    val app = NSApplication.sharedApplication()
    app.delegate = object: NSObject(), NSApplicationDelegateProtocol {
        override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean {
            return true
        }
    }
    val windowStyle = NSWindowStyleMaskTitled or
                NSWindowStyleMaskMiniaturizable or
                NSWindowStyleMaskClosable or
                NSWindowStyleMaskResizable
    val window = NSWindow(
        contentRect = NSMakeRect(0.0, 0.0, 640.0, 480.0),
        styleMask = windowStyle,
        backing = NSBackingStoreBuffered,
        defer = true)
    val skiaLayer = SkiaLayer()
    skiaLayer.skikoView = GenericSkikoView(skiaLayer, makeApp(skiaLayer))
    skiaLayer.attachTo(window)
    window.orderFrontRegardless()
    app.run()
}
