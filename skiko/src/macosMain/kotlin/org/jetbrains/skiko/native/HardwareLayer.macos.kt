package org.jetbrains.skiko

import platform.AppKit.*
import platform.Foundation.*

internal actual open class HardwareLayer {
    val nsView = NSView(NSMakeRect(0.0, 0.0, 640.0, 480.0))

    // getDpiScale is expensive operation on some platforms, so we cache it
    private var _contentScale: Float? = null
    private var isInit = false

    fun checkIsShowing() {
        if (!isInit && !nsView.hiddenOrHasHiddenAncestor) {
            // _contentScale = getDpiScale()
            _contentScale = 1.0f // TODO: what's the proper way here?
            isInit = true
        }
    }

    val contentScale: Float
        get() = _contentScale!!

    actual open fun init() {}

    actual open fun dispose() {}
}
