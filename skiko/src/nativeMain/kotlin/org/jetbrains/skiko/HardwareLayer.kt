package org.jetbrains.skiko.native

import kotlinx.cinterop.readValue
import platform.Cocoa.*
import platform.AppKit.*
import platform.Foundation.*
import platform.CoreGraphics.*

abstract class HardwareLayer {

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

    open fun init() {
    }

    protected open fun nativeInit(platformInfo: Long) {
    }

    open fun dispose() {

    }

    protected open fun contentScaleChanged() = Unit

    private fun checkContentScale() {
    }

    internal abstract fun update(nanoTime: Long)

    internal abstract fun draw()

    val contentScale: Float
        get() = _contentScale!!
}
