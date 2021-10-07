package org.jetbrains.skiko.native

import org.jetbrains.skiko.HardwareLayer
import platform.AppKit.*
import platform.Foundation.*

internal class MacOSHardwareLayer: HardwareLayer() {
    val nsView = NSView(NSMakeRect(0.0, 0.0, 640.0, 480.0))

    // getDpiScale is expensive operation on some platforms, so we cache it
    private var _contentScale: Float? = null
    private var isInit = false

    fun checkIsShowing() {
        if (!isInit && !nsView.hiddenOrHasHiddenAncestor) {
            // _contentScale = getDpiScale()
            _contentScale = 1.0f // TODO: what's the proper way here?
            init()
            isInit = true
        }
    }

    val contentScale: Float
        get() = _contentScale!!
}
