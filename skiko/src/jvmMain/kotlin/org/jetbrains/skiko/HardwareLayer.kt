package org.jetbrains.skiko

import java.awt.Graphics
import java.awt.Canvas
import javax.swing.SwingUtilities.convertPoint
import javax.swing.SwingUtilities.getRootPane

abstract class HardwareLayer : Canvas(), Drawable {

abstract class HardwareLayer : Canvas(), Drawable {

    companion object {
        init {
            Library.load()
        }
    }

    override fun paint(g: Graphics) {
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
        get() = graphicsConfiguration.defaultTransform.scaleX.toFloat()

    val absoluteX: Int
        get() = convertPoint(this, x, y, getRootPane(this)).x

    val absoluteY: Int
        get() = convertPoint(this, x, y, getRootPane(this)).y

    var fullscreen: Boolean
        get() = platformOperations.isFullscreen(this)
        set(value) = platformOperations.setFullscreen(this, value)
}