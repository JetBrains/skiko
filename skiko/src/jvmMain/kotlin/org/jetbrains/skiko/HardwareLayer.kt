package org.jetbrains.skiko

import java.awt.Canvas
import java.awt.Graphics
import java.awt.event.InputMethodEvent

internal open class HardwareLayer : Canvas() {
    companion object {
        init {
            Library.load()
        }
    }

    // getDpiScale is expensive operation on some platforms, so we cache it
    private var _contentScale: Float? = null

    fun defineContentScale() {
        _contentScale = getDpiScale()
    }

    override fun paint(g: Graphics) {}

    open fun init() {
        useDrawingSurfacePlatformInfo(::nativeInit)
    }

    fun dispose() {
        nativeDispose()
    }

    fun doProcessInputMethodEvent(e: InputMethodEvent?) {
        processInputMethodEvent(e)
    }

    private external fun nativeInit(platformInfo: Long)
    private external fun nativeDispose()

    // TODO checkContentScale is called before init. it is ok, but when we fix getDpiScale on Linux we should check [isInit]
    fun checkContentScale(): Boolean {
        val contentScale = getDpiScale()
        if (contentScale != _contentScale) {
            _contentScale = contentScale
            return true
        }
        return false
    }

    private fun getDpiScale(): Float {
        val scale = platformOperations.getDpiScale(this)
        check(scale > 0) { "HardwareLayer.contentScale isn't positive: $contentScale"}
        return scale
    }

    val windowHandle: Long
        get() = useDrawingSurfacePlatformInfo(::getWindowHandle)

    val contentScale: Float
        get() = _contentScale!!

    var fullscreen: Boolean
        get() = platformOperations.isFullscreen(this)
        set(value) = platformOperations.setFullscreen(this, value)

    private external fun getWindowHandle(platformInfo: Long): Long
}