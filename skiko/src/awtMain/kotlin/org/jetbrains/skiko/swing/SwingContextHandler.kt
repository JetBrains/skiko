package org.jetbrains.skiko.swing

import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.context.ContextHandler
import java.awt.Graphics2D

internal abstract class SwingContextHandler(
    drawContent: Canvas.() -> Unit
) : ContextHandler(drawContent) {
    protected var graphics: Graphics2D? = null

    fun draw(g: Graphics2D) {
        this.graphics = g
        draw()
        this.graphics = null
    }
}