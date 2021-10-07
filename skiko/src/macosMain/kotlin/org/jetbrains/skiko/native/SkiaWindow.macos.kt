package org.jetbrains.skiko.native

import org.jetbrains.skiko.SkiaLayer
import platform.AppKit.*
import platform.Cocoa.*
import platform.Foundation.*

open class SkiaWindow(
    properties: SkiaLayerProperties = SkiaLayerProperties()
) {
    val windowStyle =
        NSWindowStyleMaskTitled or
        NSWindowStyleMaskMiniaturizable or
        NSWindowStyleMaskClosable or
        NSWindowStyleMaskResizable

    // TODO: what's the proper way to create window here?
    // Should we subclass NSWindow?
    // Provide multiple constructors for SkiaWindow?
    // Behave similar to Skiko/JVM?
    val nsWindow = NSWindow(
        contentRect =  NSMakeRect(0.0, 0.0, 640.0, 480.0),
        styleMask = windowStyle,
        backing =  NSBackingStoreBuffered,
        defer =  true)

    val layer = SkiaLayer(properties)

    init {
        nsWindow.contentView?.addSubview(layer.nsView)
        layer.checkIsShowing() // TODO: The awt versions has hierarchy listener. What should we use here?
    }
}
