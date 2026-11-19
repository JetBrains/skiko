package org.jetbrains.skiko.swing

import org.jetbrains.skia.Surface
import java.awt.Graphics2D

/**
 * Interface for rendering Skia surfaces onto an AWT/Swing `Graphics2D` instance.
 *
 * @see SoftwareSwingDrawer
 */
internal interface SwingPainter {
    fun paint(g: Graphics2D, surface: Surface, texture: Long)
    fun dispose()
}
