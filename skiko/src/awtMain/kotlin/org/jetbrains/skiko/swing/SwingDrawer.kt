package org.jetbrains.skiko.swing

import org.jetbrains.skia.Surface
import java.awt.Graphics2D

/**
 * Interface for rendering Skia surfaces onto an AWT/Swing `Graphics2D` instance.
 *
 * @see SoftwareSwingDrawer
 */
interface SwingDrawer {
    fun draw(g: Graphics2D, surface: Surface)
    fun dispose()
}