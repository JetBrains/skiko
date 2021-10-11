package org.jetbrains.skiko

import java.awt.Canvas
import java.awt.Component
import java.awt.Graphics
import java.awt.event.InputMethodEvent
import javax.accessibility.Accessible
import javax.accessibility.AccessibleContext

internal open class HardwareLayer(
    externalAccessible: ((Component) -> Accessible)? = null
) : Canvas() {
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

    open fun dispose() {
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

    val contentHandle: Long
        get() = useDrawingSurfacePlatformInfo(::getContentHandle)

    val windowHandle: Long
        get() = useDrawingSurfacePlatformInfo(::getWindowHandle)

    val contentScale: Float
        get() = _contentScale!!

    var fullscreen: Boolean
        get() = platformOperations.isFullscreen(this)
        set(value) = platformOperations.setFullscreen(this, value)

    private external fun getContentHandle(platformInfo: Long): Long
    private external fun getWindowHandle(platformInfo: Long): Long

    private val _externalAccessible = externalAccessible?.invoke(this)
    override fun getAccessibleContext(): AccessibleContext {
        val res = _externalAccessible?.accessibleContext
        return res ?: super.getAccessibleContext()
    }
}