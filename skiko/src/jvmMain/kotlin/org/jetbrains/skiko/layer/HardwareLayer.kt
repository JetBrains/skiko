package org.jetbrains.skiko.layer

import java.awt.Graphics
import java.awt.Canvas
import javax.swing.SwingUtilities.convertPoint
import javax.swing.SwingUtilities.getRootPane
import org.jetbrains.skiko.Library
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.properties.MacOSProperties
import org.jetbrains.skiko.properties.LinuxProperties
import org.jetbrains.skiko.properties.WindowsProperties

abstract class HardwareLayer : Canvas(), Drawable {

    companion object {
        init {
            Library.load()
        }
    }

    override fun paint(g: Graphics) {
        display()
    }

    private val properties = when (hostOs) {
        OS.MacOS -> MacOSProperties()
        OS.Linux -> LinuxProperties()
        OS.Windows -> WindowsProperties()
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

    val isFullscreen: Boolean
        get() = properties.isFullscreen(this)
    
    fun makeFullscreen(value: Boolean) {
        properties.makeFullscreen(this, value)
    }
}