package org.jetbrains.skiko.sample

import kotlinx.cinterop.useContents
import org.jetbrains.skiko.SkiaLayer
import platform.AppKit.NSEvent
import platform.AppKit.NSTrackingActiveInActiveApp
import platform.AppKit.NSTrackingArea
import platform.AppKit.NSTrackingMouseMoved
import platform.AppKit.NSView
import platform.AppKit.NSViewHeightSizable
import platform.AppKit.NSViewWidthSizable
import platform.AppKit.NSWindow

class MacosClocks(skiaLayer: SkiaLayer, window: NSWindow) : Clocks(skiaLayer.renderApi) {
    init {
        val nsView = object : NSView(window.frame) {
            private var trackingArea : NSTrackingArea? = null

            override fun updateTrackingAreas() {
                trackingArea?.let { removeTrackingArea(it) }
                trackingArea = NSTrackingArea(
                    rect = bounds,
                    options = NSTrackingActiveInActiveApp or NSTrackingMouseMoved,
                    owner = this,
                    userInfo = null
                )
                addTrackingArea(trackingArea!!)
            }

            override fun mouseMoved(event: NSEvent) {
                val height = frame.useContents { size.height }
                event.locationInWindow.useContents {
                    xpos = x
                    ypos = height - y
                }
            }
        }

        val contentView = window.contentView!!
        nsView.autoresizingMask = NSViewHeightSizable or NSViewWidthSizable
        contentView.addSubview(nsView)
        skiaLayer.attachTo(nsView)
    }
}
