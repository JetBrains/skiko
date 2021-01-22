package org.jetbrains.skiko

import java.awt.Canvas
import java.awt.Graphics
import java.awt.event.HierarchyEvent

abstract class HardwareLayer : Canvas() {
    companion object {
        init {
            Library.load()
        }
    }

    // getDpiScale is expensive operation on some platforms, so we cache it
    private var _contentScale: Float? = null
    private var isInit = false

    init {
        @Suppress("LeakingThis")
        addHierarchyListener {
            if (it.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
                checkIsShowing()
            }
        }
    }

    private fun checkIsShowing() {
        if (!isInit && isShowing) {
            _contentScale = getDpiScale()
            init()
            isInit = true
        }
    }

    protected open external fun init()
    open external fun dispose()

    protected open fun contentScaleChanged() = Unit

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        super.setBounds(x, y, width, height)
        checkContentScale()
    }

    override fun paint(g: Graphics) {
        checkContentScale()
    }

    // TODO checkContentScale is called before init. it is ok, but when we fix getDpiScale on Linux we should check [isInit]
    private fun checkContentScale() {
        val contentScale = getDpiScale()
        if (contentScale != _contentScale) {
            _contentScale = contentScale
            contentScaleChanged()
        }
    }

    private fun getDpiScale(): Float {
        val scale = platformOperations.getDpiScale(this)
        check(scale > 0) { "HardwareLayer.contentScale isn't positive: $contentScale"}
        return scale
    }

    // Should be called in Swing thread
    internal abstract suspend fun update(nanoTime: Long)

    // Should be called in the OpenGL thread, and only once after update
    internal abstract fun draw()

    val windowHandle: Long
        external get

    val contentScale: Float
        get() = _contentScale!!

    var fullscreen: Boolean
        get() = platformOperations.isFullscreen(this)
        set(value) = platformOperations.setFullscreen(this, value)
}