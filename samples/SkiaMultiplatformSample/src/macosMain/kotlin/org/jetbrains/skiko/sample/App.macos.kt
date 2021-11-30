package org.jetbrains.skiko.sample

import platform.AppKit.*

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import kotlinx.cinterop.*
import platform.Foundation.NSMakeRect
import platform.Foundation.NSBundle
import platform.Foundation.NSNotification
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject

fun makeApp(skiaLayer: SkiaLayer) = Clocks(skiaLayer)

fun main() {
    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
    val appName = "SkikoNative"
    var bar = NSMenu()
    app.setMainMenu(bar)
    var appMenuItem = bar.addItemWithTitle(appName, null, "");
    var appMenu = NSMenu()
    appMenuItem.setSubmenu(appMenu)
    appMenu.addItemWithTitle("About $appName", NSSelectorFromString("orderFrontStandardAboutPanel:"), "a")
    appMenu.addItemWithTitle("Quit $appName", NSSelectorFromString("terminate:"), "q")
    
    app.delegate = object: NSObject(), NSApplicationDelegateProtocol {
        override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean {
            return true
        }
    }
    val windowStyle = NSWindowStyleMaskTitled or
                NSWindowStyleMaskMiniaturizable or
                NSWindowStyleMaskClosable or
                NSWindowStyleMaskResizable
    val window = object: NSWindow(
        contentRect = NSMakeRect(0.0, 0.0, 640.0, 480.0),
        styleMask = windowStyle,
        backing = NSBackingStoreBuffered,
        defer = false
    ) {
        override fun canBecomeKeyWindow(): Boolean {
            return true
        }
    }
    val skiaLayer = SkiaLayer()
    skiaLayer.skikoView = GenericSkikoView(skiaLayer, makeApp(skiaLayer))
    skiaLayer.attachTo(window)
    window.makeKeyAndOrderFront(app)
    app.run()
}
