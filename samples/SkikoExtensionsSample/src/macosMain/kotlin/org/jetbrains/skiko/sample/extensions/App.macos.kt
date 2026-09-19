package org.jetbrains.skiko.sample.extensions

import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.AppKit.NSBackingStoreBuffered
import platform.AppKit.NSView
import platform.AppKit.NSViewHeightSizable
import platform.AppKit.NSViewWidthSizable
import platform.AppKit.NSWindow
import platform.AppKit.NSWindowStyleMaskClosable
import platform.AppKit.NSWindowStyleMaskMiniaturizable
import platform.AppKit.NSWindowStyleMaskResizable
import platform.AppKit.NSWindowStyleMaskTitled
import platform.Foundation.NSMakeRect
import platform.Foundation.NSTimer

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val exitAfterMillis = exitAfterMillis(args)

    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)

    val appName = APP_NAME
    val window = object : NSWindow(
        contentRect = NSMakeRect(0.0, 0.0, 640.0, 640.0),
        styleMask = NSWindowStyleMaskTitled or
            NSWindowStyleMaskMiniaturizable or
            NSWindowStyleMaskClosable or
            NSWindowStyleMaskResizable,
        backing = NSBackingStoreBuffered,
        defer = false
    ) {
        override fun canBecomeKeyWindow() = true
        override fun canBecomeMainWindow() = true
    }.apply {
        title = appName
    }

    val skiaLayer = SkiaLayer()
    val player = loadSkottieAnimationPlayer()
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer) { canvas, width, height, _ ->
        player.render(canvas, width, height)
    }

    val nsView = NSView(window.frame).apply {
        autoresizingMask = NSViewHeightSizable or NSViewWidthSizable
    }
    window.contentView!!.addSubview(nsView)
    skiaLayer.attachTo(nsView)

    window.center()
    window.makeKeyAndOrderFront(app)
    app.activateIgnoringOtherApps(true)
    skiaLayer.needRender()

    if (exitAfterMillis != null) {
        NSTimer.scheduledTimerWithTimeInterval(
            interval = exitAfterMillis / 1000.0,
            repeats = false
        ) {
            println(RENDER_SUCCESS_MESSAGE)
            app.terminate(null)
        }
    }

    app.run()
}
