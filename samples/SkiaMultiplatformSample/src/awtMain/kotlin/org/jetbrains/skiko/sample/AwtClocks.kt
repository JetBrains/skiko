package org.jetbrains.skiko.sample

import org.jetbrains.skiko.CursorManager
import org.jetbrains.skiko.PredefinedCursors
import org.jetbrains.skiko.SkiaLayer
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

class AwtClocks(private val layer: SkiaLayer) : Clocks(layer.renderApi), MouseMotionListener, MouseWheelListener {
    private val cursorManager = CursorManager()

    init {
        layer.addMouseMotionListener(this)
    }

    override fun mouseDragged(event: MouseEvent) {
        xOffset += event.x - xpos
        yOffset += event.y - ypos
        xpos = event.x.toDouble()
        ypos = event.y.toDouble()
    }

    override fun mouseMoved(event: MouseEvent) {
        if (event.x > 200) {
            cursorManager.setCursor(layer.component, PredefinedCursors.HAND)
        } else {
            cursorManager.setCursor(layer.component, PredefinedCursors.DEFAULT)
        }
        xpos = event.x.toDouble()
        ypos = event.y.toDouble()
    }

    override fun mouseWheelMoved(event: MouseWheelEvent) {
        if (event.isControlDown) {
            rotate += if (event.wheelRotation < 0) -5.0 else 5.0
        } else {
            scale *= if (event.wheelRotation < 0) 0.9 else 1.1
        }
    }
}
