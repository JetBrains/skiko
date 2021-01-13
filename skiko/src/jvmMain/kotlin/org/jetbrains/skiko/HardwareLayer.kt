package org.jetbrains.skiko

import java.awt.Canvas
import java.awt.Graphics
import java.awt.event.HierarchyEvent
import javax.swing.SwingUtilities.convertPoint
import javax.swing.SwingUtilities.getRootPane

abstract class HardwareLayer : Canvas(), Drawable {
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
            _contentScale = platformOperations.getDpiScale(this)
            isInit = true
        }
    }

    protected open fun contentScaleChanged() = Unit

    override fun paint(g: Graphics) {
        val contentScale = platformOperations.getDpiScale(this)
        if (contentScale != _contentScale) {
            _contentScale = contentScale
            contentScaleChanged()
        }
        display()
    }

    open fun display() {
        this.updateLayer()
        this.redrawLayer()
    }

    open fun draw() {}

    external override fun redrawLayer()

    external override fun updateLayer()

    external override fun disposeLayer()

    override val windowHandle: Long
        external get

    override val contentScale: Float
        get() = _contentScale!!

    val absoluteX: Int
        get() = convertPoint(this, x, y, getRootPane(this)).x

    val absoluteY: Int
        get() = convertPoint(this, x, y, getRootPane(this)).y

    var fullscreen: Boolean
        get() = platformOperations.isFullscreen(this)
        set(value) = platformOperations.setFullscreen(this, value)
}